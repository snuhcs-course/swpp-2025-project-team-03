from datetime import timedelta

from courses.models import CourseClass
from django.core.management.base import BaseCommand
from django.utils import timezone


class Command(BaseCommand):
    help = "테스트용 강좌 데이터를 생성합니다. (teacher_id=1, subject_id=1 기준)"

    def handle(self, *args, **options):
        teacher_id = 1

        # 테스트용 수업 목록
        classes_data = [
            {
                "name": "과학 기초반",
                "description": "중등 과학을 예습하는 반입니다.",
                "subject_id": 4,  # 과학
                "start_date": timezone.now(),
                "end_date": timezone.now() + timedelta(days=90),
            },
            {
                "name": "초등 수학 심화반",
                "description": "초등 수학을 심화 학습합니다.",
                "subject_id": 3,  # 수학
                "start_date": timezone.now() + timedelta(days=5),
                "end_date": timezone.now() + timedelta(days=100),
            },
            {
                "name": "영어 독해 집중반",
                "description": "영어 지문의 독해 능력을 집중적으로 향상시키는 반입니다.",
                "subject_id": 2,  # 영어
                "start_date": timezone.now() + timedelta(days=10),
                "end_date": timezone.now() + timedelta(days=120),
            },
        ]

        created_count = 0
        skipped_count = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 CourseClass 생성 시작\n"))

        for data in classes_data:
            course, created = CourseClass.objects.get_or_create(
                name=data["name"],
                defaults={
                    "teacher_id": teacher_id,
                    "subject_id": data["subject_id"],
                    "description": data["description"],
                    "start_date": data["start_date"],
                    "end_date": data["end_date"],
                },
            )
            if created:
                created_count += 1
                self.stdout.write(self.style.SUCCESS(f"✓ 생성됨: {course.name}"))
            else:
                skipped_count += 1
                self.stdout.write(self.style.WARNING(f"⚠ 이미 존재함: {course.name}"))

        self.stdout.write(
            self.style.SUCCESS(
                f"\nCourseClass 생성 완료! 새로 추가된 항목: {created_count}개, 이미 존재한 항목: {skipped_count}개"
            )
        )
