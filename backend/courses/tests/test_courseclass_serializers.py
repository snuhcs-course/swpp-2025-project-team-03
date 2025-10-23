import pytest
from courses.models import Enrollment
from courses.serializers import CourseClassSerializer
from django.contrib.auth import get_user_model

from .test_courseclass_factories import (
    CourseClassFactory,
    EnrollmentFactory,
    StudentFactory,
    SubjectFactory,
    TeacherFactory,
)

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest courses/tests/test_courseclass_serializers.py -v


@pytest.fixture
def teacher():
    return TeacherFactory()


@pytest.fixture
def student():
    return StudentFactory()


@pytest.fixture
def subject():
    return SubjectFactory()


@pytest.fixture
def course_class(teacher, subject):
    return CourseClassFactory(teacher=teacher, subject=subject)


class TestCourseClassSerializer:
    """CourseClass Serializer 테스트"""

    def test_serialize_course_class(self, course_class):
        """CourseClass 직렬화 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # 필수 필드들 확인
        assert "id" in data
        assert "name" in data
        assert "description" in data
        assert "subject" in data
        assert "teacher_name" in data
        assert "start_date" in data
        assert "end_date" in data
        assert "student_count" in data
        assert "created_at" in data

        # 값 확인
        assert data["id"] == course_class.id
        assert data["name"] == course_class.name
        assert data["description"] == course_class.description
        assert data["teacher_name"] == course_class.teacher.display_name

    def test_serialize_course_class_with_subject(self, course_class):
        """Subject 정보가 포함된 직렬화 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # subject 필드 확인
        assert "subject" in data
        subject_data = data["subject"]
        assert "id" in subject_data
        assert "name" in subject_data
        assert subject_data["id"] == course_class.subject.id
        assert subject_data["name"] == course_class.subject.name

    def test_serialize_course_class_student_count(self, course_class, student):
        """학생 수 계산 테스트"""
        # 학생 등록
        EnrollmentFactory(student=student, course_class=course_class)

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["student_count"] == 1

    def test_serialize_course_class_multiple_students(self, course_class):
        """여러 학생이 등록된 클래스의 학생 수 테스트"""
        # 여러 학생 등록
        student1 = StudentFactory()
        student2 = StudentFactory()
        student3 = StudentFactory()

        EnrollmentFactory(student=student1, course_class=course_class)
        EnrollmentFactory(student=student2, course_class=course_class)
        EnrollmentFactory(student=student3, course_class=course_class)

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["student_count"] == 3

    def test_serialize_course_class_no_students(self, course_class):
        """학생이 없는 클래스의 학생 수 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["student_count"] == 0

    def test_serialize_course_class_dropped_students(self, course_class, student):
        """드롭된 학생은 카운트에서 제외되는지 테스트"""
        # 등록된 학생
        enrolled_student = StudentFactory()
        EnrollmentFactory(student=enrolled_student, course_class=course_class)

        # 드롭된 학생
        dropped_student = StudentFactory()
        EnrollmentFactory(student=dropped_student, course_class=course_class, status=Enrollment.Status.DROPPED)

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # 드롭된 학생은 카운트에서 제외되어야 함
        assert data["student_count"] == 1

    def test_serialize_course_class_teacher_name(self, course_class):
        """선생님 이름 직렬화 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["teacher_name"] == course_class.teacher.display_name

    def test_serialize_course_class_dates(self, course_class):
        """날짜 필드 직렬화 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # 날짜 필드가 ISO 형식으로 직렬화되는지 확인
        assert "start_date" in data
        assert "end_date" in data
        assert data["start_date"] is not None
        assert data["end_date"] is not None

    def test_serialize_multiple_course_classes(self, teacher, subject):
        """여러 클래스 직렬화 테스트"""
        class1 = CourseClassFactory(teacher=teacher, subject=subject)
        class2 = CourseClassFactory(teacher=teacher, subject=subject)
        class3 = CourseClassFactory(teacher=teacher, subject=subject)

        classes = [class1, class2, class3]
        serializer = CourseClassSerializer(classes, many=True)
        data = serializer.data

        assert len(data) == 3
        assert data[0]["id"] == class1.id
        assert data[1]["id"] == class2.id
        assert data[2]["id"] == class3.id

    def test_serialize_course_class_read_only_fields(self, course_class):
        """읽기 전용 필드들이 올바르게 직렬화되는지 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # 읽기 전용 필드들 확인
        assert "id" in data  # 자동 생성 필드
        assert "created_at" in data  # 자동 생성 필드
        assert "subject" in data  # read_only=True
        assert "teacher_name" in data  # read_only=True
        assert "student_count" in data  # SerializerMethodField

    def test_serialize_course_class_field_types(self, course_class):
        """필드 타입 확인 테스트"""
        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # 각 필드의 타입 확인
        assert isinstance(data["id"], int)
        assert isinstance(data["name"], str)
        assert isinstance(data["description"], str)
        assert isinstance(data["subject"], dict)
        assert isinstance(data["teacher_name"], str)
        assert isinstance(data["student_count"], int)
        assert isinstance(data["created_at"], str)  # ISO 형식 문자열

    def test_serialize_course_class_empty_description(self, teacher, subject):
        """빈 설명이 있는 클래스 직렬화 테스트"""
        course_class = CourseClassFactory(teacher=teacher, subject=subject, description="")

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["description"] == ""

    def test_serialize_course_class_long_description(self, teacher, subject):
        """긴 설명이 있는 클래스 직렬화 테스트"""
        long_description = (
            "This is a very long description that might test the serializer's ability to handle longer text content. "
            * 10
        )

        course_class = CourseClassFactory(teacher=teacher, subject=subject, description=long_description)

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["description"] == long_description

    def test_serialize_course_class_with_special_characters(self, teacher, subject):
        """특수 문자가 포함된 클래스 직렬화 테스트"""
        special_name = "Class with 특수문자 & Symbols! @#$%"
        special_description = "Description with 특수문자 & Symbols! @#$%"

        course_class = CourseClassFactory(
            teacher=teacher, subject=subject, name=special_name, description=special_description
        )

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        assert data["name"] == special_name
        assert data["description"] == special_description


class TestCourseClassSerializerValidation:
    """CourseClass Serializer 유효성 검사 테스트"""

    def test_serialize_invalid_course_class(self):
        """잘못된 CourseClass 객체 직렬화 테스트"""
        # None 객체로 직렬화 시도
        serializer = CourseClassSerializer(None)

        # 이 경우 빈 데이터가 반환되어야 함
        data = serializer.data
        assert isinstance(data, dict)
        # 기본값들이 비어있거나 None이어야 함
        assert data["name"] == ""
        assert data["description"] == ""
        assert data["start_date"] is None
        assert data["end_date"] is None

    def test_serialize_course_class_with_missing_subject(self, teacher):
        """과목이 없는 클래스 직렬화 테스트"""
        course_class = CourseClassFactory(teacher=teacher)
        # subject를 None으로 설정 (실제로는 불가능하지만 테스트용)
        course_class.subject = None

        serializer = CourseClassSerializer(course_class)
        data = serializer.data

        # subject가 None이어야 함
        assert data["subject"] is None
