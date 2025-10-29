import base64
import os
import tempfile
from concurrent.futures import ThreadPoolExecutor, as_completed

from django.conf import settings
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from pdf2image import convert_from_path

if not getattr(settings, "OPENAI_API_KEY", None):
    raise ValueError("OPENAI_API_KEY not found in Django settings.")

vision_summary_prompt = ChatPromptTemplate.from_template(
    """
You are an AI teaching assistant specialized in analyzing educational materials.

You are given an image extracted from a class slide, textbook, or diagram.

# Instructions
- You must transcribe all the text shown in the image without leaving anything out.
- You have to transcribe **everything** from the image resource into pure text.
- Also you must additionaly describe the **core educational content** of the image, not its appearance.
- If the image includes:
  - **Tables:** transcribe everything in the entries.
  - **Graphs or charts:** explain what variables are shown and the main trend or law they illustrate.
  - **Equations or formulas:** transcribe as it is.
  - **diagrams:** describe the sequence or cause-effect relationship.
- Do not mention redundant details like colors, shapes, or positions.
- Summarize in **natural, fluent text**.

# Output Format
Return text with this structure:

**Example:**
사과는 왜 땅에 떨어질까요?

중력은 물체의 질량(mass) 때문에 생기는 자연의 기본적인 힘이에요.
모든 물체는 질량을 가지고 있고, 질량이 있는 물체끼리는 서로를 끌어당기죠.
이 현상을 만유인력의 법칙(Law of Universal Gravitation) 이라고 합니다.

아이작 뉴턴은 중력을 이렇게 설명했어요:
두 물체 사이에는 서로를 끌어당기는 힘이 작용하며,
그 힘의 크기는 두 물체의 질량에 비례하고,
두 물체 사이의 거리의 제곱에 반비례한다.

수식으로는 다음과 같아요:
F = G × (m₁ × m₂) / r²
여기서 F는 두 물체 사이의 중력의 크기 (단위: 뉴턴, N),
G는 만유인력 상수 (약 6.67 × 10⁻¹¹ N·m²/kg²),
m₁, m₂는 두 물체의 질량 (킬로그램, kg),
r은 두 물체 사이의 거리 (미터, m)입니다.

즉, 질량이 큰 물체일수록 중력이 강하고, 거리가 멀어질수록 중력이 약해집니다.
지구의 질량이 아주 크기 때문에, 우리를 비롯한 모든 물체가 지구 중심 방향으로 끌려가죠.
그래서 공을 던지면 결국 땅으로 떨어지고, 우리가 ‘무게’를 느끼는 것도 바로 이 중력 때문이에요.

예를 들어, 달은 지구보다 질량이 작아서 중력이 약해요.
그래서 같은 물체라도 달에서는 지구에서보다 약 6분의 1 정도의 무게만 느껴집니다.

결국, 중력은 질량을 가진 모든 물체가 서로 끌어당기는 힘이며,
이 힘 덕분에 행성은 태양 주위를 돌고, 달은 지구 주위를 도는 거예요.
                                                         
# Rules
- You must transcribe all the text shown in the image without leaving anything out.
- Focus on educational meaning, not aesthetics.
- Resources may contain both Korean and English.
"""
)


def encode_image_to_base64(image_path: str) -> str:
    """Encode an image file to a data URL for GPT vision input."""
    with open(image_path, "rb") as f:
        return f"data:image/png;base64,{base64.b64encode(f.read()).decode('utf-8')}"


def _process_page(llm: ChatOpenAI, page, page_num: int, tmpdir: str) -> str:
    """Process a single PDF page."""
    tmp_img_path = os.path.join(tmpdir, f"page_{page_num}.png")
    page.save(tmp_img_path, "PNG")
    image_data = encode_image_to_base64(tmp_img_path)

    messages = vision_summary_prompt.format_messages()
    response = llm.invoke(
        [
            *messages,
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "Analyze this educational image."},
                    {"type": "image_url", "image_url": {"url": image_data}},
                ],
            },
        ]
    )

    return response.content.strip()


def summarize_pdf_from_s3(local_pdf_path: str) -> str:
    """Analyze each PDF page using GPT-4o Vision and return summarized text."""
    llm = ChatOpenAI(model="gpt-4o-mini", temperature=0.2)
    pages = convert_from_path(local_pdf_path, dpi=200)
    summaries = [None] * len(pages)  # 순서 보장을 위한 빈 리스트

    with tempfile.TemporaryDirectory() as tmpdir:
        # ThreadPoolExecutor를 사용하여 병렬 처리 (최대 10개 동시 처리)
        with ThreadPoolExecutor(max_workers=10) as executor:
            # 모든 페이지에 대해 작업 제출
            future_to_index = {
                executor.submit(_process_page, llm, page, i, tmpdir): i - 1 for i, page in enumerate(pages, start=1)
            }

            # 완료된 작업부터 결과 수집 (순서 보장)
            for future in as_completed(future_to_index):
                index = future_to_index[future]
                try:
                    summaries[index] = future.result()
                except Exception as e:
                    summaries[index] = f"Error processing page {index + 1}: {str(e)}"

    return "\n\n---\n\n".join(summaries)
