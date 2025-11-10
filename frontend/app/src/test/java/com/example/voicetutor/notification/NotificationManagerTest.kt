package com.example.voicetutor.notification

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NotificationManager enums and data classes.
 */
class NotificationManagerTest {

    @Test
    fun notificationType_enumValues_areCorrect() {
        assertEquals(5, NotificationType.values().size)
        assertTrue(NotificationType.values().contains(NotificationType.ASSIGNMENT_DUE))
        assertTrue(NotificationType.values().contains(NotificationType.NEW_MESSAGE))
        assertTrue(NotificationType.values().contains(NotificationType.GRADE_UPDATE))
        assertTrue(NotificationType.values().contains(NotificationType.SYSTEM_UPDATE))
        assertTrue(NotificationType.values().contains(NotificationType.REMINDER))
    }

    @Test
    fun notificationPriority_enumValues_areCorrect() {
        assertEquals(4, NotificationPriority.values().size)
        assertTrue(NotificationPriority.values().contains(NotificationPriority.LOW))
        assertTrue(NotificationPriority.values().contains(NotificationPriority.NORMAL))
        assertTrue(NotificationPriority.values().contains(NotificationPriority.HIGH))
        assertTrue(NotificationPriority.values().contains(NotificationPriority.URGENT))
    }

    @Test
    fun notificationData_creation_withAllFields_createsCorrectInstance() {
        val actions = listOf(
            NotificationAction("Action 1", "action1"),
            NotificationAction("Action 2", "action2")
        )
        val notificationData = NotificationData(
            id = 1,
            title = "Test Notification",
            message = "Test Message",
            type = NotificationType.ASSIGNMENT_DUE,
            priority = NotificationPriority.HIGH,
            autoCancel = false,
            actions = actions
        )
        
        assertEquals(1, notificationData.id)
        assertEquals("Test Notification", notificationData.title)
        assertEquals("Test Message", notificationData.message)
        assertEquals(NotificationType.ASSIGNMENT_DUE, notificationData.type)
        assertEquals(NotificationPriority.HIGH, notificationData.priority)
        assertFalse(notificationData.autoCancel)
        assertEquals(2, notificationData.actions.size)
    }

    @Test
    fun notificationData_creation_withDefaults_usesDefaults() {
        val notificationData = NotificationData(
            id = 1,
            title = "Test",
            message = "Message",
            type = NotificationType.NEW_MESSAGE
        )
        
        assertEquals(NotificationPriority.NORMAL, notificationData.priority)
        assertTrue(notificationData.autoCancel)
        assertTrue(notificationData.actions.isEmpty())
    }

    @Test
    fun notificationAction_creation_withAllFields_createsCorrectInstance() {
        val action = NotificationAction(
            title = "Test Action",
            action = "test_action",
            icon = android.R.drawable.ic_dialog_info
        )
        
        assertEquals("Test Action", action.title)
        assertEquals("test_action", action.action)
        assertEquals(android.R.drawable.ic_dialog_info, action.icon)
    }

    @Test
    fun notificationAction_creation_withDefaultIcon_usesDefault() {
        val action = NotificationAction(
            title = "Test Action",
            action = "test_action"
        )
        
        assertEquals(android.R.drawable.ic_dialog_info, action.icon)
    }
}

