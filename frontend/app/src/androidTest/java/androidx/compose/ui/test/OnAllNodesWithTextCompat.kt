package androidx.compose.ui.test

import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Backport helper for Compose UI versions that do not expose [onAllNodesWithText].
 *
 * Delegates to [onAllNodes] combined with [hasText] so existing test code can remain unchanged.
 */
fun ComposeContentTestRule.onAllNodesWithText(
    text: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
    useUnmergedTree: Boolean = false,
): SemanticsNodeInteractionCollection =
    onAllNodes(hasText(text, substring, ignoreCase), useUnmergedTree)
