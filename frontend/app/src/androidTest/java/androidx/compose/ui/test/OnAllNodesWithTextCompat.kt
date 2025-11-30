package androidx.compose.ui.test

import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun ComposeContentTestRule.onAllNodesWithText(
    text: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
    useUnmergedTree: Boolean = false,
): SemanticsNodeInteractionCollection =
    onAllNodes(hasText(text, substring, ignoreCase), useUnmergedTree)
