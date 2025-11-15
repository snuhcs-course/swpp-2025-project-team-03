from django.conf import settings
from django.db import models


class PersonalAssignment(models.Model):
    class Status(models.TextChoices):
        NOT_STARTED = "NOT_STARTED", "Not Started"
        IN_PROGRESS = "IN_PROGRESS", "In Progress"
        SUBMITTED = "SUBMITTED", "Submitted"

    student = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="personal_assignments")
    assignment = models.ForeignKey(
        "assignments.Assignment", on_delete=models.CASCADE, related_name="personal_assignments"
    )
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.NOT_STARTED)
    solved_num = models.IntegerField(default=0)
    started_at = models.DateTimeField(null=True, blank=True)
    submitted_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "personal_assignment"
        unique_together = ("student", "assignment")
        indexes = [
            models.Index(fields=["student"]),
            models.Index(fields=["assignment"]),
            models.Index(fields=["status"]),
        ]

    def __str__(self) -> str:
        return f"{self.student} - {self.assignment.title} ({self.status})"


class Answer(models.Model):
    class State(models.TextChoices):
        CORRECT = "correct", "Correct"
        INCORRECT = "incorrect", "Incorrect"
        PROCESSING = "PROCESSING", "Processing"

    question = models.ForeignKey("questions.Question", on_delete=models.CASCADE, related_name="answers")
    student = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="answers")
    started_at = models.DateTimeField(null=True, blank=True)
    submitted_at = models.DateTimeField(null=True, blank=True)
    state = models.CharField(max_length=32, choices=State.choices, null=True, blank=True)
    text_answer = models.TextField(blank=True)
    eval_grade = models.FloatField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ("question", "student")
        indexes = [
            models.Index(fields=["student"]),
            models.Index(fields=["question"]),
        ]

    def __str__(self) -> str:
        return f"Answer by {self.student} on {self.question}"
