# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal analysis (ASR, prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Conversational Review**: Short, AI-guided verbal review sessions with open-ended questions.
- **Adaptive Quizzes**: Automatically generated follow-up questions tailored to each student’s weak points.
- **Multimodal Analysis**: Evaluation of content accuracy, reasoning structure, and supportive delivery metrics (speech rate, pauses, intonation).
- **Progress Tracking**: Visual summaries of concept mastery and learning trends over time.

## Demo 3: Teacher Screen & Student Screen

<details>
<summary> Click to expand how to run demo 3 in local</summary>

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

   **Create secret key (Optional)**
   If you want to create secret key for Django, run the command below to generate a key.

   ```bash
   .venv/bin/python -c 'from django.core.management.utils import get_random_secret_key; print(get_random_secret_key()'
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

6. **SentenceTransformer model setup (for semantic feature extraction)**

   ```bash
   # download model
   python -c "from sentence_transformers import SentenceTransformer; model = SentenceTransformer('snunlp/KR-SBERT-V40K-klueNLI-augSTS'); model.save('submissions/utils/KR_SBERT_local')"
   ```

7. **Google Cloud setup (for speech-to-text)**

   1. Create project at [Google Cloud Console](https://console.cloud.google.com/)
   2. Activate Speech-to-Text API
   3. Create Service accounts & Download Json key
   4. Set .env file

   ```bash
   # add line at .env
   GOOGLE_APPLICATION_CREDENTIALS=YOUR_PATH/service-account-key(stt-tutor-blah-blah).json
   ```

#### Step 2: Run Django Server

```bash
# Run database migrations
python manage.py makemigrations
python manage.py migrate

# Create superuser (optional)
python manage.py createsuperuser

# Run development server
python manage.py runserver
```

</details>

## Demo 3-(a): Teacher Screen

[Demo Video: Teacher's view - todo: fix link to real video](demo/videos/student_view1.mp4)

---

## Demo 3-(b): Student Screen

[Demo Video: Student screen - todo: fix link to real video](demo/videos/question_generation.mp4)
