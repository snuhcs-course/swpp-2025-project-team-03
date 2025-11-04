from django.core.management.base import BaseCommand
from questions.models import Question
from submissions.models import PersonalAssignment


class Command(BaseCommand):
    help = "테스트용 질문(Question) 데이터를 생성합니다."

    def handle(self, *args, **options):
        # PersonalAssignment ID 기준으로 질문 생성
        personal_assignment_ids = [1, 2, 3, 4]  # 예상되는 PersonalAssignment ID들
        total_created = 0
        total_skipped = 0

        self.stdout.write(self.style.HTTP_INFO("테스트용 Question 생성 시작\n"))

        # 각 PersonalAssignment에 대해 질문 생성
        for pa_id in personal_assignment_ids:
            try:
                personal_assignment = PersonalAssignment.objects.get(id=pa_id)
            except PersonalAssignment.DoesNotExist:
                self.stdout.write(self.style.WARNING(f"⚠ 존재하지 않는 PersonalAssignment ID: {pa_id}"))
                continue

            # 과제별 질문 데이터 정의
            questions_data = self._get_questions_data_for_assignment(personal_assignment)

            created_count = 0
            skipped_count = 0

            for question_data in questions_data:
                question, created = Question.objects.get_or_create(
                    personal_assignment=personal_assignment,
                    number=question_data["number"],
                    defaults={
                        "content": question_data["content"],
                        "topic": question_data.get("topic", ""),
                        "achievement_code": question_data.get("achievement_code"),
                        "recalled_num": question_data.get("recalled_num", 0),
                        "explanation": question_data.get("explanation", ""),
                        "model_answer": question_data.get("model_answer", ""),
                        "difficulty": question_data.get("difficulty", Question.Difficulty.MEDIUM),
                    },
                )

                if created:
                    created_count += 1
                    self.stdout.write(
                        self.style.SUCCESS(
                            f"✓ 생성됨: {personal_assignment} - Q{question_data['number']}: {question_data['content'][:30]}..."
                        )
                    )
                else:
                    skipped_count += 1
                    self.stdout.write(
                        self.style.WARNING(f"⚠ 이미 존재함: {personal_assignment} - Q{question_data['number']}")
                    )

            total_created += created_count
            total_skipped += skipped_count

            self.stdout.write(
                self.style.SUCCESS(
                    f"\n[PersonalAssignment ID {pa_id}] 새로 생성: {created_count}개, 이미 존재: {skipped_count}개\n"
                )
            )

        self.stdout.write(
            self.style.SUCCESS(
                f"Question 생성 완료!\n총 새로 생성된 항목: {total_created}개, 이미 존재한 항목: {total_skipped}개"
            )
        )

    def _get_questions_data_for_assignment(self, personal_assignment):
        """PersonalAssignment에 따라 적절한 질문 데이터를 반환합니다."""

        # 과제의 과목과 학년에 따라 다른 질문 생성
        subject_name = personal_assignment.assignment.subject.name
        grade = personal_assignment.assignment.grade

        if "과학" in subject_name:
            return self._get_science_questions(grade)
        elif "수학" in subject_name:
            return self._get_math_questions(grade)
        elif "영어" in subject_name:
            return self._get_english_questions(grade)

    def _get_science_questions(self, grade):
        """과학 과목 질문들"""
        if "중학교" in grade:
            return [
                {
                    "number": 1,
                    "content": "호흡 운동에서 가슴이 부풀어 오르는 이유는 무엇인가요?",
                    "recalled_num": 0,
                    "explanation": "호흡 운동 시 횡격막과 늑간근의 수축으로 인해 흉강의 부피가 증가합니다.",
                    "model_answer": "횡격막과 늑간근이 수축하여 흉강의 부피가 증가하기 때문입니다.",
                    "difficulty": Question.Difficulty.MEDIUM,
                },
                {
                    "number": 2,
                    "content": "신장에서 이루어지는 주요 기능은 무엇인가요?",
                    "recalled_num": 0,
                    "explanation": "신장은 혈액을 여과하여 노폐물을 제거하고 체내 수분과 전해질 균형을 유지합니다.",
                    "model_answer": "혈액을 여과하여 노폐물을 제거하고 체내 수분과 전해질 균형을 유지합니다.",
                    "difficulty": Question.Difficulty.EASY,
                },
                {
                    "number": 3,
                    "content": "폐에서 일어나는 가스 교환 과정을 설명하세요.",
                    "recalled_num": 0,
                    "explanation": "폐포에서 산소는 혈액으로 확산되고, 이산화탄소는 혈액에서 폐포로 확산됩니다.",
                    "model_answer": "폐포에서 산소는 혈액으로 확산되고, 이산화탄소는 혈액에서 폐포로 확산됩니다.",
                    "difficulty": Question.Difficulty.HARD,
                },
            ]
        else:
            return self._get_general_questions()

    def _get_math_questions(self, grade):
        """수학 과목 질문들"""
        if "초등학교" in grade:
            return [
                {
                    "number": 1,
                    "content": "함수 f(x) = 2x + 3에서 f(5)의 값을 구하세요.",
                    "recalled_num": 0,
                    "explanation": "x에 5를 대입하여 계산합니다: f(5) = 2×5 + 3 = 13",
                    "model_answer": "13",
                    "difficulty": Question.Difficulty.EASY,
                },
                {
                    "number": 2,
                    "content": "일차함수 y = 3x - 2의 그래프의 기울기는 얼마인가요?",
                    "recalled_num": 0,
                    "explanation": "일차함수 y = ax + b에서 a가 기울기입니다.",
                    "model_answer": "3",
                    "difficulty": Question.Difficulty.MEDIUM,
                },
            ]

    def _get_english_questions(self, grade):
        """영어 과목 질문들"""
        return [
            {
                "number": 1,
                "content": "다음 문장의 주어를 찾으세요: 'The students are studying hard.'",
                "recalled_num": 0,
                "explanation": "주어는 동작의 주체를 나타내는 명사나 대명사입니다.",
                "model_answer": "The students",
                "difficulty": Question.Difficulty.EASY,
            },
            {
                "number": 2,
                "content": "다음 단어의 반의어를 찾으세요: 'happy'",
                "recalled_num": 0,
                "explanation": "happy의 반의어는 슬프다는 의미의 단어입니다.",
                "model_answer": "sad",
                "difficulty": Question.Difficulty.EASY,
            },
            {
                "number": 3,
                "content": "다음 문장을 과거형으로 바꾸세요: 'I go to school every day.'",
                "recalled_num": 0,
                "explanation": "현재형을 과거형으로 바꿀 때는 동사의 과거형을 사용합니다.",
                "model_answer": "I went to school every day.",
                "difficulty": Question.Difficulty.MEDIUM,
            },
        ]
