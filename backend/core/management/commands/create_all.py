from django.core.management import call_command
from django.core.management.base import BaseCommand


class Command(BaseCommand):
    help = "모든 초기 더미 DB 데이터를 생성"

    def handle(self, *args, **kwargs):
        self.stdout.write(self.style.HTTP_INFO("초기 더미 DB 데이터 생성 시작\n"))

        try:
            # (1) 사용자 계정 생성
            self.stdout.write(self.style.HTTP_INFO("▶ 유저 생성 중..."))
            call_command("create_test_users")

            # (2) 과목 생성
            self.stdout.write(self.style.HTTP_INFO("▶ 과목 생성 중..."))
            call_command("create_test_subjects")

            # (3) 강좌 생성
            self.stdout.write(self.style.HTTP_INFO("▶ 강좌 생성 중..."))
            call_command("create_test_courses")

            # (4) 등록 정보 생성
            self.stdout.write(self.style.HTTP_INFO("▶ 등록 정보 생성 중..."))
            call_command("create_test_enrollments")

            # (5) 과제 정보 생성
            self.stdout.write(self.style.HTTP_INFO("▶ 과제 정보 생성 중..."))
            call_command("create_test_assignments")

            #
            # self.stdout.write(self.style.HTTP_INFO("▶ seed_assignments 실행 중..."))
            # call_command("seed_assignments")

            self.stdout.write(self.style.SUCCESS("\n모든 초기 데이터 생성 완료!"))
        except Exception as e:
            self.stdout.write(self.style.ERROR(f"데이터 생성 중 오류 발생: {e}"))
