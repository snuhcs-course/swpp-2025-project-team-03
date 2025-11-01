# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal evaluation (prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Automatic Quiz Generation**: Automatically generates quiz questions from PDFs.
- **Conversational Review**: Short, AI-guided verbal review sessions with open-ended questions.
- **Adaptive Quizzes**: Personalized follow-up questions tailored to each student’s weaknesses.

## Demo 3: Teacher Screen & Student Screen

<details>
<summary> Click to expand how to run demo 3</summary>


Although the actual deployment will use a remote server, relying on it during the Iteration 3 demo may cause compatibility issues if the backend server is updated afterward. To ensure consistent behavior between the current Iteration 3 backend and frontend, we recommend running the server locally using the Android Studio emulator.
Please follow the local setup instructions described below.
<details>
<summary> Click to expand how to run demo 3 on a physical device</summary>
If you want to test on a physical device, you may connect to the remote server. However, please note that this may lead to mismatches in features or API responses due to potential backend changes after the Iteration 3 code freeze.

To force the app to use the local server even on a physical device, update the following line in
frontend/app/src/main/java/com/example/voicetutor/data/network/ApiConfig.kt (around line 30):
```
        return prefs.getString(KEY_BASE_URL, PROD_URL) ?: PROD_URL
```
Then rebuild and run the app on your device.
</details>

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

# Create initial sample data for testing (optional)
python manage.py create_all

# Run development server
python manage.py runserver
```

#### Step 3: Run Android Studio

1. **Open the "Extended Controls" window of your selected virtual device.**

2. **Go to "Microphone" and enable "Virtual microphone uses host audio input"**

This allows the emulator to capture real voice input for testing. 

Now run the VoiceTutor app on the emulator, and it should operate properly.
</details>

## Demo 3-(a): Teacher Screen

[Demo Video: Teacher's view - todo: fix link to real video](demo/videos/student_view1.mp4)

---

## Demo 3-(b): Student Screen

[Demo Video: Student screen - todo: fix link to real video](demo/videos/question_generation.mp4)
