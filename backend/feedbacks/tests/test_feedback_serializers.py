from django.test import TestCase
from feedbacks.request_serializers import MessageSendRequestSerializer
from feedbacks.serializers import (
    CourseClassInfoSerializer,
    MessageSerializer,
    StudentInfoSerializer,
    TeacherInfoSerializer,
)

from .test_feedback_factories import (
    CourseClassFactory,
    StudentFactory,
    SubjectFactory,
    TeacherFactory,
    TeacherFeedbackFactory,
)

# pytest 실행 예시
# pytest feedbacks/tests/test_feedback_serializers.py -v


class TeacherInfoSerializerTestCase(TestCase):
    """TeacherInfoSerializer 테스트"""

    def setUp(self):
        self.teacher = TeacherFactory()

    def test_teacher_info_serialization(self):
        """선생님 정보 직렬화 테스트"""
        serializer = TeacherInfoSerializer(self.teacher)
        data = serializer.data

        self.assertEqual(data["id"], self.teacher.id)
        self.assertEqual(data["display_name"], self.teacher.display_name)
        self.assertEqual(data["email"], self.teacher.email)
        self.assertEqual(len(data), 3)  # id, display_name, email만 포함


class StudentInfoSerializerTestCase(TestCase):
    """StudentInfoSerializer 테스트"""

    def setUp(self):
        self.student = StudentFactory()

    def test_student_info_serialization(self):
        """학생 정보 직렬화 테스트"""
        serializer = StudentInfoSerializer(self.student)
        data = serializer.data

        self.assertEqual(data["id"], self.student.id)
        self.assertEqual(data["display_name"], self.student.display_name)
        self.assertEqual(data["email"], self.student.email)
        self.assertEqual(len(data), 3)  # id, display_name, email만 포함


class CourseClassInfoSerializerTestCase(TestCase):
    """CourseClassInfoSerializer 테스트"""

    def setUp(self):
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)

    def test_course_class_info_serialization(self):
        """클래스 정보 직렬화 테스트"""
        serializer = CourseClassInfoSerializer(self.course_class)
        data = serializer.data

        self.assertEqual(data["id"], self.course_class.id)
        self.assertEqual(data["name"], self.course_class.name)
        self.assertEqual(len(data), 2)  # id, name만 포함


class MessageSerializerTestCase(TestCase):
    """MessageSerializer 테스트"""

    def setUp(self):
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.student = StudentFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)
        self.feedback = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="테스트 피드백 내용"
        )

    def test_message_serialization(self):
        """메시지 직렬화 테스트"""
        serializer = MessageSerializer(self.feedback)
        data = serializer.data

        # 기본 필드 확인
        self.assertEqual(data["id"], self.feedback.id)
        self.assertEqual(data["content"], self.feedback.content)
        self.assertIn("created_at", data)

        # 중첩된 객체 확인
        self.assertIn("course_class", data)
        self.assertIn("student", data)
        self.assertIn("teacher", data)

        # course_class 정보 확인
        course_class_data = data["course_class"]
        self.assertEqual(course_class_data["id"], self.course_class.id)
        self.assertEqual(course_class_data["name"], self.course_class.name)

        # student 정보 확인
        student_data = data["student"]
        self.assertEqual(student_data["id"], self.student.id)
        self.assertEqual(student_data["display_name"], self.student.display_name)
        self.assertEqual(student_data["email"], self.student.email)

        # teacher 정보 확인
        teacher_data = data["teacher"]
        self.assertEqual(teacher_data["id"], self.teacher.id)
        self.assertEqual(teacher_data["display_name"], self.teacher.display_name)
        self.assertEqual(teacher_data["email"], self.teacher.email)

    def test_message_serialization_multiple(self):
        """여러 메시지 직렬화 테스트"""
        # 추가 피드백 생성
        feedback2 = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="두 번째 피드백"
        )

        feedbacks = [self.feedback, feedback2]
        serializer = MessageSerializer(feedbacks, many=True)
        data = serializer.data

        self.assertEqual(len(data), 2)
        self.assertEqual(data[0]["content"], "테스트 피드백 내용")
        self.assertEqual(data[1]["content"], "두 번째 피드백")

    def test_message_deserialization(self):
        """MessageSerializer 역직렬화(읽기 전용 중첩 포함)"""
        data = {
            "id": 10,
            "course_class": {"id": 1, "name": "C"},
            "student": {"id": 2, "display_name": "S", "email": "s@example.com"},
            "teacher": {"id": 3, "display_name": "T", "email": "t@example.com"},
            "content": "Ignored",
            "created_at": "2025-01-01T00:00:00Z",
        }
        serializer = MessageSerializer(data=data)
        # 전체가 read-only이므로 입력은 유효하지만 validated_data는 비어있음
        self.assertTrue(serializer.is_valid())
        self.assertEqual(serializer.validated_data, {"content": "Ignored"})


class MessageSendRequestSerializerTestCase(TestCase):
    """MessageSendRequestSerializer 테스트"""

    def setUp(self):
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.student = StudentFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)

    def test_valid_request_serialization(self):
        """유효한 요청 직렬화 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "유효한 피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertTrue(serializer.is_valid())
        self.assertEqual(serializer.validated_data["teacher_id"], self.teacher.id)
        self.assertEqual(serializer.validated_data["class_id"], self.course_class.id)
        self.assertEqual(serializer.validated_data["student_id"], self.student.id)
        self.assertEqual(serializer.validated_data["content"], "유효한 피드백 내용")

    def test_invalid_teacher_id(self):
        """잘못된 teacher_id 테스트"""
        data = {
            "teacher_id": 99999,  # 존재하지 않는 ID
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("teacher_id", serializer.errors)

    def test_student_as_teacher(self):
        """학생을 선생님으로 사용하는 경우 테스트"""
        data = {
            "teacher_id": self.student.id,  # 학생 ID를 teacher_id로 사용
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("teacher_id", serializer.errors)

    def test_invalid_class_id(self):
        """잘못된 class_id 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": 99999,  # 존재하지 않는 클래스 ID
            "student_id": self.student.id,
            "content": "피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("class_id", serializer.errors)

    def test_invalid_student_id(self):
        """잘못된 student_id 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": 99999,  # 존재하지 않는 학생 ID
            "content": "피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("student_id", serializer.errors)

    def test_non_student_as_student(self):
        """선생님을 학생으로 사용하는 경우 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": self.teacher.id,  # 선생님 ID를 student_id로 사용
            "content": "피드백 내용",
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("student_id", serializer.errors)

    def test_empty_content(self):
        """빈 내용 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "",  # 빈 내용
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("content", serializer.errors)

    def test_long_content(self):
        """너무 긴 내용 테스트"""
        data = {
            "teacher_id": self.teacher.id,
            "class_id": self.course_class.id,
            "student_id": self.student.id,
            "content": "x" * 5001,  # 5000자 초과
        }
        serializer = MessageSendRequestSerializer(data=data)

        self.assertFalse(serializer.is_valid())
        self.assertIn("content", serializer.errors)

    def test_missing_required_fields(self):
        """필수 필드 누락 테스트"""
        # teacher_id 누락
        data = {"class_id": self.course_class.id, "student_id": self.student.id, "content": "피드백 내용"}
        serializer = MessageSendRequestSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("teacher_id", serializer.errors)

        # class_id 누락
        data = {"teacher_id": self.teacher.id, "student_id": self.student.id, "content": "피드백 내용"}
        serializer = MessageSendRequestSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("class_id", serializer.errors)

        # student_id 누락
        data = {"teacher_id": self.teacher.id, "class_id": self.course_class.id, "content": "피드백 내용"}
        serializer = MessageSendRequestSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("student_id", serializer.errors)

        # content 누락
        data = {"teacher_id": self.teacher.id, "class_id": self.course_class.id, "student_id": self.student.id}
        serializer = MessageSendRequestSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("content", serializer.errors)


class SerializerIntegrationTestCase(TestCase):
    """Serializer 통합 테스트"""

    def setUp(self):
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.student = StudentFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)

    def test_complete_serialization_workflow(self):
        """완전한 직렬화 워크플로우 테스트"""
        # 1. 피드백 생성
        feedback = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="통합 테스트 피드백"
        )

        # 2. MessageSerializer로 직렬화
        message_serializer = MessageSerializer(feedback)
        message_data = message_serializer.data

        # 3. 각 중첩된 serializer가 올바르게 작동하는지 확인
        self.assertIn("course_class", message_data)
        self.assertIn("student", message_data)
        self.assertIn("teacher", message_data)

        # 4. 중첩된 데이터의 일관성 확인
        course_class_data = message_data["course_class"]
        student_data = message_data["student"]
        teacher_data = message_data["teacher"]

        # course_class 정보가 올바른지 확인
        self.assertEqual(course_class_data["id"], self.course_class.id)
        self.assertEqual(course_class_data["name"], self.course_class.name)

        # student 정보가 올바른지 확인
        self.assertEqual(student_data["id"], self.student.id)
        self.assertEqual(student_data["display_name"], self.student.display_name)
        self.assertEqual(student_data["email"], self.student.email)

        # teacher 정보가 올바른지 확인
        self.assertEqual(teacher_data["id"], self.teacher.id)
        self.assertEqual(teacher_data["display_name"], self.teacher.display_name)
        self.assertEqual(teacher_data["email"], self.teacher.email)

    def test_serializer_field_consistency(self):
        """Serializer 필드 일관성 테스트"""
        feedback = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="일관성 테스트"
        )

        # MessageSerializer 사용
        message_serializer = MessageSerializer(feedback)
        message_data = message_serializer.data

        # 각 중첩된 serializer의 필드가 예상대로 포함되어 있는지 확인
        course_class_fields = set(message_data["course_class"].keys())
        student_fields = set(message_data["student"].keys())
        teacher_fields = set(message_data["teacher"].keys())

        # CourseClassInfoSerializer 필드
        expected_course_class_fields = {"id", "name"}
        self.assertEqual(course_class_fields, expected_course_class_fields)

        # StudentInfoSerializer, TeacherInfoSerializer 필드
        expected_user_fields = {"id", "display_name", "email"}
        self.assertEqual(student_fields, expected_user_fields)
        self.assertEqual(teacher_fields, expected_user_fields)

    def test_serializer_performance(self):
        """Serializer 성능 테스트"""
        # 여러 피드백 생성
        feedbacks = []
        for i in range(10):
            feedback = TeacherFeedbackFactory(
                course_class=self.course_class,
                student=self.student,
                teacher=self.teacher,
                content=f"성능 테스트 피드백 {i}",
            )
            feedbacks.append(feedback)

        # bulk 직렬화 테스트
        serializer = MessageSerializer(feedbacks, many=True)
        data = serializer.data

        self.assertEqual(len(data), 10)

        # 각 피드백이 올바르게 직렬화되었는지 확인
        for i, feedback_data in enumerate(data):
            self.assertEqual(feedback_data["content"], f"성능 테스트 피드백 {i}")
            self.assertIn("course_class", feedback_data)
            self.assertIn("student", feedback_data)
            self.assertIn("teacher", feedback_data)
