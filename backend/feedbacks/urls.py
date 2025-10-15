from django.urls import path

from .views import (
    AnalysisClassView,
    AnalysisStudentView,
    AnalysisSubjectView,
    ClassMessageListView,
    DashboardRecentActivitiesView,
    DashboardStatsView,
    MessageListView,
    MessageSendView,
    ProgressReportView,
)

urlpatterns = [
    path("messages/send/", MessageSendView.as_view()),
    path("messages/", MessageListView.as_view()),
    path("messages/<int:classId>/", ClassMessageListView.as_view()),
    path("dashboard/stats/", DashboardStatsView.as_view()),
    path("dashboard/recent-activities/", DashboardRecentActivitiesView.as_view()),
    path("analysis/student/", AnalysisStudentView.as_view()),
    path("analysis/class/", AnalysisClassView.as_view()),
    path("analysis/subject/", AnalysisSubjectView.as_view()),
    path("reports/progress/", ProgressReportView.as_view()),
]
