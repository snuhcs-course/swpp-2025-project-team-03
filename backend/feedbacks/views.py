from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


class MessageSendView(APIView):  # POST /messages/send
    @swagger_auto_schema(
        operation_id="메시지 전송",
        operation_description="사용자에게 메시지를 전송합니다.",
        responses={201: "Message sent"},
    )
    def post(self, request):
        return Response({"message": "메시지 전송"}, status=status.HTTP_201_CREATED)


class MessageListView(APIView):  # GET /messages
    @swagger_auto_schema(
        operation_id="메시지 목록 조회",
        operation_description="사용자에게 전송된 메시지 목록을 조회합니다.",
        responses={200: "Message list"},
    )
    def get(self, request):
        return Response({"message": "사용자 메시지 조회"}, status=status.HTTP_200_OK)


class ClassMessageListView(APIView):  # GET /messages/{classId}
    @swagger_auto_schema(
        operation_id="클래스 메시지 조회",
        operation_description="특정 클래스에 속한 메시지 목록을 조회합니다.",
        responses={200: "Class message list"},
    )
    def get(self, request, classId):
        return Response({"message": f"{classId}번 클래스 메시지 조회"}, status=status.HTTP_200_OK)


class DashboardStatsView(APIView):  # GET /dashboard/stats
    @swagger_auto_schema(
        operation_id="대시보드 통계 조회",
        operation_description="대시보드에 표시할 통계 정보를 조회합니다.",
        responses={200: "Dashboard stats"},
    )
    def get(self, request):
        return Response({"message": "대시보드 통계"}, status=status.HTTP_200_OK)


class DashboardRecentActivitiesView(APIView):  # GET /dashboard/recent-activities
    @swagger_auto_schema(
        operation_id="최근 활동 조회",
        operation_description="대시보드에 표시할 최근 활동 정보를 조회합니다.",
        responses={200: "Recent activities"},
    )
    def get(self, request):
        return Response({"message": "최근 활동 조회"}, status=status.HTTP_200_OK)


class AnalysisStudentView(APIView):  # POST /analysis/student
    @swagger_auto_schema(
        operation_id="학생 분석",
        operation_description="학생의 학습 데이터를 분석합니다.",
        responses={200: "Student analysis result"},
    )
    def post(self, request):
        return Response({"message": "학생 분석 결과"}, status=status.HTTP_200_OK)


class AnalysisClassView(APIView):  # POST /analysis/class
    @swagger_auto_schema(
        operation_id="클래스 분석",
        operation_description="클래스의 학습 데이터를 분석합니다.",
        responses={200: "Class analysis result"},
    )
    def post(self, request):
        return Response({"message": "클래스 분석 결과"}, status=status.HTTP_200_OK)


class AnalysisSubjectView(APIView):  # POST /analysis/subject
    @swagger_auto_schema(
        operation_id="과목 분석",
        operation_description="과목의 학습 데이터를 분석합니다.",
        responses={200: "Subject analysis result"},
    )
    def post(self, request):
        return Response({"message": "과목 분석 결과"}, status=status.HTTP_200_OK)


class ProgressReportView(APIView):  # GET /reports/progress
    @swagger_auto_schema(
        operation_id="진도 보고서 조회",
        operation_description="학생의 진도 보고서를 조회합니다.",
        responses={200: "Progress report"},
    )
    def get(self, request):
        return Response({"message": "진도 보고서 조회"}, status=status.HTTP_200_OK)
