from django.urls import path

from .views import QuestionCreateView

urlpatterns = [
    path("create/", QuestionCreateView.as_view()),
]
