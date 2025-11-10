## 0. Table of Contents

- [1. Document Revision History](#1-document-revision-history)
- [2. System Design](#2-system-design)
  - [2.1 System Architecture](#21-system-architecture)
  - [2.2 Class Diagrams & Data Models](#22-class-diagrams--data-models)
- [3. Implementation Details](#3-implementation-details)
  - [3.1 Backend API (ver.2)](#32-backend-api-ver2)
    - [Auth API](#auth-api)
    - [Assignment API](#assignment-api)
    - [Quiz API](#quiz-api)
    - [Personal Assignment API](#personal-assignment-api)
    - [Student API](#student-api)
    - [Class API](#class-api)
    - [Message API](#message-api)
    - [Subject API](#subject-api)
    - [Progress Report API](#progress-report-api)
    - [Error Codes](#error-codes)
- [4. Testing Plan (Iteration 3+)](#4-testing-plan-iteration-3)
  - [4.1 Unit Testing & Integration Testing](#41-unit-testing--integration-testing)
  - [4.2 Acceptance Testing](#42-acceptance-testing)
- [5. External Libraries](#5-external-libraries)

## 1. Document Revision History
| Version | Date       | Message              |
|---------|------------|----------------------|
| 0.1     | 2025-10-02 | Initial draft created |
| 1.1     | 2025-10-14 | Update api specification |
| 1.2     | 2025-10-14 | Add Table of Contents |
| 1.3     | 2025-10-16 | Update Testing Plan & ERD|
| 1.4     | 2025-10-16 | Update Frontend class diagram|
| 2.1     | 2025-10-23 | Update Testing Plan|
| 2.2     | 2025-10-30 | Update API specification(ver.2) | 
| 2.3     | 2025-10-30 | Update ERD & Data models|
| 2.4     | 2025-10-30 | Update architecture diagram |
| 2.5     | 2025=11-01 | Update Iter3 final ERD & API specification(ver.2) |
| 2.6     | 2025-11-02 | Update Testing Plan & Results |
| 2.7     | 2025-11-09 | Added detailed architecture, data flow, and DevOps specifications |

---

## 2. System Design
### 2.1 System Architecture
- **Frontend**: Jetpack Compose + Retrofit
- **Backend**: Django + MySQL  
- **AI API**: ChatGPT API & Google Cloud STT API
- **Pattern**: Client–Server with API integration  

#### Runtime Components

| Layer | Component | Responsibility | Tech Stack | Observability |
|-------|-----------|----------------|------------|---------------|
| Presentation | Android App (Jetpack Compose) | Render dashboards, offline caching, microphone capture, local analytics | Kotlin, Compose, Hilt, Room | Firebase Crashlytics, Timber structured logs |
| API Gateway | Nginx + Gunicorn | TLS termination, rate limiting, static asset caching | Nginx, Gunicorn | Prometheus exporter, request tracing headers |
| Application | Django services (`accounts`, `courses`, `assignments`, `questions`, `reports`, `submissions`, `feedbacks`) | Role-based access, orchestration of AI pipelines, PDF parsing, quiz lifecycle | Python 3.12, Django REST Framework, Celery | Sentry, OpenTelemetry traces, health-check endpoints |
| Data | PostgreSQL 15 | Persist transactional data (assignments, enrollments, submissions) with timezone-aware timestamps | PostgreSQL, psycopg3 | pg_stat_statements, slow-query logging |
| AI Worker | In-process LLM calls (synchronous) | STT processing, prompt generation, evaluation scoring, tail-question generation executed inside request cycle | LangChain, LangGraph, OpenAI GPT-4o-mini, Google Cloud STT | Application logging (Django logger), latency sampling in API logs |
| Storage | AWS S3 | Store PDFs, audio artifacts, generated reports | S3 Intelligent-Tiering | Object access logs, lifecycle policies |

#### Deployment Topology
- **Environments**: Local development (Django runserver) and a single Linux VM deployment; staging/production separation is pending.
- **Network**: Application and database run in the same private network/VPC; ingress handled directly by Gunicorn behind Nginx.
- **Secrets Management**: Environment variables via `.env`; migration to a managed secrets store is on the roadmap.
- **CI/CD**: GitHub Actions for lint/test; deploys triggered manually over SSH/rsync (no automated blue/green yet).

#### Data Flow Overview
1. Teacher uploads PDF → file stored temporarily in S3 pre-signed location → background job extracts text, chunks semantic sections, and generates AI summary/questions.
2. Students launch quiz → mobile app fetches assignment metadata and preloads question queue from `/personal_assignments/next`.
3. Student records response → audio chunk encrypted and uploaded → STT job transcribes (Google Cloud) → Django processes transcript, prompts GPT for evaluation and tail question.
4. Results stored in `submissions` with correctness, certainty, timestamps → push notifications issued via Firebase.
5. Teacher dashboards query aggregated statistics on demand via Django ORM (no materialized views yet); heavy aggregations are memoized per request.

#### Adaptive Feedback Pipeline
1. **Feature Extraction (`extract_all_features`)**: Runs Google STT, prosodic analysis (`extract_acoustic_features`), and semantic coherence metrics (`extract_features_from_script`). Acoustic cues (pause ratios, f0 slope, silence %) and semantic embeddings feed downstream models, embodying Hasan et al.’s affect perception requirements.
2. **Confidence Inference (`run_inference`)**: XGBoost regression produces a continuous certainty score (1–8) and letter grade from the multimodal feature vector, following Pelánek & Jarušek’s guidance to combine behavioral and linguistic signals.
3. **Semantic Planner (`planner_node`)**: A temperature-0 GPT-4o-mini call with minimal JSON prompt returns `{"is_correct": true|false}` based solely on meaning equivalence between model answer and transcript, tolerant of ASR artifacts.
4. **Rule-based Routing (`decide_bucket_confidence`)**: Correctness × confidence yields buckets A–D with configurable high/low threshold (default 3.45). This enforces the four-quadrant scaffold aligned with Abar et al.’s ZPD scaffolding findings.
5. **Strategy Selection (`decide_plan`)**: Adjusts follow-up frequency by bucket and `recalled_time`; high-performing learners can graduate to correctness-only responses after repeat success.
6. **Tail Question Actor (`actor_node`)**: Bucket-specific strategy strings plus few-shot exemplars guide GPT to emit concise Korean JSON payloads (topic, question, model answer, explanation, difficulty). Sanitizers enforce JSON validity and strip LaTeX/backslash artifacts for real-time use.
7. **State Graph Orchestration (`langgraph.StateGraph`)**: Planner → derive → actor/only_correct nodes compiled once and invoked via `generate_tail_question()`, enabling deterministic flows and granular unit tests.

| Bucket | Planner Verdict | Confidence Range | Strategy Focus | Actor Difficulty |
|--------|-----------------|------------------|----------------|------------------|
| A | Correct | ≥ high_thr | Enrichment / cross-concept transfer | hard |
| B | Correct | < high_thr | Reinforcement & confidence building | medium |
| C | Incorrect | ≥ high_thr | Misconception diagnosis & correction | medium |
| D | Incorrect | < high_thr | Foundational scaffolding & guided recall | easy |

#### External Integrations
- **OpenAI**: GPT-4o-mini/gpt-4o for quiz generation, scoring rubric alignment, and tail question creation; invoked synchronously per answer submission.
- **Google Cloud STT**: Speech-to-text transcription (16kHz mono) for uploaded WAV files using the `latest_long` model with word confidence.
- **AWS S3**: Storage for assignment materials and audio artifacts via presigned URL uploads/checks.

<img src="https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/refs/heads/main/docs/images/architecture_diagram_iter3.png" width="800">

---

### 2.2 Class Diagrams & Data Models
- **Frontend**: Show class diagram (BookListPage, BookViewerPage, SummaryPage, QuizPage, SettingsPage).
![Class Diagram Front](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/class%20diagram_front.png)
- **Backend**: Show service/entity structure.
![Class Diagram Back](https://github.com/snuhcs-course/swpp-2025-project-team-03/blob/main/docs/images/class%20diagram_backend.png)
- **Database**
![ERD_prototype](https://github.com/snuhcs-course/swpp-2025-project-team-03/blob/main/docs/images/ERD_iter3_final.png)
The following diagram illustrates an Entity Relationship Diagram (ERD) outlining the Django models for our tutoring and AI quiz service. Each entity corresponds to a key feature in the platform, and the relationships define how classes, assignments, and personalized learning are connected. Accounts are distinguished by role (student or teacher), and students register for classes through enrollments. Each class contains a foreign key to the subject. And assignments are created under these classes with structured deadlines and question sets. Materials such as PDFs can be attached to assignments for learning support. Students receive personalized assignments that track progress and completion, while individual questions include explanations, and model answers. Answers are stored with correctness and grading information, enabling both automatic evaluation and tail questions. Overall, schema enhances personalized learning by supporting classroom management, customized tasks, AI-based scoring, and continuous feedback.

#### Data Dictionary Highlights

| Table | Purpose | Key Columns | Notes |
|-------|---------|-------------|-------|
| `accounts_user` | Stores teacher & student identities | `id`, `email`, `role`, `display_name`, `locale`, `is_active` | Email unique; soft-delete tracked via `is_active` |
| `courses_courseclass` | Represents a class/cohort | `id`, `teacher_id`, `subject_id`, `name`, `description`, `start_date`, `end_date`, `created_at` | Student count computed via ORM (no stored trigger) |
| `assignments_assignment` | Assignment metadata | `id`, `course_class_id`, `subject_id`, `title`, `description`, `total_questions`, `visible_from`, `due_at`, `grade`, `created_at` | S3 materials stored via related `assignments_material` |
| `questions_question` | Canonical question bank entry | `id`, `assignment_id`, `content`, `answer`, `difficulty`, `curriculum_code` | Supports multilingual content using translation table |
| `personal_assignments_personalassignment` | Assignment per student | `id`, `assignment_id`, `student_id`, `status`, `solved_num`, `started_at`, `submitted_at`, `created_at` | Unique constraint (`student`, `assignment`) |
| `submissions_submission` | Individual answer attempt (Answer model) | `id`, `question_id`, `student_id`, `text_answer`, `state`, `eval_grade`, `started_at`, `submitted_at`, `created_at` | Correctness + confidence stored on each answer |
| `feedbacks_message` | Teacher ↔ student communications | `id`, `teacher_id`, `class_id`, `student_id`, `content`, `channel`, `delivered_at` | Channel enum: `IN_APP`, `EMAIL`, `PUSH` |

> Reports are generated on demand through `reports.utils.analyze_achievement.parse_curriculum`; no dedicated progress-report table is persisted yet.

#### Domain Events
Currently there is no external event bus. Key state transitions occur inside the API layer:
- `AssignmentPublished`: creates personal assignments for enrolled students and issues S3 upload keys.
- `SubmissionEvaluated`: updates personal-assignment status and creates tail questions when needed.
- `ReportRequested`: invokes curriculum analysis synchronously and returns the response.

---

## 3. Implementation Details
### 3.1 Backend API Overview

All endpoints are served under /api/ and return the standard envelope { "success": bool, "data": ?? "message": str | null, "error": str | null }.

#### Core
| Method | Path | Description |
| --- | --- | --- |
| GET | /core/health/ | Health check endpoint |
| GET | /core/error/ | Deliberately raises an error (debug/testing) |
| GET | /core/logs/tail | Return the last 
 lines of 
ohup.out (requires 
 query parameter) |

#### Authentication
| Method | Path | Description |
| --- | --- | --- |
| POST | /auth/signup/ | Create a new teacher or student account and issue JWT pair |
| POST | /auth/login/ | Authenticate and obtain access/refresh tokens |
| POST | /auth/logout/ | Client-side token invalidation endpoint |

#### Assignments
| Method | Path | Description |
| --- | --- | --- |
| GET | /assignments/ | List assignments (supports 	eacherId, classId, status filters) |
| POST | /assignments/create/ | Create assignment and return S3 presigned upload URL |
| GET | /assignments/{id}/ | Retrieve assignment detail (includes materials) |
| PUT | /assignments/{id}/ | Update assignment metadata/subject |
| DELETE | /assignments/{id}/ | Remove assignment |
| POST | /assignments/{id}/submit/ | Placeholder for manual submission workflow (current stub) |
| GET | /assignments/{id}/questions/ | List generated/base questions |
| GET | /assignments/{id}/results/ | Personal-assignment completion summary |
| GET | /assignments/{assignment_id}/s3-check/ | Validate uploaded PDF in S3 |
| GET | /assignments/teacher-dashboard-stats/ | Aggregate counts for teacher dashboard |

#### Questions
| Method | Path | Description |
| --- | --- | --- |
| POST | /questions/create/ | Generate base questions/summary from uploaded material |

#### Courses ??Students
| Method | Path | Description |
| --- | --- | --- |
| GET | /courses/students/ | List students (filter by 	eacherId or classId) |
| GET | /courses/students/{id}/ | Student detail with enrolments |
| PUT | /courses/students/{id}/ | Update student name/email |
| DELETE | /courses/students/{id}/ | Delete student (requires reason body) |
| GET | /courses/students/{id}/statistics/ | Assignment progress counters for the student |

#### Courses ??Classes
| Method | Path | Description |
| --- | --- | --- |
| GET | /courses/classes/ | List classes (optional 	eacherId filter) |
| POST | /courses/classes/ | Create class and link subject |
| GET | /courses/classes/{id}/ | Class detail |
| PUT | /courses/classes/{id}/ | Update class metadata |
| DELETE | /courses/classes/{id}/ | Delete class |
| GET | /courses/classes/{id}/students/ | List enrolled students |
| PUT | /courses/classes/{id}/students/ | Enrol student by id/name/email |
| GET | /courses/classes/{classId}/students-statistics/ | Per-student completion/accuracy for class |
| GET | /courses/classes/{id}/completion-rate/ | Overall completion rate for class |

#### Personal Assignments & Submissions
| Method | Path | Description |
| --- | --- | --- |
| GET | /personal_assignments/ | List personal assignments (student_id or ssignment_id required) |
| GET | /personal_assignments/{id}/questions/ | Retrieve base + tail questions for the assignment |
| GET | /personal_assignments/{id}/statistics/ | Aggregated stats for a personal assignment |
| POST | /personal_assignments/{id}/complete/ | Mark personal assignment as submitted |
| POST | /personal_assignments/answer/ | Upload WAV answer (multipart) and trigger evaluation |
| GET | /personal_assignments/answer/ | Fetch next question (personal_assignment_id query) |
| GET | /personal_assignments/{id}/correctness/ | List answered questions with correctness/explanations |
| GET | /personal_assignments/recentanswer/ | Fetch most recent in-progress personal assignment for a student |

#### Feedbacks & Messaging
| Method | Path | Description |
| --- | --- | --- |
| POST | /feedbacks/messages/send/ | Teacher sends message to class/student |
| GET | /feedbacks/messages/ | Messages for current user (userId, limit optional) |
| GET | /feedbacks/messages/{classId}/ | Class-specific message history |
| GET | /feedbacks/dashboard/stats/ | Teacher dashboard statistics |
| GET | /feedbacks/dashboard/recent-activities/ | Latest activity feed |
| POST | /feedbacks/analysis/student/ | Placeholder analytics endpoint |
| POST | /feedbacks/analysis/class/ | Placeholder analytics endpoint |
| POST | /feedbacks/analysis/subject/ | Placeholder analytics endpoint |
| GET | /feedbacks/reports/progress/ | Placeholder progress report (accepts 	eacherId, classId, period) |

#### Reports & Catalog
| Method | Path | Description |
| --- | --- | --- |
| GET | /reports/{class_id}/{student_id}/ | Curriculum analysis report generated on demand |
| GET | /catalog/subjects/ | List available subjects |

#### Tail Question Service (Implementation Notes)
- Feature extraction (submissions/utils/feature_extractor/*) converts audio to transcripts plus acoustic/semantic features.
- Confidence scoring (submissions/utils/inference.py) uses XGBoost to predict certainty (1??).
- Tail-question generation (submissions/utils/tail_question_generator/generate_questions_routed.py) routes planner verdicts through bucket strategies with LangGraph + GPT-4o-mini.
- submissions/views.AnswerSubmitView orchestrates the flow synchronously; tests mock STT/LLM for deterministic coverage.

### 3.2 Frontend Architecture & State Management
### 3.2 Frontend Architecture & State Management
- **Layering**: MVVM with `ViewModel` mediating between UI Composables and repository layer. Repository abstracts Retrofit services, Room cache, and offline-first sync orchestration.
- **Navigation**: `NavHost` with nested graphs for student vs teacher flows; deep links support class invitation acceptance.
- **State Handling**: `UiState` sealed classes capture loading/error/success; Compose `rememberSaveable` preserves quiz progress through configuration changes.
- **Offline Strategy**: Room database caches assignments, quizzes, and pending submissions. Pending audio uploads persisted in `UploadQueue` table with exponential backoff worker.
- **Dependency Injection**: Hilt modules for API clients, repositories, use-cases. Testing leverages `@TestInstallIn` to swap fake implementations.
- **Analytics Hooks**: Events dispatched via shared `AnalyticsManager` (Firebase + Amplitude) with structured payloads (screen, action, metadata).

### 3.3 DevOps, Observability & Governance
- **Infrastructure**: Single Linux VM running Nginx + Gunicorn + PostgreSQL; deployment scripted via shell/rsync. Terraform migration planned.
- **Monitoring**: Basic health checks + Django request logging; Grafana/Prometheus integration is backlog.
- **Logging**: Structured logs to stdout (captured by journald); sensitive fields scrubbed manually in log statements.
- **Security Controls**: HTTPS termination with Nginx, JWT auth, S3 bucket policies enforcing server-side encryption; WAF/IAM hardening planned.
- **Backup & Recovery**: PgDump nightly backup, S3 artifact storage; disaster-recovery runbook under preparation.
- **Compliance**: Consent/version tracking in `accounts`; DPIA and automated SAR tooling tracked in compliance roadmap.

---

### 3.4 Frontend–Backend Integration Status
- **Implemented & in use**: Authentication, assignment CRUD + S3 presign, personal assignment answer flow (`/personal_assignments/answer/`), tail-question retrieval, class/student listings, dashboard stats, curriculum analysis stub (`/reports/<class_id>/<student_id>/`).
- **Client endpoints without server support (needs implementation or removal)**:
  - `GET /courses/students/{id}/assignments/`: Retrofit interface exists but Django has no matching view.
  - `POST /assignments/{id}/submit/` and `AssignmentSubmissionRequest`/`QuestionFeedback` DTOs: backend currently relies on personal-assignment pipeline instead.
  - Extended progress report filters (`teacherId`, `period`) passed to `/reports/progress/`: server stub ignores query params and returns placeholder payload.
  - Pronunciation/confidence fields expected by `QuestionFeedback` are not provided by backend responses.
- **Action**: keep this compatibility table updated and prioritize either removing unused Retrofit calls or adding matching Django endpoints to prevent 404s in production.

---

## 4. Testing Plan (Iteration 3+)
- We use pre-commit to automatically enforce code quality before each commit.
### 4.1 Unit Testing & Integration Testing

**Schedule & Frequency**
- **When**: 
    - Conducted continuously during feature development.
    - Each developer must run unit tests before creating a PR to develop.
    - Integration tests are executed every weekend.
- **Frequency**:
    - Unit Tests → Daily (local development)
    - Integration Tests → Weekly (managed by PM)

**Responsibilities**
- **Developers**:
    - Write and maintain unit tests for their assigned modules.
    - Ensure tests pass before opening PRs.
- **PM**:
    - Reviews PRs and enforces testing compliance.
    - Execute weekly integration tests.

#### 4.1.1 Backend Testing
- **Frameworks**: Pytest
- **Architecture** : MTV, but templates are not used on backend (API-only).
- **Coverage goal**: > 90% coverage per component.  

- Testing Result
  - Passed 532 tests and achieved total 92% total coverage.
  - Per component coverage: Models:100%, Views: 97.7%
  - Excluded manage.py, migrations, commands for dummy db, test files, and few files/lines that do not need to be included in tests. 
```
---------- coverage: platform win32, python 3.13.7-final-0 -----------
Name                                                                     Stmts   Miss  Cover   Missing
------------------------------------------------------------------------------------------------------
accounts\admin.py                                                           12      0   100%
accounts\apps.py                                                             4      0   100%
accounts\models.py                                                          32      0   100%
accounts\request_serializers.py                                             13      0   100%
accounts\serializers.py                                                     31      0   100%
accounts\urls.py                                                             3      0   100%
accounts\views.py                                                           67      0   100%
assignments\admin.py                                                         4      0   100%
assignments\apps.py                                                          4      0   100%
assignments\models.py                                                       31      0   100%
assignments\request_serializers.py                                          17      0   100%
assignments\serializers.py                                                  26      0   100%
assignments\urls.py                                                          3      0   100%
assignments\views.py                                                       184      0   100%
catalog\admin.py                                                             3      0   100%
catalog\apps.py                                                              4      0   100%
catalog\models.py                                                            6      0   100%
catalog\request_serializers.py                                               3      0   100%
catalog\serializers.py                                                       6      0   100%
catalog\urls.py                                                              4      0   100%
catalog\views.py                                                            15      0   100%
core\admin.py                                                                0      0   100%
core\apps.py                                                                 4      0   100%
core\models.py                                                               0      0   100%
core\urls.py                                                                 4      0   100%
core\views.py                                                               13      0   100%
courses\admin.py                                                             4      0   100%
courses\apps.py                                                              4      0   100%
courses\models.py                                                           27      0   100%
courses\request_serializers.py                                              22      0   100%
courses\serializers.py                                                      53      0   100%
courses\urls.py                                                              3      0   100%
courses\views.py                                                           202      0   100%
feedbacks\admin.py                                                           3      0   100%
feedbacks\apps.py                                                            4      0   100%
feedbacks\models.py                                                         13      0   100%
feedbacks\request_serializers.py                                            28      0   100%
feedbacks\serializers.py                                                    24      0   100%
feedbacks\urls.py                                                            3      0   100%
feedbacks\views.py                                                         118     17    86%   51, 61-68, 78-79, 89, 99-106, 248, 302, 309, 319, 329, 339, 349
questions\admin.py                                                           3      0   100%
questions\apps.py                                                            4      0   100%
questions\models.py                                                         23      0   100%
questions\request_serializers.py                                            11      0   100%
questions\serializers.py                                                    23      0   100%
questions\urls.py                                                            3      0   100%
questions\utils\base_question_generator.py                                  53     13    75%   129, 162-170, 177-179, 184
questions\utils\pdf_to_text.py                                              35      1    97%   12
questions\views.py                                                         108      0   100%
reports\admin.py                                                             0      0   100%
reports\apps.py                                                              4      0   100%
reports\models.py                                                            0      0   100%
reports\serializers.py                                                      17      0   100%
reports\urls.py                                                              4      0   100%
reports\utils\analyze_achievement.py                                       133      0   100%
reports\views.py                                                            27      0   100%
submissions\admin.py                                                         4      0   100%
submissions\apps.py                                                          4      0   100%
submissions\models.py                                                       37      0   100%
submissions\serializers.py                                                  30      0   100%
submissions\urls.py                                                          3      0   100%
submissions\utils\feature_extractor\extract_acoustic_features.py           189     53    72%   13, 22, 26, 55, 74, 81, 89, 103-104, 140, 156-195, 216, 247, 274-284, 292-294, 306, 311-319
submissions\utils\feature_extractor\extract_all_features.py                 36      2    94%   50-51
submissions\utils\feature_extractor\extract_features_from_script.py        285     67    76%   33-34, 121, 144, 168-173, 179-200, 240, 256, 259, 279, 300, 318-319, 392, 443, 448, 501, 521-524, 538, 543-546, 560, 566-569, 577, 605-639
submissions\utils\feature_extractor\extract_semantic_features.py            76      0   100%
submissions\utils\inference.py                                              36      3    92%   45, 47, 51
submissions\utils\tail_question_generator\generate_questions_routed.py     103     47    54%   22, 476-493, 498-502, 528-536, 541-547, 552-571, 576-585, 590, 618-649
submissions\utils\wave_to_text.py                                           41      4    90%   20, 26, 47-48
submissions\views.py                                                       303      7    98%   296, 564-566, 679-682
voicetutor\settings.py                                                      43      0   100%
voicetutor\urls.py                                                           7      0   100%
------------------------------------------------------------------------------------------------------
TOTAL                                                                     2646    214    92%


=================================== 532 passed, 416 warnings in 217.01s (0:03:37) ===================================
```

- Backend Integration Test Summary
  - Verified end-to-end workflows for both teacher and student flows.
  - Checked error handling for invalid requests, missing parameters, and unknown resources.
  - Ensured proper linking across modules (auto personal assignment creation, enrollment relations).
  - Confirmed cascade delete rules function correctly.


#### 4.1.2 Frontend Testing
- **Frameworks**: JUnit
- **Architecture** : MVVM
- **Coverage goal**: > 80% coverage per component.  

- Testing Result
  - Passed 417 tests.
  - Per component coverage: Viewmodel: 95%, Models: 87%
  - As we are chaning our UI, we decided to implement UI(View) unit test in next iteration.
![frontend test](https://github.com/snuhcs-course/swpp-2025-project-team-03/blob/main/docs/images/frontend_test_iter3.png)

- Frontend Integration Test Summary
  - Implemented integration test for authentication workflows.
  - Verified form validation, user input and login flows.
  - Ensured UI state transitions.

### 4.1.3 AI Evaluation Testing
- **Golden Set**: Curated dataset of 200 annotated responses (science, math, language) with human rubrics; baseline agreement ≥ 85%.
- **Regression Guardrails**: Each AI prompt/template change triggers automated comparison against the golden set with statistical significance check (McNemar’s test).
- **Stress Scenarios**: Evaluate STT + scoring pipeline with noisy audio, code-switching, and long pauses; ensure graceful degradation and clear feedback to users.
- **Explainability Review**: Random sample of AI feedback audited weekly by pedagogy lead; flagged feedback enters retraining backlog.

### 4.1.4 Non-Functional Validation
- **Load Testing**: k6 scenarios simulate 10k concurrent quiz submissions; KPIs tracked (API latency, queue depth, error rate).
- **Chaos Testing**: Inject failures (S3 outage, STT timeout) in staging to verify retries, fallbacks, and incident alerts.
- **Security Validation**: Quarterly vulnerability scans, manual pen test scripts, JWT spoofing attempts, privilege escalation checks.
- **Accessibility Audit**: AXE automated scan + manual screen reader walkthrough; track issues in accessibility backlog with SLA 2 iterations.
- **Localization QA**: Snapshot tests for KR/EN strings, date/number formatting, pluralization; fallback to English when translation missing.

### 4.1.5 Success Metric Validation
- **Accounts (<1% auth failures)**: Load-test signup/login flows (≥1,000 requests) and compute server-side failure percentage; production dashboards monitor 5xx/error ratios.
- **Classes (invites ≤5 min)**: Capture `sent_at`/`delivered_at` timestamps for invites; QA sends batches and ensures email/FCM confirmations arrive within 300 seconds.
- **Assignments (<2% missing AI questions)**: Log LangGraph generation outcomes per assignment; nightly job calculates miss rate, while integration tests assert non-empty question sets.
- **Personal Assignments (≥95% resume success)**: Instrument resume attempts/successes; QA simulates app restarts/offline recovery to confirm state restoration above threshold.
- **Messaging (delivery ≤10 s)**: Measure message send vs confirmation timestamps; automated tests ensure confirmations within SLA, production metrics track tail latencies.

### 4.1.6 UI Instrumentation (Espresso)
- **Test Stack**: AndroidJUnitRunner, Espresso 3.5+, Compose UI Testing, Hilt testing utilities.
- **Scenarios Covered**: Student dashboard rendering, assignment navigation, audio recording/voice submission (success & STT failure), tail-question display, offline retry states, settings navigation.
- **Execution**: `./gradlew connectedAndroidTest` on API 33 emulator (Pixel 5). Compose components tagged with `Modifier.testTag` for deterministic selectors.
- **CI Integration**: Nightly pipeline launches emulator, runs instrumentation suite, archives logs/screenshots, and fails builds on regression.
- **Coverage Goal**: Maintain ≥80% statement coverage for key UI modules; smoke suite must pass before release.

### 4.2 Acceptance Testing
Selected User Stories

1. User signs up & logs in
- Given I am on the sign-up page
- When I register with name, email, password, and role (teacher or student) and then log in
- Then I should be redirected to my role-specific home screen

2. Teacher registers a new class
- Given I am logged in as a teacher
- When I create a new class with title, description, and schedule
- Then the class appears on my teacher dashboard, and I can manage contents and the student list

3. Teacher uploads a PDF to generate a quiz
- Given I am a teacher with an existing class that has enrolled students
- When I upload a PDF for that class
- Then a quiz is generated from the PDF, and enrolled students can access conversational review sessions based on its content

4. Student participates in a voice quiz session
- Given I am a student enrolled in the class and there is an active quiz
- When I start a voice-based quiz session and submit my spoken answers (converted to text via STT)
- Then my submission is recorded, and I receive AI-generated follow-up questions tailored to my weak areas

5. Teacher views student performance on the dashboard
- Given students have submitted quiz answers
- When I open the teacher dashboard for the class
- Then I can see each student’s statistics and reports based on Korean curicculum

**Schedule & Frequency**
- **When**: 
    - Conducted at the end of iteration and before major release milestones.
- **Frequency**:
    - Once per iteration.
    - Additional ad-hoc testing after significant feature updates.

**Responsibilities**
- **PM**:
    - Plan and coordinate acceptance testing sessions.
    - Verify that each selected user story meets acceptance criteria.
- **Developers**:
    - Support setup, fix identified bugs, and assist in validation.

---

## 5. External Libraries
- **Frontend**: Jetpack Compose, Retrofit, Room, Hilt  
- **Backend**: Django
- **AI**: OpenAI, Google Cloud STT

## 6. Risk Management & Mitigation
| Risk | Impact | Likelihood | Mitigation Strategy | Owner |
|------|--------|------------|---------------------|-------|
| AI scoring drift produces biased feedback | High | Medium | Weekly golden-set evaluation, automated drift alerts, human review queue | AI Lead |
| STT outage or degraded accuracy | High | Medium | Multi-region STT endpoints, fallback provider (Naver CLOVA), pre-cached prompts for manual transcription | Backend Lead |
| PDF parsing failure on complex documents | Medium | Medium | Graceful fallback to manual question creation, PDF preprocessing heuristics, validation before publish | Content Team |
| Mobile app offline submission backlog | Medium | High | Background uploader with conflict resolution, dashboard alert for pending submissions > 24h | Mobile Lead |
| Data breach of stored audio files | Critical | Low | Encryption at rest, signed URL expiry (5 min), access logging, quarterly security audit | Security Officer |
| Regulatory change (PIPA/GDPR updates) | Medium | Medium | Legal watchlist, configurable retention policies, fast track for policy updates | Compliance Officer |

## 7. Glossary & References
- **Tail Question**: Follow-up AI question triggered when accuracy/confidence below threshold.
- **Certainty Score**: Probability (0-1) returned by evaluation model representing confidence in correctness classification.
- **Curriculum Code**: Identifier aligned with Korean national curriculum competency mapping (e.g., `9과12-04`).
- **Guardian Consent**: Verified authorization from legal guardian for minors, stored with versioned policy reference.
- **Golden Set**: Curated dataset of labeled student responses used to evaluate AI performance.

**References**
- Korean Ministry of Education Curriculum Standards (2022 revision)
- OWASP ASVS v4.0.3
- WCAG 2.1 AA Guidelines
- ISO/IEC 27001 Control Mapping for Education Technology
