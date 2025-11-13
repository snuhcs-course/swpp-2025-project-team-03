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
    
    @Test
    fun notificationData_copy_createsNewInstance() {
        val original = NotificationData(
            id = 1,
            title = "Original",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE
        )
        
        val copy = original.copy(title = "Modified")
        
        assertEquals("Modified", copy.title)
        assertEquals(original.message, copy.message)
    }
    
    @Test
    fun notificationData_equality_worksCorrectly() {
        val data1 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val data2 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val data3 = NotificationData(2, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        
        assertEquals(data1, data2)
        assertNotEquals(data1, data3)
    }
    
    @Test
    fun notificationAction_equality_worksCorrectly() {
        val action1 = NotificationAction("Action", "test")
        val action2 = NotificationAction("Action", "test")
        val action3 = NotificationAction("Action", "other")
        
        assertEquals(action1, action2)
        assertNotEquals(action1, action3)
    }
    
    @Test
    fun notificationType_allTypes_haveUniqueNames() {
        val names = NotificationType.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
    
    @Test
    fun notificationPriority_allPriorities_haveUniqueNames() {
        val names = NotificationPriority.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
    
    @Test
    fun notificationData_withMultipleActions_storesCorrectly() {
        val actions = listOf(
            NotificationAction("Action 1", "action1"),
            NotificationAction("Action 2", "action2"),
            NotificationAction("Action 3", "action3")
        )
        
        val data = NotificationData(
            id = 1,
            title = "Test",
            message = "Message",
            type = NotificationType.ASSIGNMENT_DUE,
            actions = actions
        )
        
        assertEquals(3, data.actions.size)
        assertEquals("Action 1", data.actions[0].title)
        assertEquals("Action 2", data.actions[1].title)
        assertEquals("Action 3", data.actions[2].title)
    }

    @Test
    fun notificationData_hashCode_worksCorrectly() {
        val data1 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val data2 = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun notificationAction_hashCode_worksCorrectly() {
        val action1 = NotificationAction("Action", "test")
        val action2 = NotificationAction("Action", "test")
        
        assertEquals(action1.hashCode(), action2.hashCode())
    }

    @Test
    fun notificationData_toString_containsFields() {
        val data = NotificationData(1, "Title", "Message", NotificationType.ASSIGNMENT_DUE)
        val toString = data.toString()
        
        assertTrue(toString.contains("Title"))
        assertTrue(toString.contains("Message"))
        assertTrue(toString.contains("ASSIGNMENT_DUE"))
    }

    @Test
    fun notificationAction_toString_containsFields() {
        val action = NotificationAction("Action", "test")
        val toString = action.toString()
        
        assertTrue(toString.contains("Action"))
        assertTrue(toString.contains("test"))
    }

    @Test
    fun notificationData_withEmptyMessage_handlesEmptyString() {
        val data = NotificationData(1, "Title", "", NotificationType.ASSIGNMENT_DUE)
        
        assertTrue(data.message.isEmpty())
    }

    @Test
    fun notificationData_withLongMessage_handlesLongString() {
        val longMessage = "A".repeat(1000)
        val data = NotificationData(1, "Title", longMessage, NotificationType.ASSIGNMENT_DUE)
        
        assertEquals(1000, data.message.length)
    }

    @Test
    fun notificationAction_withCustomIcon_usesCustomIcon() {
        val customIcon = android.R.drawable.ic_menu_edit
        val action = NotificationAction("Action", "test", customIcon)
        
        assertEquals(customIcon, action.icon)
    }

    @Test
    fun notificationType_valueOf_worksCorrectly() {
        assertEquals(NotificationType.ASSIGNMENT_DUE, NotificationType.valueOf("ASSIGNMENT_DUE"))
        assertEquals(NotificationType.NEW_MESSAGE, NotificationType.valueOf("NEW_MESSAGE"))
        assertEquals(NotificationType.GRADE_UPDATE, NotificationType.valueOf("GRADE_UPDATE"))
        assertEquals(NotificationType.SYSTEM_UPDATE, NotificationType.valueOf("SYSTEM_UPDATE"))
        assertEquals(NotificationType.REMINDER, NotificationType.valueOf("REMINDER"))
    }

    @Test
    fun notificationPriority_valueOf_worksCorrectly() {
        assertEquals(NotificationPriority.LOW, NotificationPriority.valueOf("LOW"))
        assertEquals(NotificationPriority.NORMAL, NotificationPriority.valueOf("NORMAL"))
        assertEquals(NotificationPriority.HIGH, NotificationPriority.valueOf("HIGH"))
        assertEquals(NotificationPriority.URGENT, NotificationPriority.valueOf("URGENT"))
    }

    @Test
    fun notificationData_withSpecialCharacters_handlesCorrectly() {
        val data = NotificationData(
            1,
            "Title & Test < > \" '",
            "Message with\nnewlines\tand\ttabs",
            NotificationType.ASSIGNMENT_DUE
        )
        
        assertTrue(data.title.contains("&"))
        assertTrue(data.message.contains("\n"))
    }
}

