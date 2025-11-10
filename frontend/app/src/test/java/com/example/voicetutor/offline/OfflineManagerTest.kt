package com.example.voicetutor.offline

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for OfflineManager data classes.
 */
class OfflineManagerTest {

    @Test
    fun cachedData_creation_withAllFields_createsCorrectInstance() {
        val data = "test data"
        val timestamp = System.currentTimeMillis()
        val version = "1.0"
        
        val cachedData = CachedData(
            data = data,
            timestamp = timestamp,
            version = version
        )
        
        assertEquals(data, cachedData.data)
        assertEquals(timestamp, cachedData.timestamp)
        assertEquals(version, cachedData.version)
    }

    @Test
    fun cachedData_creation_withDefaultVersion_usesDefault() {
        val data = "test data"
        val timestamp = System.currentTimeMillis()
        
        val cachedData = CachedData(
            data = data,
            timestamp = timestamp
        )
        
        assertEquals("1.0", cachedData.version)
    }

    @Test
    fun offlineState_creation_withAllFields_createsCorrectInstance() {
        val pendingActions = listOf(
            PendingAction("id1", "type1", "data1", 1000L, 0)
        )
        
        val offlineState = OfflineState(
            isOffline = true,
            lastSyncTime = 2000L,
            pendingActions = pendingActions,
            cacheSize = 1024L
        )
        
        assertTrue(offlineState.isOffline)
        assertEquals(2000L, offlineState.lastSyncTime)
        assertEquals(1, offlineState.pendingActions.size)
        assertEquals(1024L, offlineState.cacheSize)
    }

    @Test
    fun offlineState_creation_withDefaults_usesDefaults() {
        val offlineState = OfflineState()
        
        assertFalse(offlineState.isOffline)
        assertEquals(0L, offlineState.lastSyncTime)
        assertTrue(offlineState.pendingActions.isEmpty())
        assertEquals(0L, offlineState.cacheSize)
    }

    @Test
    fun pendingAction_creation_withAllFields_createsCorrectInstance() {
        val pendingAction = PendingAction(
            id = "action1",
            type = "send_message",
            data = "message data",
            timestamp = 1000L,
            retryCount = 2
        )
        
        assertEquals("action1", pendingAction.id)
        assertEquals("send_message", pendingAction.type)
        assertEquals("message data", pendingAction.data)
        assertEquals(1000L, pendingAction.timestamp)
        assertEquals(2, pendingAction.retryCount)
    }

    @Test
    fun pendingAction_creation_withDefaultRetryCount_usesDefault() {
        val pendingAction = PendingAction(
            id = "action1",
            type = "send_message",
            data = "message data",
            timestamp = 1000L
        )
        
        assertEquals(0, pendingAction.retryCount)
    }
}

