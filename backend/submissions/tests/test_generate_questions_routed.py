import json
from unittest.mock import MagicMock, patch

from django.test import TestCase


class TestFormatStudentContext(TestCase):
    """Tests for format_student_context function"""

    def setUp(self):
        # Import here to avoid module-level import issues with environment variables
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import format_student_context

            self.format_student_context = format_student_context

    def test_none_context_returns_default_message(self):
        """Test that None context returns default message"""
        result = self.format_student_context(None)
        self.assertEqual(result, "No previous learning context available.")

    def test_empty_context_returns_default_message(self):
        """Test that empty context returns default message"""
        result = self.format_student_context({})
        self.assertEqual(result, "No previous learning context available.")

    def test_question_chain_formatting(self):
        """Test question chain is formatted correctly"""
        context = {
            "question_chain": [
                {
                    "question": "Test question",
                    "student_answer": "Test answer",
                    "is_correct": True,
                    "confidence": 4.5,
                    "difficulty": "medium",
                }
            ]
        }
        result = self.format_student_context(context)

        # Check structure and content
        self.assertIn("이 문항에서의 이전 시도:", result)
        self.assertIn("시도 1:", result)
        self.assertIn("✓ 정답", result)
        self.assertIn("자신감: 4.5/5", result)
        self.assertIn("학생 답변:", result)
        self.assertIn("Test answer", result)
        self.assertIn("난이도: medium", result)

        # Verify line structure
        lines = result.split("\n")
        self.assertGreater(len(lines), 3)  # Should have multiple lines
        self.assertTrue(any("시도 1:" in line for line in lines))

    def test_question_chain_incorrect_answer(self):
        """Test incorrect answer formatting"""
        context = {
            "question_chain": [
                {
                    "question": "Test question",
                    "student_answer": "Wrong answer",
                    "is_correct": False,
                    "confidence": 2.0,
                    "difficulty": "easy",
                }
            ]
        }
        result = self.format_student_context(context)
        self.assertIn("✗ 오답", result)

    def test_long_answer_truncation(self):
        """Test that long answers are truncated"""
        long_answer = "A" * 100  # More than 60 chars
        context = {
            "question_chain": [
                {
                    "question": "Test",
                    "student_answer": long_answer,
                    "is_correct": True,
                    "confidence": 3.0,
                }
            ]
        }
        result = self.format_student_context(context)
        self.assertIn("...", result)

    def test_overall_statistics_formatting(self):
        """Test overall statistics are formatted correctly"""
        context = {"total_answered": 10, "overall_accuracy": 0.8, "avg_confidence": 3.5}
        result = self.format_student_context(context)
        self.assertIn("전체 과제 수행 현황:", result)
        self.assertIn("정답률: 80%", result)
        self.assertIn("평균 자신감: 3.5/5", result)
        self.assertIn("답변한 문제 수: 10", result)

    def test_recent_trend_improving(self):
        """Test recent trend when student is improving"""
        context = {"recent_trend": [True, True, False, False, True]}
        result = self.format_student_context(context)
        self.assertIn("최근 추이", result)
        self.assertIn("최근 향상 중", result)

    def test_recent_trend_struggling(self):
        """Test recent trend when student is struggling"""
        context = {"recent_trend": [False, False, False, True, True]}
        result = self.format_student_context(context)
        self.assertIn("최근 어려움을 겪는 중", result)

    def test_recent_trend_unstable(self):
        """Test recent trend when performance is unstable (sum of first 3 == 1)"""
        # recent_trend[:3] = [False, True, False], sum = 1 → "불안정"
        context = {"recent_trend": [False, True, False, True, True]}
        result = self.format_student_context(context)
        self.assertIn("불안정", result)

    def test_recent_trend_short(self):
        """Test recent trend with less than 3 items (no analysis)"""
        context = {"recent_trend": [True, False]}
        result = self.format_student_context(context)
        self.assertIn("최근 추이", result)
        self.assertNotIn("향상 중", result)
        self.assertNotIn("어려움", result)

    def test_weak_concepts_formatting(self):
        """Test weak concepts are formatted correctly"""
        context = {"weak_concepts": ["운동과 에너지", "전기와 자기", "파동"]}
        result = self.format_student_context(context)
        self.assertIn("취약 개념:", result)
        self.assertIn("운동과 에너지", result)

    def test_weak_concepts_limited_to_three(self):
        """Test that weak concepts are limited to 3"""
        context = {"weak_concepts": ["A", "B", "C", "D", "E"]}
        result = self.format_student_context(context)
        self.assertIn("A", result)
        self.assertIn("B", result)
        self.assertIn("C", result)
        self.assertNotIn("D", result)
        self.assertNotIn("E", result)

    def test_no_confidence_in_chain(self):
        """Test chain item without confidence"""
        context = {
            "question_chain": [
                {
                    "question": "Test",
                    "student_answer": "Answer",
                    "is_correct": True,
                    "confidence": 0,  # Falsy value
                }
            ]
        }
        result = self.format_student_context(context)
        self.assertNotIn("자신감:", result)


class TestDecideBucketConfidence(TestCase):
    """Tests for decide_bucket_confidence function"""

    def setUp(self):
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import decide_bucket_confidence

            self.decide_bucket_confidence = decide_bucket_confidence

    def test_bucket_a_correct_high_confidence(self):
        """Test Bucket A: correct + high confidence"""
        bucket, confidence, strategy, example = self.decide_bucket_confidence(
            is_correct=True, eval_grade=4.5, high_thr=4
        )

        # Verify all return values
        self.assertEqual(bucket, "A")
        self.assertEqual(confidence, "high")
        self.assertIn("deeper", strategy.lower())

        # Verify example is a list with 6 items (from EXAMPLES["A"])
        self.assertIsInstance(example, list)
        self.assertEqual(len(example), 6)
        # Verify example structure
        for item in example:
            self.assertIn("input", item)
            self.assertIn("output", item)
            self.assertIn("question", item["input"])
            self.assertIn("model_answer", item["output"])

    def test_bucket_b_correct_low_confidence(self):
        """Test Bucket B: correct + low confidence"""
        bucket, confidence, strategy, example = self.decide_bucket_confidence(
            is_correct=True, eval_grade=3.0, high_thr=4
        )
        self.assertEqual(bucket, "B")
        self.assertEqual(confidence, "low")
        self.assertIn("reinforcing", strategy.lower())

    def test_bucket_c_incorrect_high_confidence(self):
        """Test Bucket C: incorrect + high confidence"""
        bucket, confidence, strategy, example = self.decide_bucket_confidence(
            is_correct=False, eval_grade=4.5, high_thr=4
        )
        self.assertEqual(bucket, "C")
        self.assertEqual(confidence, "high")
        self.assertIn("misconception", strategy.lower())

    def test_bucket_d_incorrect_low_confidence(self):
        """Test Bucket D: incorrect + low confidence"""
        bucket, confidence, strategy, example = self.decide_bucket_confidence(
            is_correct=False, eval_grade=2.0, high_thr=4
        )
        self.assertEqual(bucket, "D")
        self.assertEqual(confidence, "low")
        self.assertIn("scaffolding", strategy.lower())

    def test_threshold_boundary_high(self):
        """Test exactly at threshold is high"""
        bucket, confidence, _, _ = self.decide_bucket_confidence(is_correct=True, eval_grade=4.0, high_thr=4)
        self.assertEqual(confidence, "high")

    def test_threshold_boundary_low(self):
        """Test below threshold is low"""
        bucket, confidence, _, _ = self.decide_bucket_confidence(is_correct=True, eval_grade=3.99, high_thr=4)
        self.assertEqual(confidence, "low")


class TestDecidePlan(TestCase):
    """Tests for decide_plan function"""

    def setUp(self):
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import decide_plan

            self.decide_plan = decide_plan

    def test_recalled_time_0_always_ask(self):
        """Test first recall always asks"""
        for bucket in ["A", "B", "C", "D"]:
            result = self.decide_plan(bucket=bucket, recalled_time=0)
            self.assertEqual(result, "ASK")

    def test_recalled_time_1_bucket_a_only_correct(self):
        """Test recalled_time=1 with bucket A returns ONLY_CORRECT"""
        result = self.decide_plan(bucket="A", recalled_time=1)
        self.assertEqual(result, "ONLY_CORRECT")

    def test_recalled_time_1_other_buckets_ask(self):
        """Test recalled_time=1 with other buckets returns ASK"""
        for bucket in ["B", "C", "D"]:
            result = self.decide_plan(bucket=bucket, recalled_time=1)
            self.assertEqual(result, "ASK")

    def test_recalled_time_2_bucket_a_only_correct(self):
        """Test recalled_time=2 with bucket A returns ONLY_CORRECT"""
        result = self.decide_plan(bucket="A", recalled_time=2)
        self.assertEqual(result, "ONLY_CORRECT")

    def test_recalled_time_2_other_buckets_ask(self):
        """Test recalled_time=2 with other buckets returns ASK"""
        for bucket in ["B", "C", "D"]:
            result = self.decide_plan(bucket=bucket, recalled_time=2)
            self.assertEqual(result, "ASK")

    def test_recalled_time_3_or_more_only_correct(self):
        """Test recalled_time>=3 always returns ONLY_CORRECT"""
        for bucket in ["A", "B", "C", "D"]:
            for recalled_time in [3, 4, 5]:
                result = self.decide_plan(bucket=bucket, recalled_time=recalled_time)
                self.assertEqual(result, "ONLY_CORRECT")


class TestOnlyCorrectNode(TestCase):
    """Tests for only_correct_node function"""

    def setUp(self):
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import only_correct_node

            self.only_correct_node = only_correct_node

    def test_only_correct_node_correct_answer(self):
        """Test only_correct_node with correct answer"""
        state = {
            "recalled_time": 1,
            "is_correct": True,
            "bucket": "A",
            "confidence": "high",
        }
        result = self.only_correct_node(state)
        self.assertEqual(result["result"]["plan"], "ONLY_CORRECT")
        self.assertEqual(result["result"]["correctness"], "correct")
        self.assertEqual(result["result"]["recalled_time"], 2)
        self.assertIsNone(result["result"]["response"])

    def test_only_correct_node_incorrect_answer(self):
        """Test only_correct_node with incorrect answer"""
        state = {
            "recalled_time": 2,
            "is_correct": False,
            "bucket": "D",
            "confidence": "low",
        }
        result = self.only_correct_node(state)
        self.assertEqual(result["result"]["correctness"], "incorrect")
        self.assertEqual(result["result"]["recalled_time"], 3)


class TestRouteAfterDerive(TestCase):
    """Tests for route_after_derive function"""

    def setUp(self):
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import route_after_derive

            self.route_after_derive = route_after_derive

    def test_route_ask(self):
        """Test routing to ASK"""
        state = {"plan": "ASK"}
        result = self.route_after_derive(state)
        self.assertEqual(result, "ASK")

    def test_route_only_correct(self):
        """Test routing to ONLY_CORRECT"""
        state = {"plan": "ONLY_CORRECT"}
        result = self.route_after_derive(state)
        self.assertEqual(result, "ONLY_CORRECT")

    def test_route_none_defaults_to_ask(self):
        """Test that None plan defaults to ASK"""
        state = {"plan": None}
        result = self.route_after_derive(state)
        self.assertEqual(result, "ASK")


class TestDeriveAndRouteNode(TestCase):
    """Tests for derive_and_route_node function"""

    def setUp(self):
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            from submissions.utils.tail_question_generator.generate_questions_routed import derive_and_route_node

            self.derive_and_route_node = derive_and_route_node

    def test_derive_and_route_bucket_a(self):
        """Test derive_and_route for bucket A scenario"""
        state = {
            "is_correct": True,
            "eval_grade": 4.5,
            "high_thr": 4.0,
            "recalled_time": 0,
        }
        result = self.derive_and_route_node(state)
        self.assertEqual(result["bucket"], "A")
        self.assertEqual(result["confidence"], "high")
        self.assertEqual(result["plan"], "ASK")

    def test_derive_and_route_bucket_d(self):
        """Test derive_and_route for bucket D scenario"""
        state = {
            "is_correct": False,
            "eval_grade": 2.0,
            "high_thr": 4.0,
            "recalled_time": 0,
        }
        result = self.derive_and_route_node(state)
        self.assertEqual(result["bucket"], "D")
        self.assertEqual(result["confidence"], "low")
        self.assertEqual(result["plan"], "ASK")


class TestPlannerNode(TestCase):
    """Tests for planner_node function with mocked LLM"""

    def test_planner_node_correct_answer(self):
        """Test planner_node identifies correct answer"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.planner_llm") as mock_llm:
                mock_response = MagicMock()
                mock_response.content = '{"is_correct": true}'
                mock_llm.invoke.return_value = mock_response

                from submissions.utils.tail_question_generator.generate_questions_routed import planner_node

                state = {
                    "question": "Test question",
                    "model_answer": "Correct answer",
                    "student_answer": "Correct answer",
                }
                result = planner_node(state)
                self.assertTrue(result["is_correct"])

    def test_planner_node_incorrect_answer(self):
        """Test planner_node identifies incorrect answer"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.planner_llm") as mock_llm:
                mock_response = MagicMock()
                mock_response.content = '{"is_correct": false}'
                mock_llm.invoke.return_value = mock_response

                from submissions.utils.tail_question_generator.generate_questions_routed import planner_node

                state = {
                    "question": "Test question",
                    "model_answer": "Correct answer",
                    "student_answer": "Wrong answer",
                }
                result = planner_node(state)
                self.assertFalse(result["is_correct"])


class TestActorNode(TestCase):
    """Tests for actor_node function with mocked LLM"""

    def test_actor_node_generates_question(self):
        """Test actor_node generates tail question"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.actor_llm") as mock_llm:
                mock_response = MagicMock()
                mock_response.content = json.dumps(
                    {
                        "response": {
                            "topic": "Test topic",
                            "question": "Follow-up question?",
                            "model_answer": "Expected answer",
                            "explanation": "Why this matters",
                            "difficulty": "medium",
                        }
                    }
                )
                mock_llm.invoke.return_value = mock_response

                from submissions.utils.tail_question_generator.generate_questions_routed import (
                    EXAMPLES,
                    actor_node,
                    strategy_A,
                )

                state = {
                    "question": "Original question",
                    "model_answer": "Model answer",
                    "student_answer": "Student answer",
                    "strategy": strategy_A,
                    "example": EXAMPLES["A"],
                    "recalled_time": 0,
                    "is_correct": True,
                    "bucket": "A",
                    "confidence": "high",
                    "student_context": None,
                }
                result = actor_node(state)
                self.assertEqual(result["result"]["plan"], "ASK")
                self.assertEqual(result["result"]["recalled_time"], 1)
                self.assertEqual(result["result"]["response"]["topic"], "Test topic")


class TestGenerateTailQuestion(TestCase):
    """Tests for generate_tail_question function with mocked LLM"""

    def test_generate_tail_question_ask_plan(self):
        """Test generate_tail_question with ASK plan"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.app") as mock_app:
                mock_app.invoke.return_value = {
                    "is_correct": True,
                    "confidence": "high",
                    "bucket": "A",
                    "result": {
                        "plan": "ASK",
                        "recalled_time": 1,
                        "response": {
                            "topic": "Physics",
                            "question": "Follow-up?",
                            "model_answer": "Answer",
                            "explanation": "Explanation",
                            "difficulty": "hard",
                        },
                    },
                }

                from submissions.utils.tail_question_generator.generate_questions_routed import generate_tail_question

                result = generate_tail_question(
                    question="Test question",
                    model_answer="Model answer",
                    student_answer="Correct answer",
                    eval_grade=4.5,
                    recalled_time=0,
                    high_thr=4.0,
                    student_context={"total_answered": 5, "overall_accuracy": 0.8},
                )

                # Verify all required fields in response
                self.assertTrue(result["is_correct"])
                self.assertEqual(result["confidence"], "high")
                self.assertEqual(result["bucket"], "A")
                self.assertEqual(result["plan"], "ASK")
                self.assertEqual(result["recalled_time"], 1)

                # Verify tail_question structure
                tail_q = result["tail_question"]
                self.assertEqual(tail_q["topic"], "Physics")
                self.assertEqual(tail_q["question"], "Follow-up?")
                self.assertEqual(tail_q["model_answer"], "Answer")
                self.assertEqual(tail_q["explanation"], "Explanation")
                self.assertEqual(tail_q["difficulty"], "hard")

    def test_generate_tail_question_only_correct_plan(self):
        """Test generate_tail_question with ONLY_CORRECT plan"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.app") as mock_app:
                mock_app.invoke.return_value = {
                    "is_correct": True,
                    "confidence": "high",
                    "bucket": "A",
                    "result": {
                        "plan": "ONLY_CORRECT",
                        "recalled_time": 2,
                        "response": None,
                    },
                }

                from submissions.utils.tail_question_generator.generate_questions_routed import generate_tail_question

                result = generate_tail_question(
                    question="Test question",
                    model_answer="Model answer",
                    student_answer="Correct answer",
                    eval_grade=4.5,
                    recalled_time=1,
                    high_thr=4.0,
                )

                self.assertEqual(result["plan"], "ONLY_CORRECT")
                # tail_question should be empty dict when response is None
                self.assertEqual(result["tail_question"]["topic"], "")
                self.assertEqual(result["tail_question"]["question"], "")

    def test_generate_tail_question_with_full_context(self):
        """Test generate_tail_question with full student context"""
        with patch.dict("os.environ", {"OPENAI_API_KEY": "test-key"}):
            with patch("submissions.utils.tail_question_generator.generate_questions_routed.app") as mock_app:
                mock_app.invoke.return_value = {
                    "is_correct": False,
                    "confidence": "low",
                    "bucket": "D",
                    "result": {
                        "plan": "ASK",
                        "recalled_time": 1,
                        "response": {
                            "topic": "Basic concepts",
                            "question": "Easy question?",
                            "model_answer": "Simple answer",
                            "explanation": "Building foundation",
                            "difficulty": "easy",
                        },
                    },
                }

                from submissions.utils.tail_question_generator.generate_questions_routed import generate_tail_question

                student_context = {
                    "question_chain": [
                        {
                            "question": "Previous Q",
                            "student_answer": "Wrong A",
                            "is_correct": False,
                            "confidence": 2.0,
                            "difficulty": "medium",
                        }
                    ],
                    "overall_accuracy": 0.3,
                    "avg_confidence": 2.5,
                    "total_answered": 10,
                    "recent_trend": [False, False, False, True, True],
                }

                result = generate_tail_question(
                    question="Hard question",
                    model_answer="Complex answer",
                    student_answer="I don't know",
                    eval_grade=2.0,
                    recalled_time=0,
                    high_thr=4.0,
                    student_context=student_context,
                )

                self.assertFalse(result["is_correct"])
                self.assertEqual(result["bucket"], "D")
                self.assertEqual(result["tail_question"]["difficulty"], "easy")
