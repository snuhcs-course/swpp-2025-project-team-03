from django.urls import path

from . import views

app_name = "core"

urlpatterns = [
    path("health/", views.HealthView.as_view(), name="health"),
    path("logs/tail", views.LogTailView.as_view(), name="log_tail"),
]
