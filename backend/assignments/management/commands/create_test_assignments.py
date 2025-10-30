from datetime import timedelta

from assignments.models import Assignment
from django.core.management.base import BaseCommand
from django.utils import timezone


class Command(BaseCommand):
    help = "테스트용 과제(Assignment) 데이터를 생성합니다."

    def handle(self, *args, **options):
        # course_class_id 기준 더미 데이터
        assignments_data = [
            {
                "course_class": 1,  # 과학 기초반
                "subject_id": 4,  # 과학
                "title": "호흡과 배설 단원 과제",
                "description": "호흡 운동의 원리와 배설의 과정의 이해도를 평가하는 과제입니다.",
                "total_questions": 3,
                "visible_from": timezone.now(),
                "due_at": timezone.now() + timedelta(days=2),
                "grade": "중학교 2학년",
            },
            {
                "course_class": 2,  # 초등 수학 심화반
                "subject_id": 3,  # 수학
                "title": "함수의 개념 복습 과제",
                "description": "함수의 정의를 복습하는 과제입니다.",
                "total_questions": 2,
                "visible_from": timezone.now(),
                "due_at": timezone.now() + timedelta(days=10),
                "grade": "초등학교 6학년",
            },
            {
                "course_class": 3,
                "subject_id": 2,  # 영어
                "title": "Reading Practice 1-A",
                "description": "영어 독해 훈련 과제입니다.",
                "total_questions": 3,
                "visible_from": timezone.now(),
                "due_at": timezone.now() + timedelta(days=5),
                "grade": "중학교 3학년",
            },
        ]

        created_count = 0
        skipped_count = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 Assignment 생성 시작\n"))

        for data in assignments_data:
            assignment, created = Assignment.objects.get_or_create(
                course_class_id=data["course_class"],
                subject_id=data["subject_id"],
                title=data["title"],
                total_questions=data["total_questions"],
                grade=data["grade"],
                defaults={
                    "description": data["description"],
                    "visible_from": data["visible_from"],
                    "due_at": data["due_at"],
                },
            )

            if created:
                created_count += 1
                self.stdout.write(self.style.SUCCESS(f"✓ 생성됨: {assignment.title}"))
            else:
                skipped_count += 1
                self.stdout.write(self.style.WARNING(f"⚠ 이미 존재함: {assignment.title}"))

        self.stdout.write(
            self.style.SUCCESS(
                f"\nAssignment 생성 완료! 새로 추가된 항목: {created_count}개, 이미 존재한 항목: {skipped_count}개"
            )
        )
