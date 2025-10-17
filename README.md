# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal analysis (ASR, prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Conversational Review**: Short, AI-guided verbal review sessions with open-ended questions.
- **Adaptive Quizzes**: Automatically generated follow-up questions tailored to each student’s weak points.
- **Multimodal Analysis**: Evaluation of content accuracy, reasoning structure, and supportive delivery metrics (speech rate, pauses, intonation).
- **Instant Feedback**: Real-time hints and corrective prompts during spoken responses.
- **Progress Tracking**: Visual summaries of concept mastery and learning trends over time.

## Demo 2: VoiceTutor UI & Quiz Generation API

## Demo 2-(a): Frontend VoiceTutor UI

[Demo Video: Teacher's view](demo/videos/teacher_view.mp4)  
[Demo Video: Student's view (1)](demo/videos/student_view1.mp4)  
[Demo Video: Student's view (2)](demo/videos/student_view2.mp4)

We've built the frontend of the Voice Tutor project using Android Studio.  
During sign-up, user can choose between a student account and a teacher account.  
Entire UI (including question creation, assignment distribution, task submission) is in frontend/ directory.  
Note that sign-in and sign-up api are already integrated.

Open frontend/ with android studio to see the detail.

## Demo 2-(b): API for assignment generation

[Demo Video: question generation](demo/videos/question_generation.mp4)

As part of VoiceTutor, we've implemented an integrated backend API for assignment creation and automated quiz generation.  
This module allows teachers to upload class materials (in PDF format) to S3, automatically summarize them using GPT-4o Vision, and generate base quiz questions for students.  
Note that **tail question** generation logic is implemented in **research/tail_question_generator/generate_question_few_shot.py**, and it will be integrated on iter-3.  
Demo 2-(b) shows **base question** generation.  

⚙️ Overview

This API handles the entire pipeline of assignment creation and content generation:

1. Generate a presigned S3 upload URL  
   → Allows teachers to upload a PDF directly from the Android client.
2. Create database records  
   → Inserts new entries into the Assignment and Material tables.
3. Summarize PDF content  
   → Uses the summarize_pdf_from_s3() function powered by GPT-4o Vision, which:

- Downloads the uploaded PDF from S3
- Converts each page into images (pdf2image)
- Transcribes textual contents and extracts educational meaning from diagrams or formulas
- Returns a unified, natural Korean summary

4. Generate base quiz questions
   → Calls generate_base_quizzes() which leverages GPT-4o-mini to:

- Design diverse, concept-driven educational questions
- Avoid trivial or repetitive patterns
- Produce a list of structured questions with topic, difficulty, and explanation

5. Return structured API response
   → Includes assignment metadata, summarized text preview, and generated quiz list.

<br />

<details>
<summary> Click to expand demo 2-(b) details</summary>

#### Step 1: Backend setup

1. **Navigate to Backend Directory**

   ```bash
   cd backend
   ```

2. **Create .env**  
   Create your S3 bucket first, and make **.env** at **backend/** directory like below.

   ```bash
   OPENAI_API_KEY='YOUR_OPENAI_API_KEY'
   SECRET_KEY='DJANGO_SECRET_KEY'
   
   AWS_ACCESS_KEY_ID='YOUR_AWS_ACCESS_KEY_ID'
   AWS_SECRET_ACCESS_KEY='YOUR_AWS_SECRET_ACCESS_KEY'
   AWS_REGION=ap-northeast-2
   AWS_STORAGE_BUCKET_NAME='YOUR_S3_BUCKET_NAME' 
   ```

3. **Create Virtual Environment**

   ```bash
   python -m venv venv
   ```

4. **Activate Virtual Environment**

   ```bash
   # Windows
   venv\Scripts\activate

   # Linux/Mac
   source venv/bin/activate
   ```

5. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

#### Step 2: Run Django Server

```bash
python manage.py runserver
```

#### Step 3: Visit Swagger Page

eg. https://localhost:8000/swagger/

#### Step 4: POST /assignment/create/

First, you can test **POST /assignment/create/** (과제 생성) API.

Sample Request

```bash
{
  "title": "순환계와 배설계 과제",
  "class_id": 0,
  "grade": "중3",
  "subject": "과학",
  "description": "샘플 과제입니다!",
  "due_at": "2025-10-17"
}
```

Sample Response

```bash
# 201 created
{
  "assignment_id": 6,
  "material_id": 9,
  "s3_key": "pdf/0/6/blah-blah.pdf",
  "upload_url": "https://s3.ap-northeast-2.amazonaws.com/voice-tutor/pdf/0/6/blah-blah.pdf?X-Amz-Algorithm=blah-blah&X-Amz-Credential=blah-blah"
}
```

#### Step 5: Upload sample pdf

Run **demo/upload.py** to upload sample pdf using result from **Step 4**

#### Step 6: POST /questions/create/

Second, you can test POST method: **/questions/create/**

Sample Request

```bash
{
  "assignment_id": 6,
  "material_id": 9,
  "total_number": 3
}
```

Sample Response

```bash
{
  "assignment_id": 6,
  "material_summary_id": 10,
  "summary_preview": "**중2 과학**\n\n핵심요점정리\n\n4단원 - 소화, 순환, 호흡, 배설 (2)\n\nIV - 2 호흡과 배설 (1)\n\n1. 호흡과 호흡 기관\n(1) 호흡: 산소가 영양소와 반응하여 물",
  "questions": [
    {
      "id": 11,
      "number": 1,
      "question": "호흡 운동에서 갈비뼈와 횡격막의 움직임이 흉강의 압력에 어떤 영향을 미치는지 설명해 보세요.",
      "answer": "갈비뼈가 올라가고 횡격막이 내려가면 흉강의 부피가 커져 압력이 낮아지고, 이로 인해 외부에서 폐로 공기가 들어옵니다.",
      "explanation": "호흡 운동은 갈비뼈와 횡격막의 움직임에 의해 흉강의 부피와 압력이 변화하여 공기가 폐로 유입되거나 배출되는 과정을 설명합니다.",
      "difficulty": "medium"
    },
    {
      "id": 12,
      "number": 2,
      "question": "외호흡과 내호흡의 차이점에 대해 설명하고, 각 과정에서 산소와 이산화탄소의 변화는 어떻게 이루어지는지 서술해 보세요.",
      "answer": "외호흡은 폐포와 모세 혈관 사이에서 산소가 혈액으로 들어가고 이산화탄소가 나오는 과정이며, 내호흡은 혈액과 조직 세포 사이에서 산소가 세포로 들어가고 이산화탄소가 혈액으로 나오는 과정입니다.",
      "explanation": "이 질문은 학생들이 외호흡과 내호흡의 정의와 기체 교환의 원리를 이해하고, 각 과정에서 기체의 농도 변화에 대해 사고하도록 유도합니다.",
      "difficulty": "hard"
    },
    {
      "id": 13,
      "number": 3,
      "question": "콩팥에서 오줌이 생성되는 과정에서 여과, 재흡수, 분비가 각각 어떤 역할을 하는지 설명해 보세요.",
      "answer": "여과는 사구체에서 혈액의 노폐물을 걸러내는 과정, 재흡수는 필요한 물질을 다시 혈액으로 돌려보내는 과정, 분비는 노폐물을 혈액에서 세뇨관으로 이동시키는 과정입니다.",
      "explanation": "이 질문은 학생들이 콩팥의 기능과 오줌 생성 과정의 세 가지 주요 단계를 이해하고, 각 단계의 중요성을 사고하도록 합니다.",
      "difficulty": "medium"
    }
  ]
}
```

</details>
