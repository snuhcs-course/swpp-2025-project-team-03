# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal evaluation (prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Automatic Quiz Generation**: Automatically generates quiz questions from PDFs.
- **Conversational Review**: Short, AI-guided verbal review sessions with open-ended questions.
- **Adaptive Quizzes**: Personalized follow-up questions tailored to each student’s weaknesses.

## Demo 5: App functionality & Overall Stability

[![demo_team3](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/iteration-4-demo/demo/demo_team3.mp4)](https://github.com/snuhcs-course/swpp-2025-project-team-03/blob/iteration-4-demo/demo/demo_team3.mp4)

### How to Run Demo 5

#### Environment Requirements

- **Backend**: Django (Python 3.8+)
- **Frontend**: Android (Kotlin, Jetpack Compose)
- **Database**: SQLite (development) / PostgreSQL (production)
- **External Services**: OpenAI API, AWS S3, Google Cloud Speech-to-Text API

#### Step 1: Backend Setup

**Note**: The backend is currently running on Team 3's remote server. You can use the app directly without local backend setup.

<details>
<summary>Click to expand if you want to set up backend locally</summary>

<br>

1. **Poppler Installation**

   Please install Poppler for your OS and make sure its bin folder is added to your system PATH.

   - Windows: Install Poppler (prebuilt binaries) and add bin to PATH
   - macOS: `brew install poppler` (ensure PATH is updated)
   - Linux: Install poppler-utils via your package manager

   Verify by running `pdfinfo` in a new terminal.

2. **Navigate to Backend Directory**

   ```bash
   cd backend
   ```

3. **Create .env File**

   Create your S3 bucket first, and make **.env** at **backend/** directory like below.

   ```bash
   OPENAI_API_KEY='YOUR_OPENAI_API_KEY'
   SECRET_KEY='DJANGO_SECRET_KEY'

   AWS_ACCESS_KEY_ID='YOUR_AWS_ACCESS_KEY_ID'
   AWS_SECRET_ACCESS_KEY='YOUR_AWS_SECRET_ACCESS_KEY'
   AWS_REGION=ap-northeast-2
   AWS_STORAGE_BUCKET_NAME='YOUR_S3_BUCKET_NAME'
   GOOGLE_APPLICATION_CREDENTIALS='YOUR_PATH/service-account-key.json'
   ```

   **Create secret key (Optional)**
   If you want to create secret key for Django, run the command below to generate a key.

   ```bash
   python -c "from django.core.management.utils import get_random_secret_key; print(get_random_secret_key())"
   ```

4. **Google Cloud Setup (for speech-to-text)**

   1. Create project at [Google Cloud Console](https://console.cloud.google.com/)
   2. Activate Speech-to-Text API
   3. Create Service accounts & Download Json key
   4. Set `GOOGLE_APPLICATION_CREDENTIALS` in .env file

5. **Create Virtual Environment**

   ```bash
   python -m venv venv
   ```

6. **Activate Virtual Environment**

   ```bash
   # Windows
   venv\Scripts\activate

   # Linux/Mac
   source venv/bin/activate
   ```

7. **Install Dependencies**

   ```bash
   pip install -r requirements-local.txt
   ```

8. **SentenceTransformer Model Setup**

   ```bash
   # Download model for semantic feature extraction
   python -c "from sentence_transformers import SentenceTransformer; model = SentenceTransformer('snunlp/KR-SBERT-V40K-klueNLI-augSTS'); model.save('submissions/utils/KR_SBERT_local')"
   ```

9. **Run Django Server**

   ```bash
   # Run database migrations
   python manage.py makemigrations
   python manage.py migrate

   # Create superuser (optional)
   python manage.py createsuperuser

   # Create initial sample data for testing (recommended)
   python manage.py create_all

   # Run development server
   python manage.py runserver
   ```

</details>

#### Step 2: Run Android App

1. **Open Android Studio** and open the `frontend` directory

2. **For Emulator Testing**:

   - Open the "Extended Controls" window of your selected virtual device
   - Go to "Microphone" and enable "Virtual microphone uses host audio input"
   - This allows the emulator to capture real voice input for testing

3. **Build and Run** the app on your device or emulator

4. **Configure API Endpoint** (if needed):
   - Ensure the app is configured to connect to `http://10.0.2.2:8000` for emulator
   - For physical device, use your computer's local IP address

---

### What Demo 5 Demonstrates

This demo verifies the overall functionality and stability of the VoiceTutor app as of Iteration 5.  
Unlike Demo 4, which focused on showcasing newly added features, Demo 5 emphasizes end-to-end reliability, user experience under adverse conditions (e.g., network errors), and consistent performance across all core user flows.

Particular attention is given to **graceful degradation**: when the network is unstable or fails, users are informed with helpful alerts instead of facing silent failures or crashes. The goal is to ensure that all critical features work as expected — without bugs, exceptions, or incomplete transitions — under both normal and degraded conditions.

---

### Key Features

These features represent core workflows of the app and serve as **practical validation criteria** to evaluate whether the system reliably fulfills its intended purpose for both teacher and student users.

- **Class Creation & Student Enrollment**  
  Teachers can create new classes by specifying the class title, subject, and optional description. Students can then be enrolled into these classes for organized assignment and report management.

- **PDF-Based Quiz Generation**  
  Teachers can upload PDF learning materials, from which the system automatically generates quiz assignments. This enables a seamless transition from curriculum documents to interactive assessments.

- **Voice-Based Assignment Participation**  
  Students participate in conversational quiz sessions by recording spoken answers. The system captures and processes their responses, enabling dynamic follow-up questions based on identified weaknesses.

- **Assignment Review & Feedback**  
  After completing an assignment, students can review detailed reports that highlight correctness, provide explanations, and suggest areas for improvement.

- **Curriculum-Aligned Analytics for Teachers**  
  Teachers can view per-student reports and performance breakdowns aligned with the Korean national curriculum standards. Visual analytics and weakness detection help inform targeted instruction and remediation.

---

### Goals Achieved

- Verified all major user flows (sign-up, class management, assignment generation, voice submission, reporting) execute without errors or inconsistencies
- Improved user experience through network-aware error handling and user-friendly feedback messages
- Ensured that both teacher and student interfaces respond gracefully under unstable or offline conditions
- Confirmed that no critical bugs or regressions exist in high-priority areas of the app
- Demonstrated full-stack robustness of the system across roles, platforms, and interaction types
