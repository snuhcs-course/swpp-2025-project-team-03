from django.urls import path

from .views import (
    AnswerSubmitView,
    PersonalAssignmentListView,
    PersonalAssignmentQuestionsView,
    PersonalAssignmentStatisticsView,
)

urlpatterns = [
    path("", PersonalAssignmentListView.as_view(), name="personal-assignment-list"),
    path("<int:id>/statistics/", PersonalAssignmentStatisticsView.as_view(), name="personal-assignment-statistics"),
    path("<int:id>/questions/", PersonalAssignmentQuestionsView.as_view(), name="personal-assignment-questions"),
    path("answer/", AnswerSubmitView.as_view(), name="answer"),
]
