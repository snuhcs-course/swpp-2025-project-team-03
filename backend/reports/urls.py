from django.urls import path

from .views import CurriculumAnalysisView

app_name = "reports"

urlpatterns = [
    path("<int:class_id>/<int:student_id>/", CurriculumAnalysisView.as_view(), name="report-analysis"),
]
