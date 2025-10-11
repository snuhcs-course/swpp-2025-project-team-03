from django.db import models

class Assignment(models.Model):
    course_class = models.ForeignKey("courses.CourseClass", on_delete=models.CASCADE, related_name="assignments")
    topic = models.ForeignKey("catalog.Topic", on_delete=models.SET_NULL, null=True, blank=True, related_name="assignments")
    title = models.CharField(max_length=255)
    description = models.TextField(blank=True)
    total_questions = models.PositiveIntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)
    visible_from = models.DateTimeField()
    due_at = models.DateTimeField()
    
    class Meta:
        indexes = [
            models.Index(fields=["course_class"]),
            models.Index(fields=["topic"]),
            models.Index(fields=["visible_from"]),
            models.Index(fields=["due_at"]),
        ]

    def __str__(self) -> str:
        return f"{self.title} / {self.course_class.title}"

class Material(models.Model):
    class Kind(models.TextChoices):
        TEXT  = "text",  "Text"
        IMAGE = "image", "Image"
        PDF   = "pdf",   "PDF"
        OTHER = "other", "Other"
        
    assignment = models.ForeignKey("assignments.Assignment", on_delete=models.CASCADE, related_name="materials")
    created_at = models.DateTimeField(auto_now_add=True)
    kind = models.CharField(max_length=20, choices=Kind.choices, default=Kind.OTHER)
    s3_key = models.CharField(max_length=255)
    bytes = models.PositiveIntegerField()
    
    class Meta:
        indexes = [models.Index(fields=["assignment", "kind"])]

    def __str__(self) -> str:
        return f"{self.assignment.title} [{self.kind}]"
