from rest_framework import serializers


class AssignmentCreateSerializer(serializers.Serializer):
    assignment_id = serializers.IntegerField(help_text="생성된 과제의 고유 ID")
    material_id = serializers.IntegerField(help_text="생성된 자료(Material)의 고유 ID")
    s3_key = serializers.CharField(help_text="S3에 저장될 PDF 파일의 key 경로")
    upload_url = serializers.URLField(help_text="PDF 업로드용 presigned URL")
