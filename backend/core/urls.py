from django.urls import path

from . import views

app_name = "core"

urlpatterns = [
    path("health/", views.HealthView.as_view(), name="health"),
    path("error/", views.ErrorView.as_view(), name="error"),
    path("logs/tail", views.LogTailView.as_view(), name="log_tail"),
]
