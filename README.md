# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal evaluation (prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Automatic Quiz Generation**: Automatically generates quiz questions from PDFs.
- **Conversational Review**: Short, AI-guided verbal review sessions with open-ended questions.
- **Adaptive Quizzes**: Personalized follow-up questions tailored to each student’s weaknesses.

## Demo 4: Teacher Screen & Student Screen

[![demo_team3](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/iteration-4-demo/demo/demo_team3.mp4)](https://github.com/snuhcs-course/swpp-2025-project-team-03/blob/iteration-4-demo/demo/demo_team3.mp4)


### How to Run Demo 4

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

### What Demo 4 Demonstrates

This demo showcases the enhanced teacher and student screens implemented in Iteration 4 of VoiceTutor, focusing on improved user experience, assignment management, and comprehensive reporting features.

#### Core Features

1. **Tutorial System**
   - **Onboarding Tutorial**: When users sign up or reset the tutorial from settings, an interactive tutorial is displayed
   - **Role-specific Tutorials**: Different tutorials for teachers and students guide users through their respective interfaces
   - **Swipe Navigation**: Users can navigate through tutorial pages by swiping horizontally
   - **Skip Option**: Users can skip the tutorial at any time or complete it by swiping right on the last page

2. **Student Features**

   - **Assignment Management**
     - Students can view available assignments in their class dashboard
     - Students can start working on assignments or skip them as needed
     - Flexible assignment navigation allows students to proceed at their own pace
   
   - **Assignment Reports**
     - Students can view detailed reports for completed assignments
     - Reports show performance metrics and feedback on their answers

3. **Teacher Features**

   - **Assignment Creation**
     - Teachers can create new assignments by uploading PDF files
     - Automatic quiz generation from PDF content
     - **Cancellation Support**: If a teacher selects the wrong PDF, they can cancel the assignment creation process even while it's in progress
   
   - **Class Management**
     - Teachers can view all classes they manage
     - **Student Enrollment**: Teachers can add students to their classes from the class management screen
     - Teachers can view and manage class details
   
   - **Assignment Management**
     - Teachers can view all assignments in their classes
     - **Assignment Editing**: Teachers can edit assignment details and content
     - Teachers can view assignment content and details
   
   - **Comprehensive Reporting**
     - **Assignment Reports**: Teachers can view detailed reports for each assignment
     - **Student Achievement Reports**: Teachers can access per-student achievement standard reports
     - **Weakness Analysis**: The system analyzes and displays student weakness types, helping teachers identify areas that need attention
     - Visual analytics help teachers understand student performance patterns

#### Goals Achieved

- Improved user onboarding experience with interactive tutorials
- Enhanced assignment management with cancellation support
- Comprehensive reporting system for both teachers and students
- Better class and student management interface
- Data-driven insights through achievement standard reports and weakness analysis
- Flexible assignment navigation for students

---
