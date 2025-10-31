from rest_framework.test import APITestCase


class SubjectListViewTestCase(APITestCase):
    """SubjectListView 테스트"""

    def test_get_subject_list_success(self):
        """과목 목록 조회 성공 테스트"""
        # catalog 앱은 현재 URL이 설정되지 않았으므로 테스트 스킵
        # URL이 설정되면 테스트 활성화
        pass
