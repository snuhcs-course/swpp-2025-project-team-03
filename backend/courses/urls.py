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
    path("students/", StudentListView.as_view()),
    path("students/<int:id>/", StudentDetailView.as_view()),
    path("students/<int:id>/assignments/", StudentAssignmentsView.as_view()),
    path("students/<int:id>/progress/", StudentProgressView.as_view()),
    # Class APIs
    path("classes/", ClassListView.as_view()),
    path("classes/<int:id>/", ClassDetailView.as_view()),
    path("classes/<int:id>/students/", ClassStudentsView.as_view()),
]
