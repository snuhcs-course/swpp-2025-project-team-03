"""
Reports Serializer 테스트
- CurriculumAnalysisSerializer 검증
- AchievementStatisticsSerializer 검증
- 데이터 직렬화/역직렬화 테스트
"""

from reports.serializers import AchievementStatisticsSerializer, CurriculumAnalysisSerializer
from rest_framework import serializers

# pytest 실행 예시
# pytest reports/tests/test_serializers.py -v


class TestAchievementStatisticsSerializer:
    """AchievementStatisticsSerializer 테스트"""

    def test_valid_data(self):
        """유효한 데이터 직렬화 테스트"""
        data = {
            "total_questions": 5,
            "correct_questions": 3,
            "accuracy": 60.0,
            "content": "테스트 성취기준 내용",
        }

        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is True
        assert serializer.validated_data == data

    def test_invalid_total_questions(self):
        """잘못된 total_questions 테스트"""
        data = {
            "total_questions": "invalid",  # 문자열이어야 하는데 정수여야 함
            "correct_questions": 3,
            "accuracy": 60.0,
            "content": "테스트 성취기준 내용",
        }

        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is False
        assert "total_questions" in serializer.errors

    def test_invalid_correct_questions(self):
        """잘못된 correct_questions 테스트"""
        data = {
            "total_questions": 5,
            "correct_questions": -1,  # 음수는 유효하지 않음
            "accuracy": 60.0,
            "content": "테스트 성취기준 내용",
        }

        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is False
        assert "correct_questions" in serializer.errors

    def test_invalid_accuracy(self):
        """잘못된 accuracy 테스트"""
        data = {
            "total_questions": 5,
            "correct_questions": 3,
            "accuracy": "invalid",  # 문자열이어야 하는데 숫자여야 함
            "content": "테스트 성취기준 내용",
        }

        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is False
        assert "accuracy" in serializer.errors

    def test_missing_required_fields(self):
        """필수 필드 누락 테스트"""
        data = {
            "total_questions": 5,
            # correct_questions 누락
            "accuracy": 60.0,
            "content": "테스트 성취기준 내용",
        }

        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is False
        assert "correct_questions" in serializer.errors

    def test_accuracy_calculation_edge_cases(self):
        """정확도 계산 엣지 케이스 테스트"""
        # 100% 정확도
        data = {
            "total_questions": 10,
            "correct_questions": 10,
            "accuracy": 100.0,
            "content": "완벽한 성취기준",
        }
        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is True

        # 0% 정확도
        data = {
            "total_questions": 10,
            "correct_questions": 0,
            "accuracy": 0.0,
            "content": "실패한 성취기준",
        }
        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is True

        # 소수점 정확도
        data = {
            "total_questions": 3,
            "correct_questions": 1,
            "accuracy": 33.3,
            "content": "부분적 성취기준",
        }
        serializer = AchievementStatisticsSerializer(data=data)
        assert serializer.is_valid() is True


class TestCurriculumAnalysisSerializer:
    """CurriculumAnalysisSerializer 테스트"""

    def test_valid_data(self):
        """유효한 데이터 직렬화 테스트"""
        data = {
            "total_questions": 10,
            "total_correct": 7,
            "overall_accuracy": 70.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 5,
                    "correct_questions": 3,
                    "accuracy": 60.0,
                    "content": "첫 번째 성취기준",
                },
                "4과01-02": {
                    "total_questions": 5,
                    "correct_questions": 4,
                    "accuracy": 80.0,
                    "content": "두 번째 성취기준",
                },
            },
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is True
        assert serializer.validated_data == data

    def test_empty_achievement_statistics(self):
        """빈 성취기준 통계 테스트"""
        data = {
            "total_questions": 0,
            "total_correct": 0,
            "overall_accuracy": 0.0,
            "achievement_statistics": {},
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is True
        assert serializer.validated_data == data

    def test_invalid_total_questions(self):
        """잘못된 total_questions 테스트"""
        data = {
            "total_questions": "invalid",
            "total_correct": 7,
            "overall_accuracy": 70.0,
            "achievement_statistics": {},
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is False
        assert "total_questions" in serializer.errors

    def test_invalid_total_correct(self):
        """잘못된 total_correct 테스트"""
        data = {
            "total_questions": 10,
            "total_correct": -1,  # 음수는 유효하지 않음
            "overall_accuracy": 70.0,
            "achievement_statistics": {},
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is False
        assert "total_correct" in serializer.errors

    def test_invalid_overall_accuracy(self):
        """잘못된 overall_accuracy 테스트"""
        data = {
            "total_questions": 10,
            "total_correct": 7,
            "overall_accuracy": "invalid",
            "achievement_statistics": {},
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is False
        assert "overall_accuracy" in serializer.errors

    def test_missing_required_fields(self):
        """필수 필드 누락 테스트"""
        data = {
            "total_questions": 10,
            # total_correct 누락
            "overall_accuracy": 70.0,
            "achievement_statistics": {},
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is False
        assert "total_correct" in serializer.errors

    def test_to_representation_with_content(self):
        """to_representation 메서드 테스트 (content 포함)"""
        serializer = CurriculumAnalysisSerializer()
        instance = {
            "total_questions": 5,
            "total_correct": 3,
            "overall_accuracy": 60.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 5,
                    "correct_questions": 3,
                    "accuracy": 60.0,
                    "content": "기존 내용",
                },
            },
        }

        result = serializer.to_representation(instance)

        assert result["total_questions"] == 5
        assert result["total_correct"] == 3
        assert result["overall_accuracy"] == 60.0
        assert result["achievement_statistics"]["4과01-01"]["content"] == "기존 내용"

    def test_to_representation_without_content(self):
        """to_representation 메서드 테스트 (content 누락)"""
        serializer = CurriculumAnalysisSerializer()
        instance = {
            "total_questions": 5,
            "total_correct": 3,
            "overall_accuracy": 60.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 5,
                    "correct_questions": 3,
                    "accuracy": 60.0,
                    # content 누락
                },
            },
        }

        result = serializer.to_representation(instance)

        assert result["total_questions"] == 5
        assert result["total_correct"] == 3
        assert result["overall_accuracy"] == 60.0
        assert result["achievement_statistics"]["4과01-01"]["content"] == "성취기준 내용을 찾을 수 없습니다."

    def test_to_representation_empty_statistics(self):
        """to_representation 메서드 테스트 (빈 통계)"""
        serializer = CurriculumAnalysisSerializer()
        instance = {
            "total_questions": 0,
            "total_correct": 0,
            "overall_accuracy": 0.0,
            "achievement_statistics": {},
        }

        result = serializer.to_representation(instance)

        assert result["total_questions"] == 0
        assert result["total_correct"] == 0
        assert result["overall_accuracy"] == 0.0
        assert result["achievement_statistics"] == {}

    def test_to_representation_missing_statistics(self):
        """to_representation 메서드 테스트 (통계 필드 누락)"""
        serializer = CurriculumAnalysisSerializer()
        instance = {
            "total_questions": 5,
            "total_correct": 3,
            "overall_accuracy": 60.0,
            # achievement_statistics 누락
        }

        result = serializer.to_representation(instance)

        assert result["total_questions"] == 5
        assert result["total_correct"] == 3
        assert result["overall_accuracy"] == 60.0
        assert result["achievement_statistics"] == {}

    def test_accuracy_rounding(self):
        """정확도 반올림 테스트"""
        serializer = CurriculumAnalysisSerializer()
        instance = {
            "total_questions": 3,
            "total_correct": 1,
            "overall_accuracy": 33.333333,  # 반올림되어야 함
            "achievement_statistics": {},
        }

        result = serializer.to_representation(instance)
        assert result["overall_accuracy"] == 33.3

    def test_large_numbers(self):
        """큰 숫자 처리 테스트"""
        data = {
            "total_questions": 10000,
            "total_correct": 7500,
            "overall_accuracy": 75.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 10000,
                    "correct_questions": 7500,
                    "accuracy": 75.0,
                    "content": "대규모 성취기준",
                },
            },
        }

        serializer = CurriculumAnalysisSerializer(data=data)
        assert serializer.is_valid() is True
        assert serializer.validated_data == data

    def test_serializer_field_types(self):
        """시리얼라이저 필드 타입 검증"""
        serializer = CurriculumAnalysisSerializer()

        # 필드 타입 확인
        assert isinstance(serializer.fields["total_questions"], serializers.IntegerField)
        assert isinstance(serializer.fields["total_correct"], serializers.IntegerField)
        assert isinstance(serializer.fields["overall_accuracy"], serializers.FloatField)
        assert isinstance(serializer.fields["achievement_statistics"], serializers.DictField)

        # achievement_statistics의 child serializer 확인
        child_serializer = serializer.fields["achievement_statistics"].child
        assert isinstance(child_serializer, AchievementStatisticsSerializer)
