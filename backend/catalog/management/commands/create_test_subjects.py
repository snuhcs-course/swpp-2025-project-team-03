from catalog.models import Subject
from django.core.management.base import BaseCommand


class Command(BaseCommand):
    help = "테스트용 과목(Subject) 데이터를 생성합니다."

    def handle(self, *args, **options):
        subjects_data = [
            {"name": "국어"},
            {"name": "영어"},
            {"name": "수학"},
            {"name": "과학"},
            {"name": "사회"},
        ]

        created_count = 0
        skipped_count = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 과목 생성 시작\n"))

        for data in subjects_data:
            subject, created = Subject.objects.get_or_create(name=data["name"])
            if created:
                created_count += 1
                self.stdout.write(self.style.SUCCESS(f"✓ 생성됨: {subject.name}"))
            else:
                skipped_count += 1
                self.stdout.write(self.style.WARNING(f"⚠ 이미 존재함: {subject.name}"))

        self.stdout.write(
            self.style.SUCCESS(
                f"\n과목 생성 완료! 새로 추가된 항목: {created_count}개, 이미 존재한 항목: {skipped_count}개"
            )
        )
