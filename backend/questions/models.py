from django.db import models

class Question(models.Model):
    personal_assignment = models.ForeignKey("submissions.PersonalAssignment", on_delete=models.CASCADE, related_name="questions")
    number = models.PositiveIntegerField(help_text="문항 번호 (1..N)")
    content = models.CharField(max_length=255, blank=True)
    hint = models.CharField(max_length=255, blank=True, null=True)
    explanation = models.TextField(blank=True, null=True)
    model_answer = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ("personal_assignment", "number")
        ordering = ["personal_assignment_id", "number"]
        indexes = [models.Index(fields=["personal_assignment", "number"])]

    def __str__(self) -> str:
        return f"Q{self.number} of {self.personal_assignment}"
