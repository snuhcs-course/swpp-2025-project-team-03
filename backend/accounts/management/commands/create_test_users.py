from accounts.models import Account
from django.core.management.base import BaseCommand


class Command(BaseCommand):
    help = "테스트용 선생님과 학생 계정 생성"

    def handle(self, *args, **kwargs):
        # 선생님 계정 생성
        teacher_email = "teacher@voicetutor.com"
        if not Account.objects.filter(email=teacher_email).exists():
            teacher = Account.objects.create_user(
                email=teacher_email,
                password="teacher123",
                display_name="김선생",
                is_student=False,
                is_staff=False,
                is_superuser=False,
            )
            self.stdout.write(self.style.SUCCESS(f"✓ 선생님 계정 생성: {teacher_email} / teacher123"))
        else:
            self.stdout.write(self.style.WARNING(f"선생님 계정이 이미 존재합니다: {teacher_email}"))

        # 학생 계정 생성
        student_email = "student@voicetutor.com"
        if not Account.objects.filter(email=student_email).exists():
            student = Account.objects.create_user(
                email=student_email,
                password="student123",
                display_name="이학생",
                is_student=True,
                is_staff=False,
                is_superuser=False,
            )
            self.stdout.write(self.style.SUCCESS(f"✓ 학생 계정 생성: {student_email} / student123"))
        else:
            self.stdout.write(self.style.WARNING(f"학생 계정이 이미 존재합니다: {student_email}"))

        self.stdout.write(self.style.SUCCESS("\n테스트 계정 생성 완료!"))
        self.stdout.write("선생님: teacher@voicetutor.com / teacher123")
        self.stdout.write("학생: student@voicetutor.com / student123")
