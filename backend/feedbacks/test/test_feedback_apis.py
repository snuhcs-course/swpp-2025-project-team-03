from datetime import timedelta

from courses.models import Enrollment
from django.test import TestCase
from django.urls import reverse
from django.utils import timezone
from feedbacks.models import TeacherFeedback
from feedbacks.test.test_factories import (
    CourseClassFactory,
    StudentFactory,
    SubjectFactory,
    TeacherFactory,
    TeacherFeedbackFactory,
)
from rest_framework import status
from rest_framework.test import APIClient


class FeedbackAPITestCase(TestCase):
    """Feedbacks API 테스트 케이스"""

    def setUp(self):
        """테스트 데이터 설정"""
        self.client = APIClient()

        # 기본 데이터 생성
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.student = StudentFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)

        # 학생을 클래스에 등록
        self.enrollment = Enrollment.objects.create(
            course_class=self.course_class, student=self.student, status=Enrollment.Status.ENROLLED
        )

        # 기존 피드백 데이터
        self.existing_feedback = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="기존 피드백 내용"
        )

    def test_message_send_success(self):
        """메시지 전송 성공 테스트"""
        url = reverse("message-send")
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "새로운 피드백 메시지",
        }

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(response.data["success"])
        self.assertEqual(response.data["message"], "메시지 전송 성공")
        self.assertIn("data", response.data)

        # DB에 피드백이 생성되었는지 확인
        feedback_count = TeacherFeedback.objects.filter(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="새로운 피드백 메시지"
        ).count()
        self.assertEqual(feedback_count, 1)

    def test_message_send_invalid_teacher(self):
        """잘못된 선생님 ID로 메시지 전송 테스트"""
        url = reverse("message-send")
        data = {
            "teacher_id": 99999,  # 존재하지 않는 ID
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "피드백 메시지",
        }

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])
        self.assertIn("Invalid teacher_id", response.data["error"])

    def test_message_send_student_as_teacher(self):
        """학생이 선생님으로 메시지 전송 시도 테스트"""
        url = reverse("message-send")
        data = {
            "teacher_id": self.student.id,  # 학생 ID를 선생님으로 사용
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "피드백 메시지",
        }

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])
        self.assertIn("User is not a teacher", response.data["error"])

    def test_message_send_invalid_class(self):
        """존재하지 않는 클래스로 메시지 전송 테스트"""
        url = reverse("message-send")
        data = {
            "teacher_id": self.teacher.id,
            "class_id": 99999,  # 존재하지 않는 클래스 ID
            "student_id": self.student.id,
            "content": "피드백 메시지",
        }

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])
        self.assertIn("Class not found", response.data["error"])

    def test_message_send_invalid_student(self):
        """존재하지 않는 학생으로 메시지 전송 테스트"""
        url = reverse("message-send")
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": 99999,  # 존재하지 않는 학생 ID
            "content": "피드백 메시지",
        }

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])
        self.assertIn("Student not found", response.data["error"])

    def test_message_send_missing_teacher_id(self):
        """teacher_id 누락 시 테스트"""
        url = reverse("message-send")
        data = {"class_id": self.course_class.id, "student_id": self.student.id, "content": "피드백 메시지"}

        response = self.client.post(url, data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])

    def test_class_message_list_success(self):
        """클래스 메시지 목록 조회 성공 테스트"""
        url = reverse("class-message-list", kwargs={"classId": self.course_class.id})

        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data["success"])
        self.assertEqual(response.data["message"], "클래스 메시지 조회 성공")
        self.assertIn("data", response.data)

        # 기존 피드백이 포함되어 있는지 확인
        feedback_data = response.data["data"]
        self.assertGreaterEqual(len(feedback_data), 1)

        # 첫 번째 피드백이 기존 피드백인지 확인
        first_feedback = feedback_data[0]
        self.assertEqual(first_feedback["content"], "기존 피드백 내용")

    def test_class_message_list_invalid_class(self):
        """존재하지 않는 클래스의 메시지 목록 조회 테스트"""
        url = reverse("class-message-list", kwargs={"classId": 99999})

        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertFalse(response.data["success"])
        self.assertIn("Class not found", response.data["error"])

    def test_message_list_student_success(self):
        """학생의 메시지 목록 조회 성공 테스트"""
        url = reverse("message-list")
        params = {"userId": self.student.id}

        response = self.client.get(url, params)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data["success"])
        self.assertEqual(response.data["message"], "사용자 메시지 조회 성공")
        self.assertIn("data", response.data)

        # 학생이 받은 피드백이 포함되어 있는지 확인
        feedback_data = response.data["data"]
        self.assertGreaterEqual(len(feedback_data), 1)

    def test_message_list_teacher_success(self):
        """선생님의 메시지 목록 조회 성공 테스트"""
        url = reverse("message-list")
        params = {"userId": self.teacher.id}

        response = self.client.get(url, params)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data["success"])
        self.assertEqual(response.data["message"], "사용자 메시지 조회 성공")
        self.assertIn("data", response.data)

        # 선생님이 보낸 피드백이 포함되어 있는지 확인
        feedback_data = response.data["data"]
        self.assertGreaterEqual(len(feedback_data), 1)

    def test_message_list_with_limit(self):
        """limit 파라미터로 메시지 목록 조회 테스트"""
        # 추가 피드백 생성
        for i in range(5):
            TeacherFeedbackFactory(
                course_class=self.course_class, student=self.student, teacher=self.teacher, content=f"추가 피드백 {i}"
            )

        url = reverse("message-list")
        params = {"userId": self.student.id, "limit": 3}

        response = self.client.get(url, params)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data["success"])

        # limit이 적용되었는지 확인
        feedback_data = response.data["data"]
        self.assertLessEqual(len(feedback_data), 3)

    def test_message_list_missing_user_id(self):
        """userId 누락 시 테스트"""
        url = reverse("message-list")

        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertFalse(response.data["success"])
        self.assertIn("userId is required", response.data["error"])

    def test_message_list_invalid_user(self):
        """존재하지 않는 사용자로 메시지 목록 조회 테스트"""
        url = reverse("message-list")
        params = {"userId": 99999}

        response = self.client.get(url, params)

        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertFalse(response.data["success"])
        self.assertIn("User not found", response.data["error"])


class FeedbackIntegrationTestCase(TestCase):
    """Feedbacks 통합 테스트 케이스"""

    def setUp(self):
        """통합 테스트 데이터 설정"""
        self.client = APIClient()

        # 여러 선생님과 학생, 클래스 생성
        self.subject = SubjectFactory()
        self.teacher1 = TeacherFactory()
        self.teacher2 = TeacherFactory()
        self.student1 = StudentFactory()
        self.student2 = StudentFactory()

        self.class1 = CourseClassFactory(teacher=self.teacher1, subject=self.subject)
        self.class2 = CourseClassFactory(teacher=self.teacher2, subject=self.subject)

        # 학생들을 클래스에 등록
        Enrollment.objects.create(course_class=self.class1, student=self.student1, status=Enrollment.Status.ENROLLED)
        Enrollment.objects.create(course_class=self.class2, student=self.student2, status=Enrollment.Status.ENROLLED)

    def test_complete_feedback_workflow(self):
        """완전한 피드백 워크플로우 테스트"""
        # 1. 선생님이 학생에게 피드백 전송
        send_url = reverse("message-send")
        send_data = {
            "teacher_id": self.teacher1.id,
            "class_id": self.class1.id,
            "student_id": self.student1.id,
            "content": "통합 테스트 피드백",
        }

        send_response = self.client.post(send_url, send_data, format="json")
        self.assertEqual(send_response.status_code, status.HTTP_201_CREATED)

        # 2. 클래스의 모든 메시지 조회
        class_url = reverse("class-message-list", kwargs={"classId": self.class1.id})
        class_response = self.client.get(class_url)
        self.assertEqual(class_response.status_code, status.HTTP_200_OK)

        # 3. 학생의 메시지 조회
        student_url = reverse("message-list")
        student_params = {"userId": self.student1.id}
        student_response = self.client.get(student_url, student_params)
        self.assertEqual(student_response.status_code, status.HTTP_200_OK)

        # 4. 선생님의 메시지 조회
        teacher_params = {"userId": self.teacher1.id}
        teacher_response = self.client.get(student_url, teacher_params)
        self.assertEqual(teacher_response.status_code, status.HTTP_200_OK)

        # 모든 응답에서 동일한 피드백이 포함되어 있는지 확인
        class_feedbacks = class_response.data["data"]
        student_feedbacks = student_response.data["data"]
        teacher_feedbacks = teacher_response.data["data"]

        # 각 응답에서 '통합 테스트 피드백'이 포함되어 있는지 확인
        class_content = [f["content"] for f in class_feedbacks]
        student_content = [f["content"] for f in student_feedbacks]
        teacher_content = [f["content"] for f in teacher_feedbacks]

        self.assertIn("통합 테스트 피드백", class_content)
        self.assertIn("통합 테스트 피드백", student_content)
        self.assertIn("통합 테스트 피드백", teacher_content)

    def test_multiple_feedbacks_ordering(self):
        """여러 피드백의 정렬 테스트"""
        # 여러 피드백 생성 (시간 간격을 두고)
        TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="첫 번째 피드백",
            created_at=timezone.now() - timedelta(hours=2),
        )
        TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="두 번째 피드백",
            created_at=timezone.now() - timedelta(hours=1),
        )
        TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="세 번째 피드백",
            created_at=timezone.now(),
        )

        # 클래스 메시지 조회
        url = reverse("class-message-list", kwargs={"classId": self.class1.id})
        response = self.client.get(url)

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        feedbacks = response.data["data"]

        # 최신순으로 정렬되어 있는지 확인
        self.assertEqual(feedbacks[0]["content"], "세 번째 피드백")
        self.assertEqual(feedbacks[1]["content"], "두 번째 피드백")
        self.assertEqual(feedbacks[2]["content"], "첫 번째 피드백")

    def test_response_consistency(self):
        """API 응답 일관성 테스트"""
        # 피드백 생성
        TeacherFeedbackFactory(
            course_class=self.class1, student=self.student1, teacher=self.teacher1, content="일관성 테스트 피드백"
        )

        # 클래스 메시지 조회
        class_url = reverse("class-message-list", kwargs={"classId": self.class1.id})
        class_response = self.client.get(class_url)

        # 학생 메시지 조회
        student_url = reverse("message-list")
        student_params = {"userId": self.student1.id}
        student_response = self.client.get(student_url, student_params)

        # 선생님 메시지 조회
        teacher_params = {"userId": self.teacher1.id}
        teacher_response = self.client.get(student_url, teacher_params)

        # 모든 응답이 동일한 구조를 가지는지 확인
        for response in [class_response, student_response, teacher_response]:
            self.assertIn("success", response.data)
            self.assertIn("data", response.data)
            self.assertIn("message", response.data)
            self.assertTrue(response.data["success"])

        # 각 응답에서 동일한 피드백 데이터가 포함되어 있는지 확인
        class_feedback = class_response.data["data"][0]
        student_feedback = student_response.data["data"][0]
        teacher_feedback = teacher_response.data["data"][0]

        # 핵심 필드들이 동일한지 확인
        self.assertEqual(class_feedback["content"], student_feedback["content"])
        self.assertEqual(class_feedback["content"], teacher_feedback["content"])
        self.assertEqual(class_feedback["id"], student_feedback["id"])
        self.assertEqual(class_feedback["id"], teacher_feedback["id"])


# pytest 실행 예시
# pytest feedbacks/test/test_feedback_apis.py -v
