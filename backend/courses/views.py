from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView


class StudentListView(APIView):  # GET /students
    @swagger_auto_schema(
        operation_id="학생 목록 조회",
        operation_description="전체 학생 목록을 조회합니다.",
        responses={200: "Student list"},
    )
    def get(self, request):
        return Response({"message": "학생 목록 조회"}, status=status.HTTP_200_OK)


class StudentDetailView(APIView):  # GET, PUT, DELETE /students/{id}
    @swagger_auto_schema(
        operation_id="학생 상세 조회",
        operation_description="특정 학생의 상세 정보를 조회합니다.",
        responses={200: "Student detail"},
    )
    def get(self, request, id):
        return Response({"message": "학생 상세 조회"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="학생 정보 수정",
        operation_description="특정 학생의 정보를 수정합니다.",
        responses={200: "Student updated"},
    )
    def put(self, request, id):
        return Response({"message": "학생 정보 수정"}, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        operation_id="학생 삭제", operation_description="특정 학생을 삭제합니다.", responses={200: "Student deleted"}
    )
    def delete(self, request, id):
        return Response({"message": "학생 삭제"}, status=status.HTTP_200_OK)


class StudentAssignmentsView(APIView):  # GET /students/{id}/assignments
    @swagger_auto_schema(
        operation_id="학생 과제 조회",
        operation_description="특정 학생의 과제 목록을 조회합니다.",
        responses={200: "Student assignments"},
    )
    def get(self, request, id):
        return Response({"message": "학생 과제 조회"}, status=status.HTTP_200_OK)


class StudentProgressView(APIView):  # GET /students/{id}/progress
    @swagger_auto_schema(
        operation_id="학생 진도 조회",
        operation_description="특정 학생의 진도 현황을 조회합니다.",
        responses={200: "Student progress"},
    )
    def get(self, request, id):
        return Response({"message": "학생 진도 조회"}, status=status.HTTP_200_OK)


class ClassListView(APIView):  # GET /classes
    @swagger_auto_schema(
        operation_id="클래스 목록 조회",
        operation_description="전체 클래스 목록을 조회합니다.",
        responses={200: "Class list"},
    )
    def get(self, request):
        return Response({"message": "클래스 목록 조회"}, status=status.HTTP_200_OK)


class ClassDetailView(APIView):  # GET /classes/{id}
    @swagger_auto_schema(
        operation_id="클래스 상세 조회",
        operation_description="특정 클래스의 상세 정보를 조회합니다.",
        responses={200: "Class detail"},
    )
    def get(self, request, id):
        return Response({"message": "클래스 상세 조회"}, status=status.HTTP_200_OK)


class ClassStudentsView(APIView):  # GET /classes/{id}/students
    @swagger_auto_schema(
        operation_id="클래스 학생 목록 조회",
        operation_description="특정 클래스에 속한 학생 목록을 조회합니다.",
        responses={200: "Class students"},
    )
    def get(self, request, id):
        return Response({"message": "클래스 학생 목록 조회"}, status=status.HTTP_200_OK)
