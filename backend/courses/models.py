from django.conf import settings
from django.db import models


class CourseClass(models.Model):
    teacher = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="course_classes")
    subject = models.ForeignKey("catalog.Subject", on_delete=models.CASCADE, related_name="course_classes")
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True)
    start_date = models.DateTimeField()
    end_date = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        indexes = [
            models.Index(fields=["teacher"]),
            models.Index(fields=["subject"]),
            models.Index(fields=["start_date", "end_date"]),
        ]

    def __str__(self):
        return f"{self.name} ({self.subject.name})"


class Enrollment(models.Model):
    class Status(models.TextChoices):
        ENROLLED = "ENROLLED", "Enrolled"
        DROPPED = "DROPPED", "Dropped"
        COMPLETED = "COMPLETED", "Completed"

    student = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="enrollments")
    course_class = models.ForeignKey("courses.CourseClass", on_delete=models.CASCADE, related_name="enrollments")
    status = models.CharField(max_length=20, choices=Status.choices, default=Status.ENROLLED)

    class Meta:
        unique_together = ("student", "course_class")
        indexes = [
            models.Index(fields=["student"]),
            models.Index(fields=["course_class"]),
            models.Index(fields=["status"]),
        ]

    def __str__(self):
        return f"{self.student.display_name} - {self.course_class.name}"
