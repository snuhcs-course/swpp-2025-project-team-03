from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


class AssignmentListView(APIView):  # GET /assignments
    @swagger_auto_schema(
        operation_id="과제 목록 조회",
        operation_description="모든 과제를 조회합니다.",
        responses={200: "Assignment list"},
    )
    def get(self, request):
        return Response({"message": "모든 과제 조회"}, status=status.HTTP_200_OK)


class AssignmentDetailView(APIView):  # GET, PUT, DELETE /assignments/{id}
    @swagger_auto_schema(
        operation_id="과제 상세 조회",
        operation_description="특정 과제의 상세 정보를 조회합니다.",
        responses={200: "Assignment detail"},
    )
    def get(self, request, id):
        return Response({"message": f"{id}번 과제 조회"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="과제 수정", operation_description="특정 과제를 수정합니다.", responses={200: "Assignment updated"}
    )
    def put(self, request, id):
        return Response({"message": "과제 수정"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="과제 삭제", operation_description="특정 과제를 삭제합니다.", responses={200: "Assignment deleted"}
    )
    def delete(self, request, id):
        return Response({"message": "과제 삭제"}, status=status.HTTP_200_OK)


class AssignmentCreateView(APIView):  # POST /assignments
    @swagger_auto_schema(
        operation_id="과제 생성",
        operation_description="새로운 과제를 생성합니다.",
        responses={201: "Assignment created"},
    )
    def post(self, request):
        return Response({"message": "과제 생성"}, status=status.HTTP_201_CREATED)


class AssignmentSubmitView(APIView):  # POST /assignments/{id}/submit
    @swagger_auto_schema(
        operation_id="과제 제출",
        operation_description="학생이 특정 과제를 제출합니다.",
        responses={201: "Assignment submitted"},
    )
    def post(self, request, id):
        return Response({"message": "과제 제출 완료"}, status=status.HTTP_201_CREATED)


class AssignmentResultsView(APIView):  # GET /assignments/{id}/results
    @swagger_auto_schema(
        operation_id="과제 결과 조회",
        operation_description="특정 과제의 제출 결과를 조회합니다.",
        responses={200: "Assignment results"},
    )
    def get(self, request, id):
        return Response({"message": "과제 결과 조회"}, status=status.HTTP_200_OK)


class AssignmentQuestionsView(APIView):  # GET /assignments/{id}/questions
    @swagger_auto_schema(
        operation_id="과제 문제 목록 조회",
        operation_description="특정 과제에 포함된 문제 목록을 조회합니다.",
        responses={200: "Assignment questions"},
    )
    def get(self, request, id):
        return Response({"message": "과제 문제 목록 조회"}, status=status.HTTP_200_OK)


class AssignmentDraftView(APIView):  # POST /assignments/{id}/draft
    @swagger_auto_schema(
        operation_id="과제 초안 저장",
        operation_description="특정 과제의 초안을 저장합니다.",
        responses={200: "Assignment draft saved"},
    )
    def post(self, request, id):
        return Response({"message": "과제 초안 저장"}, status=status.HTTP_200_OK)
