package com.example.voicetutor.notification

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationManager helper methods and utilities.
 * These tests focus on testable logic without requiring Android Context.
 */
class NotificationManagerHelperTest {

    @Test
    fun notificationType_values_containsAllTypes() {
        val values = NotificationType.values()
        assertEquals(5, values.size)
        assertTrue(values.contains(NotificationType.ASSIGNMENT_DUE))
        assertTrue(values.contains(NotificationType.NEW_MESSAGE))
        assertTrue(values.contains(NotificationType.GRADE_UPDATE))
        assertTrue(values.contains(NotificationType.SYSTEM_UPDATE))
        assertTrue(values.contains(NotificationType.REMINDER))
    }

    @Test
    fun notificationPriority_values_containsAllPriorities() {
        val values = NotificationPriority.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(NotificationPriority.LOW))
        assertTrue(values.contains(NotificationPriority.NORMAL))
        assertTrue(values.contains(NotificationPriority.HIGH))
        assertTrue(values.contains(NotificationPriority.URGENT))
    }

    @Test
    fun notificationData_withAllNotificationTypes_createsCorrectly() {
        val types = NotificationType.values()
        types.forEach { type ->
            val data = NotificationData(
                id = 1,
                title = "Test",
                message = "Message",
                type = type,
            )
            assertEquals(type, data.type)
        }
    }

    @Test
    fun notificationData_withAllPriorities_createsCorrectly() {
        val priorities = NotificationPriority.values()
        priorities.forEach { priority ->
            val data = NotificationData(
                id = 1,
                title = "Test",
                message = "Message",
                type = NotificationType.ASSIGNMENT_DUE,
                priority = priority,
            )
            assertEquals(priority, data.priority)
        }
    }

    @Test
    fun notificationAction_withCustomIcon_storesCorrectly() {
        val customIcon = android.R.drawable.ic_menu_edit
        val action = NotificationAction(
            title = "Edit",
            action = "edit",
            icon = customIcon,
        )
        assertEquals(customIcon, action.icon)
    }

    @Test
    fun notificationData_withEmptyActions_usesEmptyList() {
        val data = NotificationData(
            id = 1,
            title = "Test",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
            actions = emptyList(),
        )
        assertTrue(data.actions.isEmpty())
    }

    @Test
    fun notificationData_withMultipleActions_storesAll() {
        val actions = (1..10).map {
            NotificationAction("Action $it", "action_$it")
        }
        val data = NotificationData(
            id = 1,
            title = "Test",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
            actions = actions,
        )
        assertEquals(10, data.actions.size)
    }

    @Test
    fun notificationData_withLongTitle_handlesCorrectly() {
        val longTitle = "A".repeat(1000)
        val data = NotificationData(
            id = 1,
            title = longTitle,
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
        )
        assertEquals(longTitle, data.title)
    }

    @Test
    fun notificationData_withLongMessage_handlesCorrectly() {
        val longMessage = "B".repeat(1000)
        val data = NotificationData(
            id = 1,
            title = "Title",
            message = longMessage,
            type = NotificationType.ASSIGNMENT_DUE,
        )
        assertEquals(longMessage, data.message)
    }

    @Test
    fun notificationData_withNegativeId_handlesCorrectly() {
        val data = NotificationData(
            id = -1,
            title = "Test",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
        )
        assertEquals(-1, data.id)
    }

    @Test
    fun notificationAction_withEmptyTitle_handlesCorrectly() {
        val action = NotificationAction(
            title = "",
            action = "test",
        )
        assertTrue(action.title.isEmpty())
    }

    @Test
    fun notificationAction_withEmptyAction_handlesCorrectly() {
        val action = NotificationAction(
            title = "Test",
            action = "",
        )
        assertTrue(action.action.isEmpty())
    }

    @Test
    fun notificationData_copy_withModifiedFields_createsNewInstance() {
        val original = NotificationData(
            id = 1,
            title = "Original",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
        )
        val modified = original.copy(
            title = "Modified",
            priority = NotificationPriority.HIGH,
        )
        assertEquals("Modified", modified.title)
        assertEquals(NotificationPriority.HIGH, modified.priority)
        assertEquals("Original", original.title)
        assertEquals(NotificationPriority.NORMAL, original.priority)
    }

    @Test
    fun notificationAction_copy_withModifiedFields_createsNewInstance() {
        val original = NotificationAction("Original", "original")
        val modified = original.copy(title = "Modified")
        assertEquals("Modified", modified.title)
        assertEquals("original", modified.action)
        assertEquals("Original", original.title)
    }

    @Test
    fun notificationType_name_returnsCorrectName() {
        assertEquals("ASSIGNMENT_DUE", NotificationType.ASSIGNMENT_DUE.name)
        assertEquals("NEW_MESSAGE", NotificationType.NEW_MESSAGE.name)
        assertEquals("GRADE_UPDATE", NotificationType.GRADE_UPDATE.name)
        assertEquals("SYSTEM_UPDATE", NotificationType.SYSTEM_UPDATE.name)
        assertEquals("REMINDER", NotificationType.REMINDER.name)
    }

    @Test
    fun notificationPriority_name_returnsCorrectName() {
        assertEquals("LOW", NotificationPriority.LOW.name)
        assertEquals("NORMAL", NotificationPriority.NORMAL.name)
        assertEquals("HIGH", NotificationPriority.HIGH.name)
        assertEquals("URGENT", NotificationPriority.URGENT.name)
    }

    @Test
    fun notificationData_hashCode_isConsistent() {
        val data1 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val data2 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun notificationAction_hashCode_isConsistent() {
        val action1 = NotificationAction("Title", "action")
        val action2 = NotificationAction("Title", "action")
        assertEquals(action1.hashCode(), action2.hashCode())
    }

    @Test
    fun notificationData_toString_containsFields() {
        val data = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val string = data.toString()
        assertTrue(string.contains("Title") || string.contains("1"))
    }

    @Test
    fun notificationAction_toString_containsFields() {
        val action = NotificationAction("Title", "action")
        val string = action.toString()
        assertTrue(string.contains("Title") || string.contains("action"))
    }
}
