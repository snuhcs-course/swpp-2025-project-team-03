import random
from datetime import timedelta

from django.core.management.base import BaseCommand
from django.utils import timezone
from questions.models import Question
from submissions.models import Answer, PersonalAssignment

random.seed(42)


class Command(BaseCommand):
    help = "테스트용 답안(Answer) 데이터를 생성합니다."

    def handle(self, *args, **options):
        # PersonalAssignment ID 기준으로 답안 생성
        personal_assignment_ids = [1, 2, 3, 4]  # 예상되는 PersonalAssignment ID들
        total_created = 0
        total_skipped = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 Answer 생성 시작\n"))

        # 각 PersonalAssignment에 대해 답안 생성
        for pa_id in personal_assignment_ids:
            try:
                personal_assignment = PersonalAssignment.objects.get(id=pa_id)
            except PersonalAssignment.DoesNotExist:
                self.stdout.write(self.style.WARNING(f"⚠ 존재하지 않는 PersonalAssignment ID: {pa_id}"))
                continue

            # PersonalAssignment의 질문들 가져오기
            questions = Question.objects.filter(personal_assignment=personal_assignment).order_by("number")

            if not questions.exists():
                self.stdout.write(self.style.WARNING(f"⚠ PersonalAssignment ID {pa_id}에 질문이 없습니다."))
                continue

            created_count = 0
            skipped_count = 0

            # PersonalAssignment 상태에 따라 답안 생성
            if personal_assignment.status == PersonalAssignment.Status.GRADED:
                # GRADED: 모든 질문에 대해 완전한 답안 생성
                created_count, skipped_count = self._create_graded_answers(personal_assignment, questions)

            elif personal_assignment.status == PersonalAssignment.Status.SUBMITTED:
                # SUBMITTED: 모든 질문에 대해 답안 생성 (state와 eval_grade는 null)
                created_count, skipped_count = self._create_submitted_answers(personal_assignment, questions)

            elif personal_assignment.status == PersonalAssignment.Status.IN_PROGRESS:
                # IN_PROGRESS: Q1과 Q2만 답안 생성 (state와 eval_grade는 null)
                created_count, skipped_count = self._create_in_progress_answers(personal_assignment, questions)

            elif personal_assignment.status == PersonalAssignment.Status.NOT_STARTED:
                # NOT_STARTED: 답안 생성하지 않음
                self.stdout.write(
                    self.style.WARNING(f"⚠ NOT_STARTED 상태이므로 답안을 생성하지 않습니다: {personal_assignment}")
                )
                continue

            total_created += created_count
            total_skipped += skipped_count

            self.stdout.write(
                self.style.SUCCESS(
                    f"\n[PersonalAssignment ID {pa_id}] 새로 생성: {created_count}개, 이미 존재: {skipped_count}개\n"
                )
            )

        self.stdout.write(
            self.style.SUCCESS(
                f"Answer 생성 완료!\n총 새로 생성된 항목: {total_created}개, 이미 존재한 항목: {total_skipped}개"
            )
        )

    def _create_graded_answers(self, personal_assignment, questions):
        """GRADED 상태의 PersonalAssignment에 대해 완전한 답안 생성"""
        created_count = 0
        skipped_count = 0

        for question in questions:
            answer, created = Answer.objects.get_or_create(
                question=question,
                student=personal_assignment.student,
                defaults={
                    "started_at": personal_assignment.started_at or timezone.now() - timedelta(hours=2),
                    "submitted_at": personal_assignment.submitted_at or timezone.now() - timedelta(hours=1),
                    "state": self._get_random_answer_state(),
                    "text_answer": self._get_answer_text(question),
                    "eval_grade": self._get_random_grade(),
                },
            )

            if created:
                created_count += 1
                self.stdout.write(
                    self.style.SUCCESS(
                        f"✓ 생성됨 (GRADED): {personal_assignment.student} - Q{question.number} [{answer.state}]"
                    )
                )
            else:
                skipped_count += 1
                self.stdout.write(
                    self.style.WARNING(f"⚠ 이미 존재함: {personal_assignment.student} - Q{question.number}")
                )

        return created_count, skipped_count

    def _create_submitted_answers(self, personal_assignment, questions):
        """SUBMITTED 상태의 PersonalAssignment에 대해 답안 생성 (state와 eval_grade는 null)"""
        created_count = 0
        skipped_count = 0

        for question in questions:
            answer, created = Answer.objects.get_or_create(
                question=question,
                student=personal_assignment.student,
                defaults={
                    "started_at": personal_assignment.started_at or timezone.now() - timedelta(hours=1),
                    "submitted_at": personal_assignment.submitted_at or timezone.now(),
                    "state": None,  # SUBMITTED 상태에서는 아직 채점되지 않음
                    "text_answer": self._get_answer_text(question),
                    "eval_grade": None,  # SUBMITTED 상태에서는 아직 채점되지 않음
                },
            )

            if created:
                created_count += 1
                self.stdout.write(
                    self.style.SUCCESS(f"✓ 생성됨 (SUBMITTED): {personal_assignment.student} - Q{question.number}")
                )
            else:
                skipped_count += 1
                self.stdout.write(
                    self.style.WARNING(f"⚠ 이미 존재함: {personal_assignment.student} - Q{question.number}")
                )

        return created_count, skipped_count

    def _create_in_progress_answers(self, personal_assignment, questions):
        """IN_PROGRESS 상태의 PersonalAssignment에 대해 Q1과 Q2만 답안 생성"""
        created_count = 0
        skipped_count = 0

        # Q1과 Q2만 처리 (number가 1, 2인 질문들)
        questions_to_process = questions.filter(number__in=[1, 2])

        for question in questions_to_process:
            answer, created = Answer.objects.get_or_create(
                question=question,
                student=personal_assignment.student,
                defaults={
                    "started_at": personal_assignment.started_at or timezone.now() - timedelta(minutes=30),
                    "submitted_at": None,  # 아직 제출하지 않음
                    "state": None,  # IN_PROGRESS 상태에서는 아직 채점되지 않음
                    "text_answer": self._get_answer_text(question),
                    "eval_grade": None,  # IN_PROGRESS 상태에서는 아직 채점되지 않음
                },
            )

            if created:
                created_count += 1
                self.stdout.write(
                    self.style.SUCCESS(f"✓ 생성됨 (IN_PROGRESS): {personal_assignment.student} - Q{question.number}")
                )
            else:
                skipped_count += 1
                self.stdout.write(
                    self.style.WARNING(f"⚠ 이미 존재함: {personal_assignment.student} - Q{question.number}")
                )

        return created_count, skipped_count

    def _get_answer_text(self, question):
        """질문에 대한 적절한 답안 텍스트 생성"""
        # 질문 내용에 따라 적절한 답안 생성
        content = question.content.lower()

        if "호흡" in content and "가슴" in content:
            return "횡격막과 늑간근이 수축하여 흉강의 부피가 증가하기 때문입니다."
        elif "신장" in content and "기능" in content:
            return "혈액을 여과하여 노폐물을 제거하고 체내 수분과 전해질 균형을 유지합니다."
        elif "폐" in content and "가스" in content:
            return "폐포에서 산소는 혈액으로 확산되고, 이산화탄소는 혈액에서 폐포로 확산됩니다."
        elif "f(5)" in content:
            return "13"
        elif "기울기" in content:
            return "3"
        elif "주어" in content:
            return "The students"
        elif "반의어" in content and "happy" in content:
            return "sad"
        elif "과거형" in content:
            return "I went to school every day."
        else:
            return "학생이 작성한 답안입니다."

    def _get_random_answer_state(self):
        """랜덤한 답안 상태 반환"""
        return random.choice([Answer.State.CORRECT, Answer.State.INCORRECT])

    def _get_random_grade(self):
        """랜덤한 점수 반환 (0.0 ~ 1.0)"""
        return round(random.uniform(0.3, 1.0), 2)
