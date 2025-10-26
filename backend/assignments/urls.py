from django.urls import path

from .views import (
    AssignmentCreateView,
    AssignmentDetailView,
    AssignmentListView,
    AssignmentQuestionsView,
    AssignmentResultsView,
    AssignmentSubmitView,
    S3UploadCheckView,
)

urlpatterns = [
    path("", AssignmentListView.as_view(), name="assignment-list"),
    path("<int:id>/", AssignmentDetailView.as_view(), name="assignment-detail"),
    path("create/", AssignmentCreateView.as_view(), name="assignment-create"),
    path("<int:id>/submit/", AssignmentSubmitView.as_view(), name="assignment-submit"),
    path("<int:id>/results/", AssignmentResultsView.as_view(), name="assignment-results"),
    path("<int:id>/questions/", AssignmentQuestionsView.as_view(), name="assignment-questions"),
    path("<int:assignment_id>/s3-check/", S3UploadCheckView.as_view(), name="s3-upload-check"),
]
