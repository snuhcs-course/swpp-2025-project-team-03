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

## Demo 1-(a): Speech to Text

[Demo Video](demo/videos/stt_demo.mp4)

As part of VoiceTutor, we implemented a real-time speech recognition demo using Google Cloud Speech-to-Text API. This module provides word-level timestamps and high accuracy for speech-to-text conversion, designed to be integrated into a learning application for student speech evaluation.

<details>
<summary> Click to expand demo 1-(a) details</summary>

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
   cd research/stt/flask-server
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

## Demo 1-(b): feature extraction / naive evaluation model

[Feature Extraction Demo Video](demo/videos/preprocess_dataset.mp4)  
[Train Demo Video](demo/videos/train_basic.mp4)

As part of VoiceTutor, we implemented an acoustic/semantic feature extractor. This module provides useful features to evaluate student's speech.

<details>
<summary>Examples of features</summary>

### 🔈 Silence-related features

- **`total_silence_sec`** — total duration of silence (in seconds)
- **`percent_silence`** — ratio of silence to total utterance duration (%)

---

### 🎵 Pitch (f0) features

- **`min_f0_hz`** — minimum fundamental frequency (Hz)
- **`max_f0_hz`** — maximum fundamental frequency (Hz)
- **`range_f0_hz`** — overall f0 range (Hz)
- **`tot_slope_f0_st_per_s`** — average f0 slope over the voiced frames across the entire utterance (semitone/s)
- **`end_slope_f0_st_per_s`** — f0 slope in the ending segment (semitone/s)

---

### 💬 Semantic adjacency similarity

_(local coherence between consecutive sentences)_

- **`sem_adj_sim_mean`** — mean semantic similarity between adjacent sentences
- **`sem_adj_sim_std`** — standard deviation of adjacent sentence similarity
- **`sem_adj_sim_p10`** — 10th percentile of adjacent similarity
- **`sem_adj_sim_p50`** — median (50th percentile) of adjacent similarity
- **`sem_adj_sim_p90`** — 90th percentile of adjacent similarity
- **`sem_adj_sim_frac_high`** — fraction of sentence pairs with high similarity (≥ high_thr)
- **`sem_adj_sim_frac_low`** — fraction of sentence pairs with low similarity (≤ low_thr)

---

### 🧭 Topic transition and overall coherence

- **`sem_topic_path_len`** — cumulative semantic distance between adjacent sentences (higher → more topic shifts)
- **`sem_dist_to_centroid_mean`** — mean distance from document centroid (semantic dispersion)
- **`sem_dist_to_centroid_std`** — standard deviation of centroid distances
- **`sem_coherence_score`** — overall semantic cohesion score _(1 − mean centroid distance)_

---

### 🧩 Section-level cohesion and diversity

- **`sem_intra_coh`** — mean intra-section cohesion (front/middle/end segments)
- **`sem_inter_div`** — inter-section diversity of topic centroids (higher → more distinct sections)

</details>
<br />

For training purpose, we've measured those features to [Public Speech Dataset](https://www.aihub.or.kr/aihubdata/data/view.do?pageIndex=1&currMenu=115&topMenu=100&srchOneDataTy=DATA004&srchOptnCnd=OPTNCND001&searchKeyword=&srchDetailCnd=DETAILCND001&srchOrder=ORDER001&srchPagePer=20&srchDataRealmCode=REALM002&srchDataRealmCode=REALM010&aihubDataSe=data&dataSetSn=71663), and then run simple random forest algorithm to see if it fits.

<details>
<summary> Click to expand demo 1-(b) details</summary>

#### Step 1: Research Requirements Setup

1. **Navigate to Research Directory**

   ```bash
   cd research
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

#### Step 2: Run feature extractor with demo wav

Note that these command simply print out the features without changing or creating annotation files.

```bash
python extract_acoustic_features.py demo_data/certain.wav
python extract_acoustic_features.py demo_data/uncertain.wav

python extract_semantic_features.py demo_data/certain.json
python extract_semantic_features.py demo_data/uncertain.json
```

#### Step 3: Preprocess Speech Dataset (Optional)

With **research/dataset/\*.py**, you may preprocess speech dataset.  
[Sample Dataset](https://drive.google.com/file/d/1iViLvPPGB-bHA1GBeMPprkR_hkAQCHJY/view?usp=sharing) is a **very** small dataset sampled from [Public Speech Dataset](https://www.aihub.or.kr/aihubdata/data/view.do?pageIndex=1&currMenu=115&topMenu=100&srchOneDataTy=DATA004&srchOptnCnd=OPTNCND001&searchKeyword=&srchDetailCnd=DETAILCND001&srchOrder=ORDER001&srchPagePer=20&srchDataRealmCode=REALM002&srchDataRealmCode=REALM010&aihubDataSe=data&dataSetSn=71663). (for testing purpose)

You can preprocess dataset using commands like below.

```bash
# Command Examples
cd dataset

python mp4_to_wav.py --input_root "sample_dataset/train"
python label_formatter.py --input_root "sample_dataset/train/label"
python add_acoustic_features.py --input_root "sample_dataset/train"
python add_semantic_features.py --input_root "sample_dataset/train/label"

python mp4_to_wav.py --input_root "sample_dataset/valid"
python label_formatter.py --input_root "sample_dataset/valid/label"
python add_acoustic_features.py --input_root "sample_dataset/valid"
python add_semantic_features.py --input_root "sample_dataset/valid/label"
```

#### Step 4: Train with Public Speech Dataset

[Preprocessed Public Speech Dataset](https://drive.google.com/file/d/1tu9G6k25s6N6me_8KZ6Mw-57WU2Ri_AT/view?usp=sharing) is a dataset annotated with acoustic features. You can train a model on this dataset using the command below.

```bash
python train.py --dataset_path {your_dataset_path}
```

</details>
