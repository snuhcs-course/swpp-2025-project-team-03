from django.db import models
from django.conf import settings


class PersonalAssignment(models.Model):
    class Status(models.TextChoices):
        NOT_STARTED = "NOT_STARTED", "Not Started"
        IN_PROGRESS = "IN_PROGRESS", "In Progress"
        SUBMITTED = "SUBMITTED", "Submitted"
        GRADED = "GRADED", "Graded"
    
    student = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="personal_assignments")
    assignment = models.ForeignKey("assignments.Assignment", on_delete=models.CASCADE,related_name="personal_assignments")
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
  
class Questions(models.Model):
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

class Answer(models.Model):
    class State(models.TextChoices):
        CORRECT = "correct",           "Correct"
        INCORRECT = "incorrect",       "Incorrect"
    
    question = models.ForeignKey("submissions.Questions", on_delete=models.CASCADE, related_name="answers")
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
