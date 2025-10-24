from django.db import models


class Question(models.Model):
    class Difficulty(models.TextChoices):
        EASY = "easy", "Easy"
        MEDIUM = "medium", "Medium"
        HARD = "hard", "Hard"

    # TODO: api 테스트 목적으로 null을 허용했지만 반드시!!!! 제거해야합니다!!!!!
    personal_assignment = models.ForeignKey(
        "submissions.PersonalAssignment", on_delete=models.CASCADE, related_name="questions", null=True, blank=True
    )
    number = models.PositiveIntegerField(help_text="문항 번호 (1..N)")
    content = models.CharField(max_length=255, blank=True)
    topic = models.CharField(max_length=64, blank=True, null=True, help_text="단원")
    achievement_code = models.CharField(max_length=32, blank=True, null=True, help_text="교육과정 성취기준")
    recalled_num = models.IntegerField(help_text="몇 번째 (tail) question인지 나타내는 숫자")
    explanation = models.TextField(blank=True, null=True)
    model_answer = models.TextField(blank=True)
    difficulty = models.CharField(max_length=20, choices=Difficulty.choices, default=Difficulty.MEDIUM)
    created_at = models.DateTimeField(auto_now_add=True)

    base_question = models.ForeignKey(
        "self",
        on_delete=models.CASCADE,
        null=True,
        blank=True,
        related_name="tail_questions",
        help_text="The base question this one is derived from (null if this is a base question)",
    )

    class Meta:
        unique_together = ("personal_assignment", "number")
        ordering = ["personal_assignment_id", "number"]
        indexes = [models.Index(fields=["personal_assignment", "number"])]

    def __str__(self) -> str:
        return f"Q{self.number} of {self.personal_assignment}"
