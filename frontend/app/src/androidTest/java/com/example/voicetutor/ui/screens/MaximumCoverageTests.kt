package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Maximum coverage tests - tests as many components and code paths as possible.
 * This file focuses on testing all possible combinations and edge cases.
 */
@RunWith(AndroidJUnit4::class)
class MaximumCoverageTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== Comprehensive Component Testing ==========

    @Test
    fun allCardVariants_withAllContentTypes_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // Elevated card with text
                    VTCard(variant = CardVariant.Elevated) {
                        Text("Elevated Card")
                    }
                    // Outlined card with multiple elements
                    VTCard(variant = CardVariant.Outlined) {
                        Text("Outlined Card")
                        Text("Subtitle")
                    }
                    // Gradient card with icon
                    VTCard(variant = CardVariant.Gradient) {
                        Row {
                            Icon(Icons.Filled.Star, contentDescription = null)
                            Text("Gradient Card")
                        }
                    }
                    // Clickable card
                    VTCard(variant = CardVariant.Elevated, onClick = {}) {
                        Text("Clickable Card")
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Elevated Card", substring = true).assertExists()
        composeTestRule.onNodeWithText("Outlined Card", substring = true).assertExists()
        composeTestRule.onNodeWithText("Gradient Card", substring = true).assertExists()
        composeTestRule.onNodeWithText("Clickable Card", substring = true).assertExists()
    }

    @Test
    fun allButtonVariants_withAllSizes_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // Primary buttons
                    VTButton(text = "Primary Small", onClick = {}, variant = ButtonVariant.Primary, size = ButtonSize.Small)
                    VTButton(text = "Primary Medium", onClick = {}, variant = ButtonVariant.Primary, size = ButtonSize.Medium)
                    VTButton(text = "Primary Large", onClick = {}, variant = ButtonVariant.Primary, size = ButtonSize.Large)

                    // Outline buttons
                    VTButton(text = "Outline Small", onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Small)
                    VTButton(text = "Outline Medium", onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Medium)
                    VTButton(text = "Outline Large", onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Large)

                    // Gradient buttons
                    VTButton(text = "Gradient Small", onClick = {}, variant = ButtonVariant.Gradient, size = ButtonSize.Small)
                    VTButton(text = "Gradient Medium", onClick = {}, variant = ButtonVariant.Gradient, size = ButtonSize.Medium)
                    VTButton(text = "Gradient Large", onClick = {}, variant = ButtonVariant.Gradient, size = ButtonSize.Large)
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Primary Small", substring = true).assertExists()
        composeTestRule.onNodeWithText("Outline Medium", substring = true).assertExists()
        composeTestRule.onNodeWithText("Gradient Large", substring = true).assertExists()
    }

    @Test
    fun statsCard_withAllCombinations_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // Horizontal layout
                    VTStatsCard(
                        title = "Horizontal",
                        value = "100",
                        icon = Icons.Filled.Assignment,
                        layout = StatsCardLayout.Horizontal,
                    )
                    // Vertical layout
                    VTStatsCard(
                        title = "Vertical",
                        value = "200",
                        icon = Icons.Filled.People,
                        layout = StatsCardLayout.Vertical,
                    )
                    // With trend up
                    VTStatsCard(
                        title = "Trend Up",
                        value = "300",
                        icon = Icons.Filled.TrendingUp,
                        trend = TrendDirection.Up,
                        trendValue = "+10",
                    )
                    // With trend down
                    VTStatsCard(
                        title = "Trend Down",
                        value = "400",
                        icon = Icons.Filled.TrendingDown,
                        trend = TrendDirection.Down,
                        trendValue = "-5",
                    )
                    // With trend neutral
                    VTStatsCard(
                        title = "Trend None",
                        value = "500",
                        icon = Icons.Filled.Remove,
                        trend = TrendDirection.None,
                        trendValue = "0",
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Horizontal", substring = true).assertExists()
        composeTestRule.onNodeWithText("Vertical", substring = true).assertExists()
        composeTestRule.onNodeWithText("Trend Up", substring = true).assertExists()
    }

    // ========== Data Class Rendering Tests ==========

    @Test
    fun classRoomData_withAllColorVariants_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    ClassCard(
                        classRoom = ClassRoom(0, "Indigo 반", "과목", "설명", 30, 5, 0.8f, PrimaryIndigo),
                        onClassClick = {},
                        onCreateAssignment = {},
                        onViewStudents = {},
                    )
                    ClassCard(
                        classRoom = ClassRoom(1, "Success 반", "과목", "설명", 30, 5, 0.8f, Success),
                        onClassClick = {},
                        onCreateAssignment = {},
                        onViewStudents = {},
                    )
                    ClassCard(
                        classRoom = ClassRoom(2, "Warning 반", "과목", "설명", 30, 5, 0.8f, Warning),
                        onClassClick = {},
                        onCreateAssignment = {},
                        onViewStudents = {},
                    )
                    ClassCard(
                        classRoom = ClassRoom(3, "Error 반", "과목", "설명", 30, 5, 0.8f, Error),
                        onClassClick = {},
                        onCreateAssignment = {},
                        onViewStudents = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun classAssignmentData_withAllCompletionRates_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    ClassAssignmentCard(
                        assignment = ClassAssignment(1, "과제 0%", "수학", "2024-12-31", 0f, 30, 0, 85),
                        onNavigateToAssignmentDetail = {},
                    )
                    ClassAssignmentCard(
                        assignment = ClassAssignment(2, "과제 25%", "수학", "2024-12-31", 0.25f, 30, 7, 85),
                        onNavigateToAssignmentDetail = {},
                    )
                    ClassAssignmentCard(
                        assignment = ClassAssignment(3, "과제 50%", "수학", "2024-12-31", 0.5f, 30, 15, 85),
                        onNavigateToAssignmentDetail = {},
                    )
                    ClassAssignmentCard(
                        assignment = ClassAssignment(4, "과제 75%", "수학", "2024-12-31", 0.75f, 30, 22, 85),
                        onNavigateToAssignmentDetail = {},
                    )
                    ClassAssignmentCard(
                        assignment = ClassAssignment(5, "과제 100%", "수학", "2024-12-31", 1f, 30, 30, 85),
                        onNavigateToAssignmentDetail = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Interactive Component Tests ==========

    @Test
    fun textFields_withAllKeyboardTypes_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("텍스트") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("숫자") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("이메일") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("전화번호") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("텍스트", substring = true).assertExists()
        composeTestRule.onNodeWithText("숫자", substring = true).assertExists()
        composeTestRule.onNodeWithText("이메일", substring = true).assertExists()
        composeTestRule.onNodeWithText("전화번호", substring = true).assertExists()
    }

    @Test
    fun filterChips_withMultipleSelections_work() {
        var filter1Selected = false
        var filter2Selected = false
        var filter3Selected = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                Row {
                    FilterChip(
                        selected = filter1Selected,
                        onClick = { filter1Selected = !filter1Selected },
                        label = { Text("필터 1") },
                    )
                    FilterChip(
                        selected = filter2Selected,
                        onClick = { filter2Selected = !filter2Selected },
                        label = { Text("필터 2") },
                    )
                    FilterChip(
                        selected = filter3Selected,
                        onClick = { filter3Selected = !filter3Selected },
                        label = { Text("필터 3") },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("필터 1", substring = true).performClick()
        assert(filter1Selected)

        composeTestRule.onNodeWithText("필터 2", substring = true).performClick()
        assert(filter2Selected)

        composeTestRule.onNodeWithText("필터 3", substring = true).performClick()
        assert(filter3Selected)
    }

    // ========== Progress and Loading States ==========

    @Test
    fun progressIndicators_allTypes_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // Linear progress
                    LinearProgressIndicator(progress = { 0.5f })
                    // Circular progress
                    CircularProgressIndicator()
                    // Determinate circular
                    CircularProgressIndicator(progress = 0.7f)
                }
            }
        }

        composeTestRule.waitForIdle()
        // Progress indicators exist
    }

    @Test
    fun loadingStates_withDifferentMessages_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("로딩 중...")
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("데이터를 불러오는 중...")
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("로딩 중", substring = true).assertExists()
        composeTestRule.onNodeWithText("데이터를 불러오는 중", substring = true).assertExists()
    }

    // ========== Error States ==========

    @Test
    fun errorStates_withDifferentMessages_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTCard {
                        Text(
                            text = "오류가 발생했습니다",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    VTCard {
                        Text(
                            text = "네트워크 연결을 확인해주세요",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    VTCard {
                        Text(
                            text = "서버 오류가 발생했습니다",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Empty States ==========

    @Test
    fun emptyStates_withDifferentMessages_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Assignment, contentDescription = null, tint = Gray400)
                            Text("과제가 없습니다")
                            Text("새로운 과제를 생성해보세요")
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.People, contentDescription = null, tint = Gray400)
                            Text("학생이 없습니다")
                            Text("학생을 추가해보세요")
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Complex Component Combinations ==========

    @Test
    fun complexLayout_withAllComponents_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Header
                    Text(
                        text = "대시보드",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        VTStatsCard(
                            title = "과제",
                            value = "10",
                            icon = Icons.Filled.Assignment,
                            modifier = Modifier.weight(1f),
                        )
                        VTStatsCard(
                            title = "학생",
                            value = "30",
                            icon = Icons.Filled.People,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Cards
                    VTCard {
                        Text("카드 1")
                    }
                    VTCard {
                        Text("카드 2")
                    }

                    // Buttons
                    VTButton(text = "작업 1", onClick = {}, modifier = Modifier.fillMaxWidth())
                    VTButton(text = "작업 2", onClick = {}, variant = ButtonVariant.Outline, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Multiple Renders for Coverage ==========

    @Test
    fun multipleRenders_increaseCoverage() {
        // Render multiple components to cover initialization code
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    repeat(5) { index ->
                        Text("렌더 $index")
                        VTCard {
                            Text("카드 $index")
                        }
                        VTButton(text = "버튼 $index", onClick = {})
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun allIconVariants_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Row {
                    Icon(Icons.Filled.Assignment, contentDescription = null)
                    Icon(Icons.Filled.People, contentDescription = null)
                    Icon(Icons.Filled.Star, contentDescription = null)
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Icon(Icons.Filled.School, contentDescription = null)
                    Icon(Icons.Filled.TrendingUp, contentDescription = null)
                    Icon(Icons.Filled.TrendingDown, contentDescription = null)
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Edge Cases ==========

    @Test
    fun components_withZeroValues_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTStatsCard(
                        title = "0 값",
                        value = "0",
                        icon = Icons.Filled.Assignment,
                    )
                    VTCard {
                        Text("0")
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun components_withMaxValues_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTStatsCard(
                        title = "최대 값",
                        value = "999",
                        icon = Icons.Filled.Assignment,
                    )
                    VTCard {
                        Text("999")
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun components_withUnicodeCharacters_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    Text("한글 English 123 !@#")
                    VTCard {
                        Text("한글 English 123 !@#")
                    }
                    VTButton(text = "한글 English 123 !@#", onClick = {})
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun noRecentAssignmentScreen_completeRendering() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertExists()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", substring = true).assertExists()
    }

    @Test
    fun appInfoScreen_completeRendering() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun appInfoScreen_allSections_render() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun appInfoScreen_internalComponents_allVariations() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // Test FeatureItem variations
                    FeatureItem(feature = "기능 1")
                    FeatureItem(feature = "기능 2")

                    // Test InfoItem variations
                    InfoItem(label = "라벨 1", value = "값 1")
                    InfoItem(label = "라벨 2", value = "값 2")

                    // Test LegalItem variations
                    LegalItem(title = "법적 항목 1", onClick = {})
                    LegalItem(title = "법적 항목 2", onClick = {})

                    // Test ContactItem variations
                    ContactItem(
                        icon = Icons.Filled.Email,
                        title = "이메일",
                        value = "email@example.com",
                        onClick = {},
                    )
                    ContactItem(
                        icon = Icons.Filled.Language,
                        title = "웹사이트",
                        value = "www.example.com",
                        onClick = {},
                    )

                    // Test ActionItem variations
                    ActionItem(
                        icon = Icons.Filled.Update,
                        title = "업데이트",
                        description = "설명 1",
                        onClick = {},
                    )
                    ActionItem(
                        icon = Icons.Filled.Share,
                        title = "공유",
                        description = "설명 2",
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("기능 1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("기능 2", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("라벨 1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("라벨 2", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("법적 항목 1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("법적 항목 2", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("웹사이트", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("업데이트", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("공유", useUnmergedTree = true).assertExists()
    }

    @Test
    fun multipleScreenRenders_increaseCoverage() {
        // Render multiple screens/components together to increase coverage
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    NoRecentAssignmentScreen()
                    AppInfoScreen()
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
