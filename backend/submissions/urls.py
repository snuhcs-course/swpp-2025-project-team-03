from django.urls import path

from .views import (
    AnswerCorrectnessView,
    AnswerSubmitView,
    PersonalAssignmentCompleteView,
    PersonalAssignmentListView,
    PersonalAssignmentQuestionsView,
    PersonalAssignmentStatisticsView,
)

urlpatterns = [
    path("", PersonalAssignmentListView.as_view(), name="personal-assignment-list"),
    path("<int:id>/statistics/", PersonalAssignmentStatisticsView.as_view(), name="personal-assignment-statistics"),
    path("<int:id>/questions/", PersonalAssignmentQuestionsView.as_view(), name="personal-assignment-questions"),
    path("<int:id>/complete/", PersonalAssignmentCompleteView.as_view(), name="personal-assignment-complete"),
    path("answer/", AnswerSubmitView.as_view(), name="answer"),
    path("<int:id>/correctness/", AnswerCorrectnessView.as_view(), name="answer-correctness"),
]
