from assignments.models import Assignment
from courses.models import Enrollment
from django.core.management.base import BaseCommand
from submissions.models import PersonalAssignment


class Command(BaseCommand):
    help = "테스트용 개인 과제(PersonalAssignment)를 생성합니다."

    def handle(self, *args, **options):
        assignment_ids = [1, 2, 3]
        total_created = 0
        total_skipped = 0

        # 상태 순서 정의
        status_cycle = [
            PersonalAssignment.Status.SUBMITTED,
            PersonalAssignment.Status.IN_PROGRESS,
            PersonalAssignment.Status.NOT_STARTED,
            PersonalAssignment.Status.SUBMITTED,
        ]
        status_index = 0  # 전체 과제 간 순환용 인덱스

        self.stdout.write(self.style.HTTP_INFO("개인 과제(PersonalAssignment) 생성 시작\n"))

        for assignment_id in assignment_ids:
            try:
                assignment = Assignment.objects.get(id=assignment_id)
            except Assignment.DoesNotExist:
                self.stdout.write(self.style.WARNING(f"⚠ 존재하지 않는 과제 ID: {assignment_id}\n"))
                continue

            enrollments = (
                Enrollment.objects.filter(course_class=assignment.course_class).select_related("student").all()
            )

            if not enrollments.exists():
                self.stdout.write(self.style.WARNING(f"⚠ 수강 중인 학생이 없습니다: 과제 ID {assignment_id}\n"))
                continue

            created_count = 0
            skipped_count = 0

            for enrollment in enrollments:
                student = enrollment.student
                # 전역 순서에 따라 상태 부여
                status = status_cycle[status_index % len(status_cycle)]
                status_index += 1

                obj, created = PersonalAssignment.objects.get_or_create(
                    student=student,
                    assignment=assignment,
                    defaults={
                        "status": status,
                        "started_at": None,
                        "submitted_at": None,
                    },
                )

                if created:
                    created_count += 1
                    self.stdout.write(
                        self.style.SUCCESS(
                            f"✓ 생성됨: 학생 {student.id} → 과제 {assignment_id} ({assignment.title}) [{status}]"
                        )
                    )
                else:
                    skipped_count += 1
                    self.stdout.write(
                        self.style.WARNING(
                            f"⚠ 이미 존재함: 학생 {student.id} → 과제 {assignment_id} ({assignment.title}) [{obj.status}]"
                        )
                    )

            total_created += created_count
            total_skipped += skipped_count

            self.stdout.write(
                self.style.SUCCESS(
                    f"\n[과제 ID {assignment_id}] 새로 생성: {created_count}개, 이미 존재: {skipped_count}개\n"
                )
            )

        self.stdout.write(
            self.style.SUCCESS(
                f"PersonalAssignment 생성 완료!\n총 새로 생성된 항목: {total_created}개, 이미 존재한 항목: {total_skipped}개"
            )
        )
