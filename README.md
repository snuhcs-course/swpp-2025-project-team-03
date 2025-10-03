# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**.  
Through conversational review sessions, adaptive quizzes, and multimodal analysis (ASR, prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**.  
It is designed for students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## Features

- **Conversational Review**: Short, AI-guided verbal practice sessions with open-ended questions.  
- **Adaptive Quizzes**: Automatically generated follow-up questions tailored to each student’s weak points.  
- **Multimodal Analysis**: Evaluation of content accuracy, reasoning structure, and supportive delivery metrics (speech rate, pauses, intonation).  
- **Instant Feedback**: Real-time hints and corrective prompts during spoken responses.  
- **Progress Tracking**: Visual summaries of concept mastery and learning trends over time.  
- **Cross-Platform Access**: Designed for mobile and web for flexible use in classrooms or at home.  


## Demo 1: Google Cloud Speech-to-Text Demo
[Demo Video](demo/videos/stt_demo.mp4)  

As part of VoiceTutor, we implemented a real-time speech recognition demo using Google Cloud Speech-to-Text API. This module provides word-level timestamps and high accuracy for speech-to-text conversion, designed to be integrated into a learning application for student speech evaluation.

<details>
<summary> Click to expand demo 1 details</summary>

### Implemented Features

- **Voice Recording**: Students can record their speech directly in the web browser
- **Text Transcription**: Converts speech to text with high accuracy
- **Word-level Timestamps**: Provides precise start/end times for each word

### Integration with Actual Application

When integrated into the actual learning application:
1. Students record their speech responses to learning prompts
2. Audio is captured and sent to the STT service
3. Google Cloud API processes the audio and returns text with timestamps
4. Results are sent to the Django backend server
5. The Django server evaluates the student's pronunciation, fluency, and accuracy
6. Feedback is provided to both students and instructors


## Project Structure

```
stt/
├── flask-server/          # Flask backend server
│   ├── app.py            # Main server file
│   ├── requirements.txt  # Python dependencies
│   └── uploads/          # Temporary file storage
├── stt-ui/               # React frontend
│   ├── src/
│   │   ├── App.js        # Main app component
│   │   └── components/
│   │       ├── STTRecorder.js  # Voice recording component
│   │       └── STTRecorder.css # Stylesheet
│   └── package.json      # Node.js dependencies
├── google_stt.py         # Google STT API client
├── google-cloud-setup.md # Google Cloud setup guide
├── start-server.bat      # Server startup script
├── start-ui.bat          # UI startup script
└── stt-project-*.json    # Google Cloud service account key
```

## Setup & Execution Instructions

### Prerequisites

- Python 3.8 or higher
- Node.js 16 or higher
- Google Cloud account with billing enabled
- FFmpeg (optional, for audio conversion)

### Environment Setup

We used the following environment configuration:
- **Operating System**: Windows 10/11
- **Python Version**: 3.8+
- **Node.js Version**: 16+
- **Browser**: Chrome/Edge (for WebRTC support)
- **Google Cloud Project**: stt-project-473514

### Step-by-Step Demo Execution

#### Step 1: Google Cloud Setup

1. **Create Google Cloud Project**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing project
   - Note your project ID

2. **Enable Speech-to-Text API**
   - Navigate to "APIs & Services" > "Library"
   - Search for "Cloud Speech-to-Text API"
   - Click "Enable"

3. **Create Service Account**
   - Go to "IAM & Admin" > "Service Accounts"
   - Click "Create Service Account"
   - Name: `stt-service-account`
   - Role: "Cloud Speech-to-Text Client"
   - Create and download JSON key file

4. **Set Environment Variable**
   ```powershell
   # Windows PowerShell
   $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\your\service-account-key.json"
   
   # Windows Command Prompt
   set GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\your\service-account-key.json
   
   # Linux/Mac
   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-key.json"
   ```

#### Step 2: Backend Server Setup

1. **Navigate to Flask Server Directory**
   ```bash
   cd flask-server
   ```

2. **Create Virtual Environment**
   ```bash
   python -m venv venv
   ```

3. **Activate Virtual Environment**
   ```bash
   # Windows
   venv\Scripts\activate
   
   # Linux/Mac
   source venv/bin/activate
   ```

4. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

#### Step 3: Frontend Setup

1. **Navigate to React UI Directory**
   ```bash
   cd stt-ui
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

#### Step 4: Run the Application

**Option 1: Using Batch Files (Windows)**

1. **Start Backend Server**
   ```bash
   # In first terminal
   ./start-server.bat
   ```

2. **Start Frontend**
   ```bash
   # In second terminal
   ./start-ui.bat
   ```

#### Step 5: Access the Application

- **Web Interface**: http://localhost:3000
- **API Server**: http://localhost:5000

#### Step 6: Test the Demo

1. Open http://localhost:3000 in your browser
2. Click "Start Recording" button
3. Speak clearly into your microphone
4. Click "Stop Recording" button
5. Wait for processing (usually 2-5 seconds)
6. View the transcribed text with word-level timestamps

</details>