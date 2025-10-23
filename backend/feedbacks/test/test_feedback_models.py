from datetime import timedelta

from django.test import TestCase
from django.utils import timezone
from feedbacks.models import TeacherFeedback
from feedbacks.test.test_factories import (
    CourseClassFactory,
    StudentFactory,
    SubjectFactory,
    TeacherFactory,
    TeacherFeedbackFactory,
)

# pytest 실행 예시
# pytest feedbacks/test/test_feedback_models.py -v


class TeacherFeedbackModelTestCase(TestCase):
    """TeacherFeedback 모델 테스트"""

    def setUp(self):
        """테스트 데이터 설정"""
        self.subject = SubjectFactory()
        self.teacher = TeacherFactory()
        self.student = StudentFactory()
        self.course_class = CourseClassFactory(teacher=self.teacher, subject=self.subject)

    def test_teacher_feedback_creation(self):
        """TeacherFeedback 생성 테스트"""
        feedback = TeacherFeedback.objects.create(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="테스트 피드백 내용"
        )

        self.assertEqual(feedback.course_class, self.course_class)
        self.assertEqual(feedback.student, self.student)
        self.assertEqual(feedback.teacher, self.teacher)
        self.assertEqual(feedback.content, "테스트 피드백 내용")
        self.assertIsNotNone(feedback.created_at)

    def test_teacher_feedback_str_representation(self):
        """TeacherFeedback 문자열 표현 테스트"""
        feedback = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="문자열 테스트"
        )

        expected_str = f"{self.teacher} → {self.student} ({self.course_class})"
        self.assertEqual(str(feedback), expected_str)

    def test_teacher_feedback_auto_created_at(self):
        """TeacherFeedback 자동 생성 시간 테스트"""
        before_creation = timezone.now()

        feedback = TeacherFeedback.objects.create(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="시간 테스트"
        )

        after_creation = timezone.now()

        self.assertGreaterEqual(feedback.created_at, before_creation)
        self.assertLessEqual(feedback.created_at, after_creation)

    def test_teacher_feedback_blank_content(self):
        """TeacherFeedback 빈 내용 테스트"""
        feedback = TeacherFeedback.objects.create(
            course_class=self.course_class,
            student=self.student,
            teacher=self.teacher,
            content="",  # 빈 내용
        )

        self.assertEqual(feedback.content, "")
        self.assertIsNotNone(feedback.created_at)

    def test_teacher_feedback_relationships(self):
        """TeacherFeedback 관계 테스트"""
        feedback = TeacherFeedbackFactory(course_class=self.course_class, student=self.student, teacher=self.teacher)

        # ForeignKey 관계 확인
        self.assertEqual(feedback.course_class, self.course_class)
        self.assertEqual(feedback.student, self.student)
        self.assertEqual(feedback.teacher, self.teacher)

        # 역참조 관계 확인
        self.assertIn(feedback, self.course_class.teacher_feedbacks.all())
        self.assertIn(feedback, self.student.received_feedbacks.all())
        self.assertIn(feedback, self.teacher.given_feedbacks.all())

    def test_teacher_feedback_cascade_delete(self):
        """TeacherFeedback CASCADE 삭제 테스트"""
        feedback = TeacherFeedbackFactory(course_class=self.course_class, student=self.student, teacher=self.teacher)

        feedback_id = feedback.id

        # course_class 삭제 시 피드백도 삭제되는지 확인
        self.course_class.delete()
        self.assertFalse(TeacherFeedback.objects.filter(id=feedback_id).exists())

        # 새로운 피드백 생성
        feedback2 = TeacherFeedbackFactory(
            course_class=CourseClassFactory(), student=self.student, teacher=self.teacher
        )
        feedback2_id = feedback2.id

        # student 삭제 시 피드백도 삭제되는지 확인
        self.student.delete()
        self.assertFalse(TeacherFeedback.objects.filter(id=feedback2_id).exists())

        # 새로운 피드백 생성
        feedback3 = TeacherFeedbackFactory(
            course_class=CourseClassFactory(), student=StudentFactory(), teacher=self.teacher
        )
        feedback3_id = feedback3.id

        # teacher 삭제 시 피드백도 삭제되는지 확인
        self.teacher.delete()
        self.assertFalse(TeacherFeedback.objects.filter(id=feedback3_id).exists())

    def test_teacher_feedback_unique_constraints(self):
        """TeacherFeedback 고유 제약 조건 테스트"""
        # 동일한 course_class, student, teacher로 여러 피드백 생성 가능
        feedback1 = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="첫 번째 피드백"
        )

        feedback2 = TeacherFeedbackFactory(
            course_class=self.course_class, student=self.student, teacher=self.teacher, content="두 번째 피드백"
        )

        # 두 피드백이 모두 생성되었는지 확인
        self.assertNotEqual(feedback1.id, feedback2.id)
        self.assertEqual(
            TeacherFeedback.objects.filter(
                course_class=self.course_class, student=self.student, teacher=self.teacher
            ).count(),
            2,
        )

    def test_teacher_feedback_indexes(self):
        """TeacherFeedback 인덱스 테스트"""
        # 인덱스가 올바르게 설정되어 있는지 확인
        from django.db import connection

        with connection.cursor() as cursor:
            # PostgreSQL의 경우
            if "postgresql" in connection.vendor:
                cursor.execute(
                    """
                    SELECT indexname FROM pg_indexes 
                    WHERE tablename = 'teacher_feedback'
                """
                )
                indexes = [row[0] for row in cursor.fetchall()]

                # 예상되는 인덱스들이 있는지 확인
                self.assertTrue(any("course_class" in idx for idx in indexes))
                self.assertTrue(any("student" in idx for idx in indexes))
                self.assertTrue(any("teacher" in idx for idx in indexes))

    def test_teacher_feedback_db_table(self):
        """TeacherFeedback DB 테이블명 테스트"""
        self.assertEqual(TeacherFeedback._meta.db_table, "teacher_feedback")

    def test_teacher_feedback_meta_options(self):
        """TeacherFeedback Meta 옵션 테스트"""
        meta = TeacherFeedback._meta

        # db_table 확인
        self.assertEqual(meta.db_table, "teacher_feedback")

        # indexes 확인
        self.assertTrue(len(meta.indexes) >= 3)

        # 인덱스 필드 확인
        index_fields = [index.fields for index in meta.indexes]
        self.assertIn(["course_class"], index_fields)
        self.assertIn(["student"], index_fields)
        self.assertIn(["teacher"], index_fields)


class TeacherFeedbackModelIntegrationTestCase(TestCase):
    """TeacherFeedback 모델 통합 테스트"""

    def setUp(self):
        """통합 테스트 데이터 설정"""
        self.subject = SubjectFactory()
        self.teacher1 = TeacherFactory()
        self.teacher2 = TeacherFactory()
        self.student1 = StudentFactory()
        self.student2 = StudentFactory()

        self.class1 = CourseClassFactory(teacher=self.teacher1, subject=self.subject)
        self.class2 = CourseClassFactory(teacher=self.teacher2, subject=self.subject)

    def test_multiple_feedbacks_same_class(self):
        """같은 클래스의 여러 피드백 테스트"""
        # 같은 클래스에 여러 피드백 생성
        feedback1 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student1, teacher=self.teacher1, content="첫 번째 피드백"
        )

        feedback2 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student2, teacher=self.teacher1, content="두 번째 피드백"
        )

        # 클래스의 모든 피드백 조회
        class_feedbacks = TeacherFeedback.objects.filter(course_class=self.class1)
        self.assertEqual(class_feedbacks.count(), 2)

        # 각 피드백이 올바른 관계를 가지는지 확인
        self.assertEqual(feedback1.course_class, self.class1)
        self.assertEqual(feedback2.course_class, self.class1)
        self.assertEqual(feedback1.teacher, self.teacher1)
        self.assertEqual(feedback2.teacher, self.teacher1)

    def test_multiple_feedbacks_same_student(self):
        """같은 학생의 여러 피드백 테스트"""
        # 같은 학생에게 여러 피드백 생성
        feedback1 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student1, teacher=self.teacher1, content="첫 번째 피드백"
        )

        feedback2 = TeacherFeedbackFactory(
            course_class=self.class2, student=self.student1, teacher=self.teacher2, content="두 번째 피드백"
        )

        # 학생이 받은 모든 피드백 조회
        student_feedbacks = TeacherFeedback.objects.filter(student=self.student1)
        self.assertEqual(student_feedbacks.count(), 2)

        # 각 피드백이 올바른 관계를 가지는지 확인
        self.assertEqual(feedback1.student, self.student1)
        self.assertEqual(feedback2.student, self.student1)
        self.assertNotEqual(feedback1.teacher, feedback2.teacher)
        self.assertNotEqual(feedback1.course_class, feedback2.course_class)

    def test_multiple_feedbacks_same_teacher(self):
        """같은 선생님의 여러 피드백 테스트"""
        # 같은 선생님이 여러 피드백 생성
        feedback1 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student1, teacher=self.teacher1, content="첫 번째 피드백"
        )

        feedback2 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student2, teacher=self.teacher1, content="두 번째 피드백"
        )

        # 선생님이 보낸 모든 피드백 조회
        teacher_feedbacks = TeacherFeedback.objects.filter(teacher=self.teacher1)
        self.assertEqual(teacher_feedbacks.count(), 2)

        # 각 피드백이 올바른 관계를 가지는지 확인
        self.assertEqual(feedback1.teacher, self.teacher1)
        self.assertEqual(feedback2.teacher, self.teacher1)
        self.assertNotEqual(feedback1.student, feedback2.student)

    def test_feedback_ordering(self):
        """피드백 정렬 테스트"""
        # 시간 간격을 두고 피드백 생성
        feedback1 = TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="첫 번째 피드백",
            created_at=timezone.now() - timedelta(hours=2),
        )

        feedback2 = TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="두 번째 피드백",
            created_at=timezone.now() - timedelta(hours=1),
        )

        feedback3 = TeacherFeedbackFactory(
            course_class=self.class1,
            student=self.student1,
            teacher=self.teacher1,
            content="세 번째 피드백",
            created_at=timezone.now(),
        )

        # 최신순 정렬
        ordered_feedbacks = TeacherFeedback.objects.filter(course_class=self.class1).order_by("-created_at")

        self.assertEqual(ordered_feedbacks[0], feedback3)
        self.assertEqual(ordered_feedbacks[1], feedback2)
        self.assertEqual(ordered_feedbacks[2], feedback1)

        # 오래된순 정렬
        ordered_feedbacks_asc = TeacherFeedback.objects.filter(course_class=self.class1).order_by("created_at")

        self.assertEqual(ordered_feedbacks_asc[0], feedback1)
        self.assertEqual(ordered_feedbacks_asc[1], feedback2)
        self.assertEqual(ordered_feedbacks_asc[2], feedback3)

    def test_feedback_filtering(self):
        """피드백 필터링 테스트"""
        # 다양한 조건의 피드백 생성
        feedback1 = TeacherFeedbackFactory(
            course_class=self.class1, student=self.student1, teacher=self.teacher1, content="클래스1 학생1 피드백"
        )

        TeacherFeedbackFactory(
            course_class=self.class1, student=self.student2, teacher=self.teacher1, content="클래스1 학생2 피드백"
        )

        TeacherFeedbackFactory(
            course_class=self.class2, student=self.student1, teacher=self.teacher2, content="클래스2 학생1 피드백"
        )

        # 클래스별 필터링
        class1_feedbacks = TeacherFeedback.objects.filter(course_class=self.class1)
        self.assertEqual(class1_feedbacks.count(), 2)

        class2_feedbacks = TeacherFeedback.objects.filter(course_class=self.class2)
        self.assertEqual(class2_feedbacks.count(), 1)

        # 학생별 필터링
        student1_feedbacks = TeacherFeedback.objects.filter(student=self.student1)
        self.assertEqual(student1_feedbacks.count(), 2)

        student2_feedbacks = TeacherFeedback.objects.filter(student=self.student2)
        self.assertEqual(student2_feedbacks.count(), 1)

        # 선생님별 필터링
        teacher1_feedbacks = TeacherFeedback.objects.filter(teacher=self.teacher1)
        self.assertEqual(teacher1_feedbacks.count(), 2)

        teacher2_feedbacks = TeacherFeedback.objects.filter(teacher=self.teacher2)
        self.assertEqual(teacher2_feedbacks.count(), 1)

        # 복합 필터링
        specific_feedback = TeacherFeedback.objects.filter(
            course_class=self.class1, student=self.student1, teacher=self.teacher1
        )
        self.assertEqual(specific_feedback.count(), 1)
        self.assertEqual(specific_feedback.first(), feedback1)

    def test_feedback_content_variations(self):
        """피드백 내용 변형 테스트"""
        # 다양한 내용의 피드백 생성
        contents = [
            "짧은 피드백",
            "이것은 좀 더 긴 피드백 내용입니다. 여러 줄에 걸쳐 작성된 피드백입니다.",
            "특수문자 포함 피드백: !@#$%^&*()",
            "한글 피드백 내용입니다.",
            "English feedback content.",
            "숫자 포함 피드백: 12345",
            "줄바꿈 포함 피드백\n두 번째 줄",
            "   공백 포함 피드백   ",
        ]

        feedbacks = []
        for i, content in enumerate(contents):
            feedback = TeacherFeedbackFactory(
                course_class=self.class1, student=self.student1, teacher=self.teacher1, content=content
            )
            feedbacks.append(feedback)

        # 모든 피드백이 올바르게 생성되었는지 확인
        self.assertEqual(len(feedbacks), len(contents))

        for i, feedback in enumerate(feedbacks):
            self.assertEqual(feedback.content, contents[i])

    def test_feedback_relationships_integrity(self):
        """피드백 관계 무결성 테스트"""
        feedback = TeacherFeedbackFactory(course_class=self.class1, student=self.student1, teacher=self.teacher1)

        # 관계가 올바르게 설정되었는지 확인
        self.assertEqual(feedback.course_class.teacher, self.teacher1)
        self.assertEqual(feedback.student.is_student, True)
        self.assertEqual(feedback.teacher.is_student, False)

        # 역참조가 올바르게 작동하는지 확인
        self.assertIn(feedback, self.class1.teacher_feedbacks.all())
        self.assertIn(feedback, self.student1.received_feedbacks.all())
        self.assertIn(feedback, self.teacher1.given_feedbacks.all())

        # 관계를 통한 쿼리가 올바르게 작동하는지 확인
        class_feedbacks = self.class1.teacher_feedbacks.all()
        student_feedbacks = self.student1.received_feedbacks.all()
        teacher_feedbacks = self.teacher1.given_feedbacks.all()

        self.assertEqual(class_feedbacks.count(), 1)
        self.assertEqual(student_feedbacks.count(), 1)
        self.assertEqual(teacher_feedbacks.count(), 1)

        self.assertEqual(class_feedbacks.first(), feedback)
        self.assertEqual(student_feedbacks.first(), feedback)
        self.assertEqual(teacher_feedbacks.first(), feedback)
