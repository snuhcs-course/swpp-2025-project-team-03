## 0. Table of Contents

- [1. Document Revision History](#1-document-revision-history)
- [2. Project Abstract](#2-Project-Abstract)
- [3. Customer](#3-Customer)
- [4. Competitive Landscape](#4-Competitive-Landscape)
- [5. Functional Requirements](#5-Functional-Requirements)
  - [User Stories](#User-Stories)
  - [User Acceptance Criteria](#User-Acceptance-Criteria)
- [6. Non-Functional Requirements](#6-Non-Functional-Requirements)
- [7. User Interface Requirements](#7-User-Interface-Requirements)
  - [Wireframes](#Wireframes)
  - [Failure Cases](#Failure-cases)
  - [Key Screens Overview](#Key-Screens-Overview)

---

## 1. Document Revision History
| Version | Date       | Message              |
|---------|------------|----------------------|
| 0.1     | 2025-09-24 | Initial draft created |
| 0.2    | 2025-09-30 | Added login, roles, class & assignment flows |
| 0.3    | 2025-10-01 | Revised user stories, added user acceptance criteria |
| 0.4    | 2025-10-03 | Updated customer (persona) |
| 0.5    | 2025-10-04 | Updated competitive landscape |
| 1.1    | 2025-10-14 | Revised project abstract |
| 1.2    | 2025-10-14 | Add table of contents |
| 2.1    | 2025-10-30 | Minor revisions |
| 2.2    | 2025-11-01 | Revised project abstract & Non-functional requirements |
| 2.3    | 2025-11-02 | Added failure cases & Updated Wireframe |
| 3.0    | 2025-11-09 | Expanded detailed requirements, KPIs, and compliance criteria |
---

## 2. Project Abstract
This platform is an **AI-powered learning and assignment management system**.  
Users (students and teachers) can register with role-based accounts, manage courses and assignments, and interact with AI-driven personalized assessments. The AI-driven personalized assessments are conducted in a conversational format. The AI generates open-ended questions, analyzes students’ verbal responses, and then creates adaptive follow-up quizzes as part of the review process. The system integrates a Jetpack Compose frontend, Django backend. Key features include AI-generated continuous question-answer quizzes, STT (Speech-to-Text) based student responses, and detailed performance reports aligned with Korean national curriculum.

---

## 3. Customer
### Students (Primary)
- **Who they are**: Elementary and middle school students in after-school academies or private tutoring programs.  
- **Current pain**: Lessons are often one-directional; students memorize procedures but struggle to explain concepts, making it unclear if they truly understand.  
- **Why VoiceTutor**: Encourages verbal explanation with AI feedback, helping students strengthen comprehension, build confidence, and close knowledge gaps.  

### Teachers (Primary)
- **Who they are**: Instructors at academies or schools managing large groups with diverse comprehension levels.  
- **Current pain**: Difficult to individually assess conceptual understanding; most evaluation relies only on written tests.  
- **Why VoiceTutor**: Provides structured insights into verbal explanations, making it easier to identify weak areas and adapt teaching strategies.

### Educational Institutions (Secondary)
- **Who they are**: Academies, schools, or edu-tech providers seeking to improve learning outcomes with innovative tools.  
- **Current pain**: Lack standardized methods to evaluate comprehension and communication skills across students.  
- **Why VoiceTutor**: Delivers AI-enhanced comprehension and evaluation tools that integrate with existing programs for scalable, consistent measurement.  

---

## 4. Competitive Landscape
| Feature | Kahoot! | Vocal Image | Quizlet | Google Classroom | VoiceTutor |
|---------|---------|-------------|---------|------------------|--------------------------|
| Role-based sign-up (student/teacher) | O | X | O | O | O |
| STT-based voice submission | X | O | X | X | O |
| AI-based comprehension feedback | X | △  | X | X | O |
| PDF & curriculum-based quiz generation | O | X | O  | X | O |

- Kahoot!, Quizlet  
  They are known for their quiz platforms, offering students a flexible way to practice and review materials.  
  However, they mainly focus on multiple-choice answers, not verbal explanations.

- Vocal Image  
  It specializes in voice-based analysis for communication skills, particularly pronunciation and emotional tone detection.  
  It provides only surface-level analysis of voice without linking to academic content or structured curriculum.

- Google Classroom  
  It is strong in logistics and assignment management.  
  However, it lacks advanced assessment tools, AI-driven feedback, and voice-based evaluation.

- VoiceTutor  
  VoiceTutor uniquely combines the strengths of existing solutions while addressing their limitations.  
  Unlike Kahoot! and Quizlet, it goes beyond memorization and competition, focusing on students’ verbal explanations to measure true comprehension.  
  Unlike Vocal Image, it doesn’t stop at voice analytics. It connects voice responses directly to curriculum content, generating AI-based follow-up questions.



---

## 5. Functional Requirements
### User Stories
1. **Account & Roles**  
   - As a user, I can sign up with my name, email, password, and role (student/teacher).  
   - As a user, I can log in with email and password.  
   - As a user, I can update settings (notifications, help, app info).  

2. **Teacher Role**  
   - As a teacher, I can access my dashboard and see all created assignments.  
   - As a teacher, I can create assignments with title, description, due date, class selection.
   - As a teacher, I can upload pdf files, so that students can participate in conversational review sessions based on the pdf files.
   - As a teacher, I can set the curriculum of a class, so that I can notice curriculum of a class to students.
   - As a teacher, I can register classes, so that I can manage contents of classes and students who participate in my classes.
   - As a teacher, I can view student submissions, correctness, and certainty levels.
   - As a teacher, I can invite students to the class created, using students’ ID. I can also kick out students for those who quit the course.  
   - As a teacher, I can send messages to the whole class or individual students.
   - As a teacher, I can get report (pdf) which contains students’ academic achievement based on their verbal response to the question.  

3. **Student Role**
   - As a student, I can accept an invitation, so that I can get access to the class.
   - As a student, I can receive assigned homework and submit before the deadline.  
   - As a student, I can record my voice answers. The app converts them via STT into text + timestamps.  
   - As a student, I receive AI-generated follow-up questions based on my previous answers.
   - As a student, I can check my learning status, so that I can make sure that my review was done well.  
   - As a student, I can review my performance report: answers, model answers, correctness, certainty, and score.  

4. **AI Features**  
   - AI generates quizzes based on PDF material.  
   - AI evaluates answers based on model's answer.  
   - AI provides tailored follow-up questions to reinforce weak areas.

### User Acceptance Criteria
**Scenario: Teacher uploads PDF file after the class**

- **Given** that I am a teacher logged into the system. I have a class with 5 students.
And I have a PDF file prepared for class,
- **when** I upload the PDF file,
- **then** five students participating in my class are able to access conversational review sessions based on the PDF content.

**Scenario: Teacher sets curriculum of a class**

- **Given** that I have already created a class,
- **when** I add or update the curriculum for the class,
- **then** the curriculum should be saved in the system, and enrolled students should be able to check the curriculum.

**Scenario: Teacher registers a new class**

- **Given** that I am logged in as a teacher,
- **when** I create a new class with required information (title, description, schedule etc.),
- **then** the class should appear in my teacher dashboard, and I should be able to manage class contents and student list.

**Scenario: Teacher invites students**

- **Given** that I have created a class,
- **when** I enter a student’s ID and send an invitation,
- **then** the student should receive an invitation to join the class.

**Scenario: Teacher removes student**

- **Given** that a student has enrolled in my class,
- **when** I choose to remove the student,
- **then** the student should no longer have access to the class materials or sessions.

**Scenario: Teacher want to check generated reports**

- **Given** that students have completed voice quiz sessions,
- **when** I request a report of their academic achievement,
- **then** the system should generate a PDF report. And the report should include each student’s performance based on verbal responses. So I can analyze and use it for teaching

**Scenario: Student accepts an invitation to join a class**

- **Given** that I am a student with a valid invitation,
- **when** I accept the invitation from the teacher of my class,
- **then** I should gain access to the class materials, sessions, and quizzes.


**Scenario: Student participates in a voice quiz session**

- **Given** that I am a student enrolled in a class,
- **when** I start a voice-based quiz session,
- **then** the system should listen to my verbal responses, and provide feedback to teacher’s app.


**Scenario: Student selects a class to take quizzes from**

- **Given** that I am a student enrolled in multiple classes,
- **when** I choose a specific class before starting a quiz,
- **then** the quiz questions should align with the curriculum of the chosen class.


**Scenario: Student checks their learning progress**

- **Given** that I am a student who has completed quiz sessions,
- **when** I open my learning dashboard,
- **then** I should see a progress indicator (e.g., traffic-light or percentage) so that  I can confirm whether I’ve reviewed sufficiently and know which areas need improvement.**er the class**

---

### Additional Functional Detail

**Information Architecture**

| Domain Area | Entities & Key Attributes | Primary Actions | Success Metrics |
|-------------|---------------------------|-----------------|-----------------|
| Accounts | `User(id, role, email, displayName, locale, consentVersion)` | Sign up, log in/out, password reset, consent renewal | < 1% auth failures due to platform error |
| Classes | `Class(id, teacherId, subjectId, name, schedule, maxCapacity, visibility)` | Create, update, archive, invite/kick students, export roster | 100% invitations delivered within 5 minutes |
| Assignments | `Assignment(id, classId, title, dueAt, rubricId, materialId)` | Create, duplicate, publish, close, archive | < 2% assignments missing AI question set |
| Personal Assignments | `PersonalAssignment(id, assignmentId, studentId, status, solvedNum, startedAt, submittedAt)` | Fetch next question, submit answer, resume session | ≥ 95% resume success after interruption |
| Messaging | `Message(id, senderId, recipientScope, channel, status)` | Broadcast, direct message, queue, deliver, mark as read | Delivery confirmation within 10 seconds |

**Success Metric Validation Plan**
- **Accounts (`< 1% auth failures`)**: Run automated login/signup test suites (e.g., k6/Gatling) with ≥1,000 requests; capture HTTP 5xx or server exceptions and verify failure ratio. Monitor production via error-rate dashboards.
- **Classes (`100% invitations delivered ≤ 5 min`)**: Instrument invite creation with `sent_at`/`delivered_at`; QA sends batched invites and confirms delivery events (email/FCM) occur within 300 seconds. Alert on any exceedance.
- **Assignments (`< 2% missing AI question set`)**: After assignment creation, log AI question-generation outcomes. Nightly job computes failure percentage; unit/integration tests ensure LangGraph pipeline returns non-empty questions.
- **Personal Assignments (`≥ 95% resume success`)**: Log resume attempts/success events. QA regression includes forced app restarts/network drops to confirm session state persists; calculate success ratio over sample runs.
- **Messaging (`delivery confirmation ≤ 10 s`)**: Capture message send/confirm timestamps (FCM/email API). Automated test sends messages and asserts confirmation within 10 seconds; production dashboard monitors tail latency.

**Teacher Journeys**
- Prepare lesson → upload PDF → receive AI summary within 90 seconds → adjust baseline questions → attach rubric & publish assignment.
- Monitor progress → dashboard heatmap by curriculum tag → identify at-risk learners (accuracy < 70% & certainty < 0.4) → drill into transcript + waveform for each response.
- Intervene → send individualized feedback referencing timestamps → auto-attach recommended remedial materials from `Subject` catalog.

**Student Journeys**
- Onboarding → accept invitation or join with class code → verify guardian consent if age < 14 → run microphone diagnostics.
- Quiz flow → pre-flight checklist (network, mic) → record → STT & AI verdict with confidence bands → tail questions continue until mastery threshold reached.
- Review → dashboard shows streaks, revision queue, exportable self-reflection log, and AI tips.

**Administrative Journeys**
- Support agent can initiate read-only session shadowing with explicit audit entry.
- System nudges inactive students (no submission for 7 days) via push/email using templated messaging.

**Adaptive Feedback Buckets**
- We classify every verbal response along two axes—semantic correctness and confidence score—to produce four rule-based feedback buckets (`A`: correct/high confidence, `B`: correct/low confidence, `C`: incorrect/high confidence, `D`: incorrect/low confidence). This structure operationalizes Hasan et al. (2020) Affective Tutoring System (ATS) by combining knowledge state with affective cues, and Pelánek & Jarušek’s recommendation to incorporate paralinguistic signals (pauses, prosody) alongside semantic meaning.
- Confidence is inferred by an XGBoost regression model trained on acoustic (pause ratios, f0 slopes, silence %) and semantic coherence features (adjacent sentence similarity, topic drift) extracted during feature processing. Semantic correctness is judged by an LLM “planner” prompted to return a minimal JSON verdict while tolerating STT noise and paraphrases.
- The four buckets enable differentiated scaffolding consistent with Abar et al. (2025): `A` routes to enrichment prompts, `B` reinforces concepts to build self-efficacy, `C` targets misconception repair, and `D` re-teaches foundational ideas. Each bucket maps to tailored follow-up strategies, difficulty levels, and tone so learners receive feedback that matches their zone of proximal development.

**Data Lifecycle**
- Raw audio encrypted at rest (AES-256) retained 90 days (configurable), metadata retained per compliance policy.
- AI prompts/responses stored with assignment/question references for traceability and model evaluation.

**Frontend Integration Status**
- Implemented: authentication, assignment CRUD + S3 upload, personal-assignment answer flow (`/personal_assignments/answer/`), tail-question retrieval, class/student listings, curriculum report stub.
- Pending implementation on backend: `GET /courses/students/{id}/assignments/`, `POST /assignments/{id}/submit/`, and pronunciation/confidence fields returned in assignment feedback.
- `/reports/progress/` currently returns a placeholder response even though the client passes `teacherId`, `classId`, and `period` query params. Marked as beta until analytics pipeline ships.
- Action item: align Retrofit definitions with Django endpoints prior to launch (either add endpoints or remove unused calls).

### Backend API Surface
All REST endpoints are served under `/api/` and return `{ "success": bool, "data": …, "message": str | null, "error": str | null }`.

**Core**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/core/health/` | Health check endpoint |
| GET | `/core/error/` | Intentionally raises an error (debug/testing) |
| GET | `/core/logs/tail` | Return the last `n` lines of `nohup.out` (`n` query param required) |

**Authentication**

| Method | Path | Description |
| --- | --- | --- |
| POST | `/auth/signup/` | Create teacher/student account and issue JWT pair |
| POST | `/auth/login/` | Authenticate and obtain JWT pair |
| POST | `/auth/logout/` | Client-initiated logout (stateless) |

**Assignments**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/assignments/` | List assignments (`teacherId`, `classId`, `status` filters) |
| POST | `/assignments/create/` | Create assignment and return S3 presigned upload URL |
| GET | `/assignments/{id}/` | Retrieve assignment detail (materials included) |
| PUT | `/assignments/{id}/` | Update assignment metadata |
| DELETE | `/assignments/{id}/` | Delete assignment |
| POST | `/assignments/{id}/submit/` | Placeholder endpoint for manual submission workflow |
| GET | `/assignments/{id}/questions/` | List generated/base questions |
| GET | `/assignments/{id}/results/` | Completion summary for personal assignments |
| GET | `/assignments/{assignment_id}/s3-check/` | Validate uploaded PDF in S3 |
| GET | `/assignments/teacher-dashboard-stats/` | Aggregate counts for teacher dashboard |

**Questions**

| Method | Path | Description |
| --- | --- | --- |
| POST | `/questions/create/` | Generate base questions/summary from uploaded material |

**Courses – Students**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/courses/students/` | List students (filter by `teacherId`/`classId`) |
| GET | `/courses/students/{id}/` | Student profile with enrolments |
| PUT | `/courses/students/{id}/` | Update student fields |
| DELETE | `/courses/students/{id}/` | Delete student (requires reason body) |
| GET | `/courses/students/{id}/statistics/` | Assignment progress counts |

**Courses – Classes**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/courses/classes/` | List classes (optional `teacherId`) |
| POST | `/courses/classes/` | Create class |
| GET | `/courses/classes/{id}/` | Class detail |
| PUT | `/courses/classes/{id}/` | Update class metadata |
| DELETE | `/courses/classes/{id}/` | Delete class |
| GET | `/courses/classes/{id}/students/` | List enrolled students |
| PUT | `/courses/classes/{id}/students/` | Enrol student via id/name/email |
| GET | `/courses/classes/{classId}/students-statistics/` | Per-student completion stats |
| GET | `/courses/classes/{id}/completion-rate/` | Overall completion rate |

**Personal Assignments & Submissions**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/personal_assignments/` | List personal assignments (`student_id` or `assignment_id` required) |
| GET | `/personal_assignments/{id}/questions/` | Base + tail questions for personal assignment |
| GET | `/personal_assignments/{id}/statistics/` | Aggregated stats |
| POST | `/personal_assignments/{id}/complete/` | Mark as submitted |
| POST | `/personal_assignments/answer/` | Upload WAV answer (multipart) |
| GET | `/personal_assignments/answer/` | Fetch next question (`personal_assignment_id` query) |
| GET | `/personal_assignments/{id}/correctness/` | List answered questions with correctness |
| GET | `/personal_assignments/recentanswer/` | Most recent in-progress assignment for student |

**Feedbacks & Messaging**

| Method | Path | Description |
| --- | --- | --- |
| POST | `/feedbacks/messages/send/` | Teacher sends message |
| GET | `/feedbacks/messages/` | Messages for current user (`userId`, `limit`) |
| GET | `/feedbacks/messages/{classId}/` | Class message history |
| GET | `/feedbacks/dashboard/stats/` | Teacher dashboard stats |
| GET | `/feedbacks/dashboard/recent-activities/` | Latest activity feed |
| POST | `/feedbacks/analysis/student/` | Placeholder analytics endpoint |
| POST | `/feedbacks/analysis/class/` | Placeholder analytics endpoint |
| POST | `/feedbacks/analysis/subject/` | Placeholder analytics endpoint |
| GET | `/feedbacks/reports/progress/` | Placeholder progress report (`teacherId`, `classId`, `period`) |

**Reports & Catalog**

| Method | Path | Description |
| --- | --- | --- |
| GET | `/reports/{class_id}/{student_id}/` | Curriculum analysis report generated on demand |
| GET | `/catalog/subjects/` | List available subjects |

---

### Extended Acceptance Criteria

- **Scenario: Teacher edits AI-generated question before publish**
  - **Given** an AI-generated question set exists,
  - **when** the teacher modifies question text, answer, or difficulty,
  - **then** version history captures the edit, AI baseline remains accessible, and students only see the latest approved version.

- **Scenario: Student recovers from upload failure**
  - **Given** an audio upload fails due to connectivity,
  - **when** the device regains connection within 2 minutes,
  - **then** upload resumes automatically without losing recorded audio and the student receives success confirmation.

- **Scenario: Guardian requests monthly participation report**
  - **Given** the guardian has verified access,
  - **when** they request a report,
  - **then** the system generates a PDF summarizing attendance, quiz attempts, accuracy trend, and AI recommendations.

- **Scenario: Accessibility compliance**
  - **Given** accessibility mode is enabled,
  - **when** the student navigates any view,
  - **then** all controls expose TalkBack/VoiceOver labels, respect high-contrast palettes, and maintain minimum 48dp touch targets.

- **Scenario: Teacher bulk imports roster**
  - **Given** a CSV roster follows the template,
  - **when** it is uploaded,
  - **then** valid rows create invitations, invalid rows produce inline errors with suggested fixes, and duplicates are ignored with notice.

- **Scenario: Class analytics export**
  - **Given** a teacher filters dashboard data,
  - **when** they export analytics,
  - **then** a CSV including curriculum codes, accuracy, certainty, completion rate, and timestamps is generated within 60 seconds.

- **Scenario: AI follow-up question moderation**
  - **Given** content moderation flags a follow-up question,
  - **when** the teacher reviews it,
  - **then** they can approve, edit, or reject; rejected content is removed from queue and logged for AI policy review.

- **Scenario: Adaptive bucket routing produces tailored follow-up**
  - **Given** a student submits a voice answer,
  - **when** the system evaluates semantic correctness via the planner and confidence via the multimodal model,
  - **then** it assigns one of four buckets (A–D) and delivers the corresponding follow-up strategy (enrichment, reinforcement, misconception repair, or scaffolding) with a question generated specifically for that bucket.

---

## 6. Non-Functional Requirements
- **Latency**: AI feedback (quiz or evaluation) in < 15 seconds.  
- **Reliability**: Ensure submission integrity (voice files, STT transcription, evaluation logs).  
- **Usability**: Simple onboarding for both students and teachers. 
- **Scalability**: Supports multiple concurrent quiz sessions 

**Additional Quality Attributes**

- **Availability**: Single-node deployment (Gunicorn + PostgreSQL); uptime tracked manually, with planned migration to monitored hosting.
- **Security**: JWT authentication (30-minute access tokens, 1-day refresh); secrets loaded from `.env`; HTTPS termination handled by Nginx.
- **Privacy & Compliance**: Voice and transcript data stored in PostgreSQL/S3; retention window configurable via admin scripts; guardian consent tracked in user profile.
- **Observability**: Django structured logging to stdout; log aggregation/alerting TBD (currently manual tail and Sentry backlog item).
- **Accessibility**: UI follows Material accessibility guidelines; backend returns localized error messages (KR) where applicable.
- **Internationalization**: Current locale support KR/en-US; additional locales planned through resource bundles.
- **Device Support**: Android API 26+ (phones/tablets); handles intermittent connectivity with local caching of assignments.
- **Scalability**: Vertical scaling (larger VM) and request batching; asynchronous job queue is a roadmap item.
- **Resilience**: S3 interactions wrapped in try/except with user-facing fallbacks; no circuit breaker yet.
- **Maintainability**: Django ORM migrations; OpenAPI (drf-yasg) generated docs; API versioning policy to be formalized before next major release.
- **Data Store**: PostgreSQL 15 for transactional data; AWS S3 for PDFs/audio; Redis not yet in use.

**Operational Metrics**
- Track request latency and error rates via Django logs (exported weekly).
- STT transcription quality reviewed on sampled submissions; manual regrading for disputes.
- Tail-question latency monitored per request to keep total answer submission under 15 seconds.
- Incident log maintained in project wiki (includes root-cause notes and follow-up tasks).

**Instrumentation (Espresso)**
- Android UI regression tests run with Espresso + Compose testing. Gradle task: `./gradlew connectedAndroidTest`.
- Test environment: Android API 33 virtual device (Pixel 5) with Hilt test runner `androidx.test.runner.AndroidJUnitRunner`.
- Core scenarios covered: student dashboard rendering, assignment navigation, audio recording UI states, error dialogs (STT failure, network retry), tail-question display.
- CI gate: instrumentation suite must pass on nightly builds; failures block release until triaged.

---

## 7. User Interface Requirements
### Wireframes
**Teacher Wireframe**
![Teacher Wireframe](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/Wireframe_teacher_iter3_fixed.png)

**Student Wireframe**
![Student Wireframe](https://raw.githubusercontent.com/snuhcs-course/swpp-2025-project-team-03/main/docs/images/Wireframe_student_iter3_fixed.png)

### Failure Cases
- **Teacher Flow**
  - If the uploaded PDF fails to parse, show **“Upload failed, please retry.”**
  - If the uploaded file is not a PDF, show **“Unsupported file type.”**
  - If quiz generation takes too long or times out, show **“Generating quiz… please wait or try again later.”**
  - If the teacher tries to create a class without a class name, show **“Class name is required.”**
  - If adding a student fails due to invalid student ID/email, show **“Student not found.”**
  - If server communication fails while saving an assignment, show **“Network error, draft saved locally.”**
  - If the teacher attempts to open assignment results but data is missing, show **“Report unavailable, please refresh.”**
  - If a removed student’s detail page is accessed through a stale link, show **“This student is no longer enrolled.”**

- **Student Flow**
  - If microphone permission is denied, show alert and guide the user to system settings.
  - If STT transcription fails, show **“Could not recognize voice, please try again.”**
  - If the quiz session is interrupted by network loss, show **“Connection lost. Reconnecting…”** and pause recording.
  - If the student tries to access an expired or disabled quiz, show **“This assignment is no longer available.”**
  - If no audio is detected during recording, show **“No speech detected. Please try again.”**
  - If answer submission fails due to timeout, show **“Submission failed. Retrying…”** and auto-retry in background.
  - If adaptive feedback fails to load, show **“Unable to generate feedback right now.”**
  - If the quiz is exited mid-question, show confirm modal: **“Leave quiz? Your progress will be saved.”**

### Key Screens Overview
- **Onboarding**: Role selection (student/teacher), sign-up/login.  
- **Settings**: Notifications, language, help, app info.  
- **Teacher**:  
  - **Landing & Sign-Up**: Teachers register and access their dashboard.
  - **Dashboard**: Overview of classes, quizzes, and student progress.
  - **Quiz Management**: Create, edit, or delete quizzes.
  - **Student Monitoring**: View student details, performance, and send feedback.
  - **Reports**: Check class-wide insights and learning trends.
- **Student**:  
  - **Landing & Sign-Up**: Users enter the app and create an account as a student.
  - **Dashboard**: Main hub showing quizzes, progress, and quick navigation.
  - **Quiz Details & Start**: View quiz info and begin solving.
  - **Recording & Results**: Answers are recorded and results shown with basic analytics.
  - **Reports & Profile**: Students can track performance trends and edit personal settings.  

---

## 8. Data Governance & Ethics
- **Consent Management**: Capture explicit guardian consent for minors; persist consent version, timestamp, and policy reference; re-consent triggered after substantive policy updates.
- **Data Minimization**: Collect only essential PII (name, email, role); redact STT transcripts before downstream analytics to remove sensitive phrases; anonymize aggregated reports.
- **Bias Monitoring**: Quarterly review of AI scoring across demographic cohorts; maintain fairness dashboard (gender, grade level, language proficiency); flag deviations greater than ±5%.
- **Audit Trail**: Immutable logs for role changes, assignment edits, AI override actions, and data exports; provide downloadable audit bundle for compliance reviews.
- **Incident Response**: 24-hour notification SLA for data incidents; conduct semiannual tabletop exercises; maintain breach communication templates and root-cause playbooks.

## 9. Release & QA Checklist
- **Definition of Ready**: User story includes acceptance criteria, negative paths, analytics requirements, mock data, and performance targets.
- **Definition of Done**: Code merged with unit/integration tests, QA sign-off, accessibility verification, documentation updates, feature flags configured, and rollback plan rehearsed.
- **Performance Testing**: Load test AI pipeline with 500 concurrent uploads; ensure queue wait time < 5 seconds; publish Grafana snapshot per release.
- **Security Testing**: Static analysis per commit, dependency scanning weekly, penetration test each major release, OWASP ASVS Level 2 checklist updated quarterly.
- **User Acceptance Testing**: Pilot cohort (≥ 5 teachers, ≥ 20 students) executes scripted scenarios; collect SUS survey (target ≥ 80) and usability findings backlog.
- **Rollout Strategy**: Feature flags for AI tail-depth and dashboard analytics; staged rollout (10% → 50% → 100%); documented rollback steps including data migration reversal.
- **Documentation**: Release notes, API changelog, teacher/student quick-start guides, support FAQ updates synchronized with deployment; knowledge base articles localized (KR/EN).
