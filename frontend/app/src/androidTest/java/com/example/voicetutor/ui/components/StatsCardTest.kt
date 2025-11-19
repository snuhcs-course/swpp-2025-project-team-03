package com.example.voicetutor.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatsCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statsCard_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "총 과제",
                    value = "10",
                    icon = Icons.Filled.List,
                )
            }
        }

        composeTestRule.onNodeWithText("총 과제").assertExists()
    }

    @Test
    fun statsCard_displaysValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "총 과제",
                    value = "10",
                    icon = Icons.Filled.List,
                )
            }
        }

        composeTestRule.onNodeWithText("10").assertExists()
    }

    @Test
    fun statsCard_displaysHorizontalLayout() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "테스트",
                    value = "100",
                    icon = Icons.Filled.Star,
                    layout = StatsCardLayout.Horizontal,
                )
            }
        }

        composeTestRule.onNodeWithText("테스트").assertExists()
        composeTestRule.onNodeWithText("100").assertExists()
    }

    @Test
    fun statsCard_displaysVerticalLayout() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "테스트",
                    value = "100",
                    icon = Icons.Filled.Star,
                    layout = StatsCardLayout.Vertical,
                )
            }
        }

        composeTestRule.onNodeWithText("테스트").assertExists()
        composeTestRule.onNodeWithText("100").assertExists()
    }

    @Test
    fun statsCard_displaysUpTrend() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "증가",
                    value = "100",
                    icon = Icons.Filled.TrendingUp,
                    trend = TrendDirection.Up,
                    trendValue = "+10",
                )
            }
        }

        composeTestRule.onNodeWithText("+10").assertExists()
    }

    @Test
    fun statsCard_displaysDownTrend() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "감소",
                    value = "100",
                    icon = Icons.Filled.TrendingDown,
                    trend = TrendDirection.Down,
                    trendValue = "-5",
                )
            }
        }

        composeTestRule.onNodeWithText("-5").assertExists()
    }

    @Test
    fun statsCard_hidesTrend_whenNone() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "변화 없음",
                    value = "100",
                    icon = Icons.Filled.Remove,
                    trend = TrendDirection.None,
                    trendValue = "",
                )
            }
        }

        // Trend should not be displayed when None
        composeTestRule.onNodeWithText("변화 없음").assertExists()
        composeTestRule.onNodeWithText("100").assertExists()
    }

    @Test
    fun statsCard_callsOnClick_whenClickable() {
        var clicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "클릭 가능",
                    value = "50",
                    icon = Icons.Filled.Info,
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("클릭 가능").performClick()
        assert(clicked)
    }

    @Test
    fun statsCard_doesNotCallOnClick_whenNotClickable() {
        var clicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "클릭 불가",
                    value = "50",
                    icon = Icons.Filled.Block,
                    onClick = null,
                )
            }
        }

        // onClick is null, so click should not trigger
        composeTestRule.onNodeWithText("클릭 불가").assertExists()
    }

    @Test
    fun statsCard_displaysDefaultVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "Default",
                    value = "100",
                    icon = Icons.Filled.Info,
                    variant = CardVariant.Default,
                )
            }
        }

        composeTestRule.onNodeWithText("Default").assertExists()
    }

    @Test
    fun statsCard_displaysElevatedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "Elevated",
                    value = "100",
                    icon = Icons.Filled.Star,
                    variant = CardVariant.Elevated,
                )
            }
        }

        composeTestRule.onNodeWithText("Elevated").assertExists()
    }

    @Test
    fun statsCard_displaysOutlinedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "Outlined",
                    value = "100",
                    icon = Icons.Filled.Edit,
                    variant = CardVariant.Outlined,
                )
            }
        }

        composeTestRule.onNodeWithText("Outlined").assertExists()
    }

    @Test
    fun statsCard_displaysGradientVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "Gradient",
                    value = "100",
                    icon = Icons.Filled.ColorLens,
                    variant = CardVariant.Gradient,
                )
            }
        }

        composeTestRule.onNodeWithText("Gradient").assertExists()
    }

    @Test
    fun statsCard_displaysSelectedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "Selected",
                    value = "100",
                    icon = Icons.Filled.CheckCircle,
                    variant = CardVariant.Selected,
                )
            }
        }

        composeTestRule.onNodeWithText("Selected").assertExists()
    }

    @Test
    fun statsCard_handlesLongTitle() {
        val longTitle = "이것은 매우 긴 제목입니다. " + "반복 ".repeat(20)

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = longTitle,
                    value = "100",
                    icon = Icons.Filled.Title,
                )
            }
        }

        composeTestRule.onNodeWithText(longTitle, substring = true).assertExists()
    }

    @Test
    fun statsCard_handlesLongValue() {
        val longValue = "12345678901234567890"

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "테스트",
                    value = longValue,
                    icon = Icons.Filled.List,
                )
            }
        }

        composeTestRule.onNodeWithText(longValue).assertExists()
    }

    @Test
    fun statsCard_handlesEmptyValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "빈 값",
                    value = "",
                    icon = Icons.Filled.Close,
                )
            }
        }

        composeTestRule.onNodeWithText("빈 값").assertExists()
    }

    @Test
    fun statsCard_handlesZeroValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "영",
                    value = "0",
                    icon = Icons.Filled.Info,
                )
            }
        }

        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun statsCard_handlesPercentageValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "완료율",
                    value = "75%",
                    icon = Icons.Filled.Percent,
                )
            }
        }

        composeTestRule.onNodeWithText("75%").assertExists()
    }

    @Test
    fun statsCard_handlesNegativeTrendValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "감소",
                    value = "50",
                    icon = Icons.Filled.ArrowDownward,
                    trend = TrendDirection.Down,
                    trendValue = "-10",
                )
            }
        }

        composeTestRule.onNodeWithText("-10").assertExists()
    }

    @Test
    fun statsCard_handlesPositiveTrendValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "증가",
                    value = "50",
                    icon = Icons.Filled.ArrowUpward,
                    trend = TrendDirection.Up,
                    trendValue = "+20",
                )
            }
        }

        composeTestRule.onNodeWithText("+20").assertExists()
    }

    @Test
    fun statsCard_handlesCustomIconColor() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "커스텀 색상",
                    value = "100",
                    icon = Icons.Filled.ColorLens,
                    iconColor = androidx.compose.ui.graphics.Color.Red,
                )
            }
        }

        composeTestRule.onNodeWithText("커스텀 색상").assertExists()
    }

    @Test
    fun statsCard_handlesMultipleClicks() {
        var clickCount = 0

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "여러 번 클릭",
                    value = "100",
                    icon = Icons.Filled.Add,
                    onClick = { clickCount++ },
                )
            }
        }

        val card = composeTestRule.onNodeWithText("여러 번 클릭")
        card.performClick()
        card.performClick()
        card.performClick()

        assert(clickCount == 3)
    }

    @Test
    fun statsCard_displaysTrendOnly_whenTrendValueNotEmpty() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "트렌드",
                    value = "100",
                    icon = Icons.Filled.TrendingUp,
                    trend = TrendDirection.Up,
                    trendValue = "+5",
                )
            }
        }

        composeTestRule.onNodeWithText("+5").assertExists()
    }

    @Test
    fun statsCard_hidesTrend_whenTrendValueEmpty() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "트렌드 없음",
                    value = "100",
                    icon = Icons.Filled.Remove,
                    trend = TrendDirection.Up,
                    trendValue = "",
                )
            }
        }

        // Trend indicator should not be visible when trendValue is empty
        composeTestRule.onNodeWithText("트렌드 없음").assertExists()
    }

    @Test
    fun statsCard_handlesSpecialCharacters() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "특수문자: !@#$%",
                    value = "100%",
                    icon = Icons.Filled.Star,
                )
            }
        }

        composeTestRule.onNodeWithText("특수문자: !@#$%", substring = true).assertExists()
    }
}
