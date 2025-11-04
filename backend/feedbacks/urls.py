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
    path("messages/send/", MessageSendView.as_view(), name="message-send"),
    path("messages/", MessageListView.as_view(), name="message-list"),
    path("messages/<int:classId>/", ClassMessageListView.as_view(), name="class-message-list"),
    path("dashboard/stats/", DashboardStatsView.as_view(), name="dashboard-stats"),
    path("dashboard/recent-activities/", DashboardRecentActivitiesView.as_view(), name="dashboard-recent-activities"),
    path("analysis/student/", AnalysisStudentView.as_view(), name="analysis-student"),
    path("analysis/class/", AnalysisClassView.as_view(), name="analysis-class"),
    path("analysis/subject/", AnalysisSubjectView.as_view(), name="analysis-subject"),
    path("reports/progress/", ProgressReportView.as_view(), name="reports-progress"),
]
