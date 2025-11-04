from assignments.models import Assignment, Material
from django.core.management.base import BaseCommand


class Command(BaseCommand):
    help = "테스트용 Material 데이터 생성 (PDF 타입)"

    def handle(self, *args, **options):
        self.stdout.write("테스트용 Material 생성 시작...")

        try:
            # 모든 과제 가져오기
            assignments = Assignment.objects.all().order_by("id")
            if not assignments.exists():
                self.stdout.write(self.style.WARNING("과제가 없습니다. 먼저 과제를 생성해주세요."))
                return

            self.stdout.write(f"총 {assignments.count()}개의 과제에 Material 생성")

            # 1번 assignment용 요약 텍스트 (1번과 2번 summary 합친 것)
            assignment_1_summary = """**Text Transcription:**

중2 과학

4단원 - 소화, 순환, 호흡, 배설 (2)

IV - 2 호흡과 배설 (1)

1. 호흡과 호흡 기관
(1) 호흡: 산소가 영양소와 반응하여 물과 이산화탄소로 분해되고, 생활에 필요한 에너지가 발생하는 과정
(2) 호흡 기관

| 코 | 콧속을 지나는 공기의 온도와 습도를 알맞게 조절·안쪽 벽의 털과 점액으로 먼지를 거름 |
| 기관 | 안쪽 벽에 있는 섬모가 이물질을 거름 |
| 기관지 | 기관에서 나누어져 폐로 들어가며, 폐 속에서 더 갈게 나누어져 폐포와 연결 |
| 폐 | 흉강에 좌우 한 쌍씩 있음 · 수많은 폐포로 구성 → 표면적이 넓어 기체 교환이 효율적으로 일어남 |

2. 호흡 운동
(1) 호흡 운동의 원리: 갈비뼈와 횡격막의 상하 운동에 의한 흉강의 부피 변화

| 구분 | 갈비뼈 | 횡격막 | 흉강 부피 | 흉강 압력 | 폐 부피 | 폐 압력 | 공기 이동 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 들숨 | 올라감 | 내려감 | 커짐 | 작아짐 | 커짐 | 작아짐 | 외부 → 폐 |
| 날숨 | 내려감 | 올라감 | 작아짐 | 커짐 | 작아짐 | 커짐 | 폐 → 외부 |

(2) 들숨과 날숨의 성분: 들숨에는 날숨에 비해 산소가 많고 이산화탄소가 적으며, 날숨에는 들숨에 비해 산소가 적고, 이산화탄소가 많다.

3. 외호흡과 내호흡 기체 교환 원리는 분압 차에 의한 확산

| 구분 | (가) 외호흡(정맥혈 → 동맥혈) | (나) 내호흡(동맥혈 → 정맥혈) |
| --- | --- | --- |
| 의미 | 폐포와 모세 혈관 사이의 기체 교환 | 모세 혈관과 조직 세포 사이의 기체 교환 |
| 기체 교환 | 산소 ↑ 이산화탄소 ↓ | 산소 ↓ 이산화탄소 ↑ |

4. 세포 호흡과 에너지
(1) 세포 호흡: 조직 세포에서 산소를 이용해 영양소를 분해하여 에너지를 얻는 과정
(2) 에너지의 이용: 체온 유지, 성장 및 근육 운동 등 생명 활동에 이용

IV - 2 호흡과 배설 (2)

1. 배설
(1) 배설: 세포 호흡 결과 생성된 노폐물을 몸 밖으로 내보내는 과정
(2) 노폐물의 생성

| 영양소 | 구성 물질 | 노폐물 생성 |
|--------|-----------|-------------|
| 탄수화물, 지방 | 탄소, 수소, 산소 | 물, 이산화탄소 |
| 단백질 | 탄소, 수소, 산소, 질소 | 물, 이산화탄소, 암모니아 |

(3) 노폐물의 배설

| 물 | 이산화탄소 | 암모니아 |
|----|-------------|----------|
| 음수에서 다시 사용되거나 남음, 오줌, 땀을 통해 몸 밖으로 배설 | 날숨을 통해 몸 밖으로 배설 | 간에서 요소로 전환된 후 혈과 함께 오줌을 통해 몸 밖으로 배설 |

2. 배설 기관

| 콩팥 동맥 | 심장에서 온 혈액이 콩팥으로 들어가는 혈관 |
|----------|----------------------------------------|
| 콩팥 정맥 | 콩팥에서 노폐물이 걸러진 혈액이 콩팥에서 나와 심장으로 이동하는 혈관 |
| 콩팥 | 혈액 속의 노폐물을 걸러 오줌을 생성하는 기관 |
| 콩팥 | 겉질 | 말피기 소체(사구체 + 보먼주머니) + 일부 세뇨관 |
| 콩팥 | 속질 | 세뇨관과 집합관이 있음 |
| 콩팥 깔때기 | 오줌이 모여 혈과 저장되는 기관 |
| 오줌관 | 콩팥과 방광을 연결하는 기관 |
| 방광 | 콩팥에서 만들어진 오줌을 모아두는 기관 |
| 요도 | 방광에 모인 오줌을 몸 밖으로 내보내는 통로 |

**Core Educational Content:**

This educational material explains the process of respiration and the respiratory system. It describes how respiration involves the reaction of oxygen with nutrients to produce water, carbon dioxide, and energy necessary for life. The respiratory organs include the nose, trachea, bronchi, and lungs, each playing a role in filtering, warming, and humidifying air, as well as facilitating gas exchange.

The mechanics of breathing involve the movement of the rib cage and diaphragm, which change the volume and pressure in the thoracic cavity, allowing air to move in and out of the lungs. During inhalation, the rib cage rises and the diaphragm lowers, increasing lung volume and decreasing pressure, drawing air in. Exhalation is the reverse process.

The material also distinguishes between external respiration (gas exchange between alveoli and blood) and internal respiration (gas exchange between blood and tissue cells), highlighting the differences in oxygen and carbon dioxide levels during these processes.

The image explains cellular respiration and excretion processes. Cellular respiration involves breaking down nutrients using oxygen in cells to produce energy, which is used for maintaining body temperature, growth, and muscle movement. Excretion is the process of removing waste products from the body, which are generated as a result of cellular respiration. The table outlines how different nutrients like carbohydrates, fats, and proteins are broken down into waste products such as water, carbon dioxide, and ammonia. The excretion organs include the kidneys, which filter blood to produce urine, and the urinary system, which includes the ureters, bladder, and urethra, responsible for transporting and expelling urine from the body."""

            # 2번 assignment용 요약 텍스트 (수학 기초 개념)
            assignment_2_summary = """**수학 기초 개념 정리**

1. 함수의 개념
- 함수: 두 변수 x, y 사이에 x의 값이 정해지면 y의 값이 오직 하나씩 정해지는 관계
- 정의역: 함수에서 x가 취할 수 있는 값의 범위
- 치역: 함수에서 y가 취할 수 있는 값의 범위

2. 일차함수
- y = ax + b (a ≠ 0)의 꼴로 나타내어지는 함수
- 그래프: 직선
- 기울기: a (x가 1만큼 증가할 때 y가 증가하는 양)

3. 이차함수
- y = ax² + bx + c (a ≠ 0)의 꼴로 나타내어지는 함수
- 그래프: 포물선
- 꼭짓점: (-b/2a, f(-b/2a))

**학습 포인트:**
- 함수의 정의와 성질을 정확히 이해하기
- 일차함수와 이차함수의 그래프 그리기
- 함수의 활용 문제 해결하기"""

            # 3번 assignment용 요약 텍스트 (영어 문법 기초)
            assignment_3_summary = """**영어 문법 기초**

1. 문장의 구성 요소
- 주어(Subject): 문장의 주체가 되는 부분
- 동사(Verb): 주어의 동작이나 상태를 나타내는 부분
- 목적어(Object): 동사의 대상이 되는 부분
- 보어(Complement): 주어나 목적어를 보충 설명하는 부분

2. 시제
- 현재시제: 현재의 사실이나 일반적인 사실
- 과거시제: 과거에 일어난 일
- 미래시제: 미래에 일어날 일

3. 조동사
- can: 능력, 가능성
- will: 미래, 의지
- should: 의무, 충고
- must: 의무, 추측

**학습 포인트:**
- 문장의 기본 구조 파악하기
- 시제의 올바른 사용법 익히기
- 조동사의 의미와 용법 구분하기"""

            created_count = 0
            existing_count = 0

            for i, assignment in enumerate(assignments, 1):
                # 1, 2, 3번 assignment는 특별한 요약 사용
                if i == 1:
                    summary_text = assignment_1_summary
                    s3_key = f"test_materials/assignment_{i}_respiration_excretion_combined.pdf"
                elif i == 2:
                    summary_text = assignment_2_summary
                    s3_key = f"test_materials/assignment_{i}_math_functions.pdf"
                elif i == 3:
                    summary_text = assignment_3_summary
                    s3_key = f"test_materials/assignment_{i}_english_grammar.pdf"
                else:
                    # 4번째부터는 기본 텍스트 사용
                    summary_text = "기본 학습 자료입니다."
                    s3_key = f"test_materials/assignment_{i}_default_material.pdf"

                # PDF Material 생성
                material, created = Material.objects.get_or_create(
                    assignment=assignment,
                    kind=Material.Kind.PDF,
                    defaults={
                        "s3_key": s3_key,
                        "bytes": len(summary_text.encode("utf-8")),
                        "summary": summary_text,  # PDF 요약 내용을 content에 저장
                    },
                )

                if created:
                    self.stdout.write(self.style.SUCCESS(f"✓ 생성됨: {assignment.title} - PDF 자료 (PDF)"))
                    created_count += 1
                else:
                    self.stdout.write(self.style.WARNING(f"⚠ 이미 존재: {assignment.title} - PDF 자료 (PDF)"))
                    existing_count += 1

            self.stdout.write(
                self.style.SUCCESS(
                    f"\nMaterial 생성 완료! 새로 생성된 항목: {created_count}개, 이미 존재한 항목: {existing_count}개"
                )
            )

        except Exception as e:
            self.stdout.write(self.style.ERROR(f"Material 생성 중 오류 발생: {str(e)}"))
            raise
