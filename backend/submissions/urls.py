from django.urls import path

from .views import AnswerSubmitView, PersonalAssignmentListView

urlpatterns = [
    path("", PersonalAssignmentListView.as_view()),
    path("<int:id>/submit/", AnswerSubmitView.as_view()),
]
