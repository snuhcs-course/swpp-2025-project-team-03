from django.db import models


class Subject(models.Model):
    name = models.CharField(max_length=255)
    code = models.CharField(max_length=50, unique=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self) -> str:
        return f"[{self.code}] {self.name}"


class Topic(models.Model):
    subject = models.ForeignKey("catalog.Subject", on_delete=models.CASCADE, related_name="topics")
    name = models.CharField(max_length=255)

    class Meta:
        unique_together = ("subject", "name")

    def __str__(self) -> str:
        return f"{self.subject.code} / {self.name}"
