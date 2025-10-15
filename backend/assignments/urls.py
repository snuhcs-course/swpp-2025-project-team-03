from django.urls import path

from .views import (
    AssignmentCreateView,
    AssignmentDetailView,
    AssignmentDraftView,
    AssignmentListView,
    AssignmentQuestionsView,
    AssignmentResultsView,
    AssignmentSubmitView,
)

urlpatterns = [
    path("", AssignmentListView.as_view()),
    path("<int:id>/", AssignmentDetailView.as_view()),
    path("create/", AssignmentCreateView.as_view()),
    path("<int:id>/submit/", AssignmentSubmitView.as_view()),
    path("<int:id>/results/", AssignmentResultsView.as_view()),
    path("<int:id>/questions/", AssignmentQuestionsView.as_view()),
    path("<int:id>/draft/", AssignmentDraftView.as_view()),
]
