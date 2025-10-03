# swpp-2025-project-team-03

Temporary development rule file

---

# Frontend

## 1. Initial Setup

---

# Backend

## 1. Initial Setup

Setting up django development environment

### 1. Create and Activate Virtual Environment (venv)

```bash
# Navigate to the project backend directory
cd backend

# Create virtual environment
python -m venv .venv
python3 -m venv .venv # on macOS

# Activate virtual environment
# On macOS/Linux:
source .venv/bin/activate

# On Windows:
# On cmd terminal
.venv\Scripts\activate.bat

# On git bash
source .venv/Scripts/activate
```

### 2. Install Dependencies from requirements.txt

```bash
# Make sure virtual environment is activated
pip install --upgrade pip
pip install -r requirements.txt
```

### 3. Create .env File and Configure Environment Variables

```bash
# Create .env file
touch .env
```

Add environment variables to `.env` file:

```env
# Django Settings
SECRET_KEY=your-secret-key-here

# API Keys (if needed)
# OPENAI_API_KEY=your-openai-api-key
# GOOGLE_API_KEY=your-google-api-key
```

If you want to create secret key for Django, run the command below to generate a key.

```bash
.venv/bin/python -c 'from django.core.management.utils import get_random_secret_key; print(get_random_secret_key())'
```

### 4. Enable Pre-commit Configuration

```bash
# Install pre-commit
pip install pre-commit

# Install pre-commit hooks
pre-commit install
```

### 5. Django Setup

```bash
# Navigate to backend directory
cd backend

# Run database migrations
python manage.py makemigrations
python manage.py migrate

# Create superuser (optional)
python manage.py createsuperuser

# Run development server
python manage.py runserver
```

# Research

## 1. Initial Setup

### 1. Create and Activate Virtual Environment (venv)

```bash
# Navigate to the project backend directory
cd research

# Create virtual environment
python -m venv .venv
python3 -m venv .venv # on macOS

# Activate virtual environment
# On macOS/Linux:
source .venv/bin/activate

# On Windows:
# On cmd terminal
.venv\Scripts\activate.bat

# On git bash
source .venv/Scripts/activate
```

### 2. Install Dependencies from requirements.txt

```bash
# Make sure virtual environment is activated
pip install --upgrade pip
pip install -r requirements.txt
```

## 2. Run

Prepare sample wav file and run **extract_acoustic_features.py** to extract acoustic features  
Run **extract_semantic_features.py** to extract semantic features  
Note that **research/data** directory is included in **.gitignore**

Download public speech dataset (see **research/dataset/README.md**)  
dataset folder should contain 'train' and 'valid' folder  
Run **train.py** to fit

```bash
python extract_acoustic_features.py {wav_file_path}
python extract_semantic_features.py {json_file_path}
python train.py {dataset_folder}
```
