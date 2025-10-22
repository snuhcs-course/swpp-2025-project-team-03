from django.urls import path

from .views import (
    ClassDetailView,
    ClassListView,
    ClassStudentsView,
    StudentAssignmentsView,
    StudentDetailView,
    StudentListView,
    StudentProgressView,
)

urlpatterns = [
    # Student APIs
    path("students/", StudentListView.as_view(), name="student-list"),
    path("students/<int:id>/", StudentDetailView.as_view(), name="student-detail"),
    path("students/<int:id>/assignments/", StudentAssignmentsView.as_view(), name="student-assignments"),
    path("students/<int:id>/progress/", StudentProgressView.as_view(), name="student-progress"),
    # Class APIs
    path("classes/", ClassListView.as_view(), name="class-list"),
    path("classes/<int:id>/", ClassDetailView.as_view(), name="class-detail"),
    path("classes/<int:id>/students/", ClassStudentsView.as_view(), name="class-students"),
]
