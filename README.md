# VoiceTutor

![Logo](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/logo.png)

VoiceTutor is an AI-powered learning application that helps students strengthen their understanding by **speaking out loud**. Through conversational review sessions, adaptive quizzes, and multimodal evaluation (prosody, and reasoning), the app evaluates not just answers but **conceptual understanding**. It is designed for elementary or middle school students, teachers, and parents who want deeper insight into learning progress beyond traditional test scores.

## How It Works

VoiceTutor analyzes spoken responses using multiple signals: linguistic features (filler words, sentence structure, etc), prosodic cues (speech rate, pauses, pitch variation, etc), and semantic coherence. Based on this analysis, the system generates adaptive follow-up questions that target specific learning gaps or reinforce understanding.

For teachers, the platform provides insights into individual and class-wide comprehension patterns, helping identify common misconceptions and track progress beyond simple accuracy metrics. The system maps student performance to the 2022 Korean national curriculum achievement standards, enabling systematic analysis of learning gaps and weak areas by curriculum competency.

## Key Features

- **Multimodal Analysis**: Combines speech recognition, acoustic feature extraction, and semantic analysis to assess understanding
- **Adaptive Questioning**: Generates personalized follow-up questions based on student responses
- **Curriculum Alignment**: Aligns questions with Korean national curriculum standards using AI-powered achievement code inference
- **Progress Tracking**: Provides detailed analytics with meaningful progress indicators for teachers

## Technical Overview

The system uses Google Cloud Speech-to-Text for Korean transcription, extracts acoustic and linguistic features for confidence scoring via XGBoost models, and generates adaptive questions using LLM-powered workflows. The mobile application is built with Jetpack Compose for Android devices.

The platform integrates with Korean national curriculum standards, using a two-stage AI approach (RoBERTa filtering followed by GPT selection) to efficiently align generated questions with achievement codes.

## Getting Started

### Prerequisites

- Android device (API 26+)
- Backend server deployment (Django + PostgreSQL)
- Google Cloud and OpenAI API credentials

### Documentation

- [Design Documentation](<https://github.com/snuhcs-course/swpp-2025-project-team-03/wiki/2)-Design-Documentation>)
- [Requirements & Specifications](<https://github.com/snuhcs-course/swpp-2025-project-team-03/wiki/1)-Requirements-&-Specifications>)

## License

This project was developed as part of the Principles and Practices of Software Development course at Seoul National University (2025).
