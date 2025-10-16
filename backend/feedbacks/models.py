from django.conf import settings
from django.db import models


class TeacherFeedback(models.Model):
    course_class = models.ForeignKey("courses.CourseClass", on_delete=models.CASCADE, related_name="teacher_feedbacks")
    student = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="received_feedbacks")
    teacher = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="given_feedbacks")
    content = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "teacher_feedback"
        indexes = [
            models.Index(fields=["course_class"]),
            models.Index(fields=["student"]),
            models.Index(fields=["teacher"]),
        ]

    def __str__(self) -> str:
        return f"{self.teacher} → {self.student} ({self.course_class})"
