from django.urls import path

from .views import AnswerSubmitView, PersonalAssignmentListView, PersonalAssignmentQuestionsView

urlpatterns = [
    path("", PersonalAssignmentListView.as_view()),
    path("<int:id>/questions/", PersonalAssignmentQuestionsView.as_view()),
    path("<int:id>/submit/", AnswerSubmitView.as_view()),
]
