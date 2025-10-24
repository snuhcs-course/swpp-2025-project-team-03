from courses.models import Enrollment
from django.core.management.base import BaseCommand


class Command(BaseCommand):
    help = "테스트용 수강 등록(Enrollment) 데이터를 생성합니다."

    def handle(self, *args, **options):
        # 등록할 수강 정보
        enrollments_data = [
            {"student_id": 2, "course_class_id": 1},  # 1번 학생 - 1번 수업
            {"student_id": 3, "course_class_id": 1},  # 2번 학생 - 1번 수업
            {"student_id": 2, "course_class_id": 2},  # 1번 학생 - 2번 수업
            {"student_id": 3, "course_class_id": 3},  # 2번 학생 - 3번 수업
        ]

        created_count = 0
        skipped_count = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 Enrollment 생성 시작\n"))

        for data in enrollments_data:
            enrollment, created = Enrollment.objects.get_or_create(
                student_id=data["student_id"],
                course_class_id=data["course_class_id"],
            )
            if created:
                created_count += 1
                self.stdout.write(
                    self.style.SUCCESS(f"✓ 등록됨: 학생 {data['student_id']} → 수업 {data['course_class_id']} ")
                )
            else:
                skipped_count += 1
                self.stdout.write(
                    self.style.WARNING(
                        f"⚠ 이미 등록되어 있음: 학생 {data['student_id']} → 수업 {data['course_class_id']} "
                    )
                )

        self.stdout.write(
            self.style.SUCCESS(
                f"\nEnrollment 생성 완료! 새로 등록된 항목: {created_count}개, 이미 등록된 항목: {skipped_count}개"
            )
        )
