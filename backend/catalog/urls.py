from django.urls import path

from .views import SubjectListView

app_name = "catalog"

urlpatterns = [
    path("subjects/", SubjectListView.as_view(), name="subject-list"),
]
