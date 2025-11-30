package com.example.voicetutor.offline

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CachedData<T>(
    val data: T,
    val timestamp: Long,
    val version: String = "1.0",
)

data class OfflineState(
    val isOffline: Boolean = false,
    val lastSyncTime: Long = 0L,
    val pendingActions: List<PendingAction> = emptyList(),
    val cacheSize: Long = 0L,
)

data class PendingAction(
    val id: String,
    val type: String,
    val data: String,
    val timestamp: Long,
    val retryCount: Int = 0,
)

class OfflineManager(context: Context) {

    private val _offlineState = MutableStateFlow(OfflineState())
    val offlineState: StateFlow<OfflineState> = _offlineState.asStateFlow()

    private val cacheDir = File(context.cacheDir, "offline_cache")
    private val pendingActionsFile = File(cacheDir, "pending_actions.json")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        loadOfflineState()
    }

    fun <T> cacheData(key: String, data: T): Boolean {
        return try {
            val cachedData = CachedData(
                data = data,
                timestamp = System.currentTimeMillis(),
            )

            val jsonObject = JSONObject().apply {
                put("data", data.toString())
                put("timestamp", cachedData.timestamp)
                put("version", cachedData.version)
            }

            val cacheFile = File(cacheDir, "$key.json")
            cacheFile.writeText(jsonObject.toString())
            updateCacheSize()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getCachedData(key: String, maxAge: Long = 24 * 60 * 60 * 1000): String? {
        return try {
            val cacheFile = File(cacheDir, "$key.json")
            if (!cacheFile.exists()) return null

            val jsonString = cacheFile.readText()
            val jsonObject = JSONObject(jsonString)
            val timestamp = jsonObject.getLong("timestamp")

            if (System.currentTimeMillis() - timestamp > maxAge) {
                cacheFile.delete()
                return null
            }

            jsonObject.getString("data")
        } catch (_: Exception) {
            null
        }
    }

    fun addPendingAction(type: String, data: String): String {
        val actionId = "${type}_${System.currentTimeMillis()}"
        val pendingAction = PendingAction(
            id = actionId,
            type = type,
            data = data,
            timestamp = System.currentTimeMillis(),
        )

        val currentActions = _offlineState.value.pendingActions.toMutableList()
        currentActions.add(pendingAction)

        _offlineState.value = _offlineState.value.copy(
            pendingActions = currentActions,
        )

        savePendingActions()
        return actionId
    }

    fun removePendingAction(actionId: String) {
        val currentActions = _offlineState.value.pendingActions.filter { it.id != actionId }
        _offlineState.value = _offlineState.value.copy(
            pendingActions = currentActions,
        )
        savePendingActions()
    }

    fun retryPendingAction(actionId: String) {
        val currentActions = _offlineState.value.pendingActions.toMutableList()
        val actionIndex = currentActions.indexOfFirst { it.id == actionId }

        if (actionIndex != -1) {
            val action = currentActions[actionIndex]
            currentActions[actionIndex] = action.copy(
                retryCount = action.retryCount + 1,
            )

            _offlineState.value = _offlineState.value.copy(
                pendingActions = currentActions,
            )
            savePendingActions()
        }
    }

    fun syncPendingActions(): Int {
        var syncedCount = 0
        val actionsToRemove = mutableListOf<String>()

        for (action in _offlineState.value.pendingActions) {
            try {
                val success = executePendingAction(action)
                if (success) {
                    actionsToRemove.add(action.id)
                    syncedCount++
                } else if (action.retryCount >= 3) {
                    actionsToRemove.add(action.id)
                }
            } catch (_: Exception) {
                retryPendingAction(action.id)
            }
        }

        actionsToRemove.forEach { actionId ->
            removePendingAction(actionId)
        }

        _offlineState.value = _offlineState.value.copy(
            lastSyncTime = System.currentTimeMillis(),
        )

        return syncedCount
    }

    private fun executePendingAction(action: PendingAction): Boolean {
        return when (action.type) {
            "send_message" -> {
                true
            }
            "submit_assignment" -> {
                true
            }
            "update_profile" -> {
                true
            }
            else -> false
        }
    }

    fun clearCache(): Boolean {
        return try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".json") && file.name != "pending_actions.json") {
                    file.delete()
                }
            }
            updateCacheSize()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun clearOldCache(maxAge: Long = 7 * 24 * 60 * 60 * 1000): Int {
        var deletedCount = 0
        val currentTime = System.currentTimeMillis()

        cacheDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".json") && file.name != "pending_actions.json") {
                if (currentTime - file.lastModified() > maxAge) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
        }

        updateCacheSize()
        return deletedCount
    }

    fun setOfflineMode(isOffline: Boolean) {
        _offlineState.value = _offlineState.value.copy(
            isOffline = isOffline,
        )
    }

    private fun updateCacheSize() {
        val cacheSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        _offlineState.value = _offlineState.value.copy(
            cacheSize = cacheSize,
        )
    }

    private fun loadOfflineState() {
        try {
            if (pendingActionsFile.exists()) {
                val jsonString = pendingActionsFile.readText()
                val jsonArray = JSONArray(jsonString)
                val pendingActions = mutableListOf<PendingAction>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val action = PendingAction(
                        id = jsonObject.getString("id"),
                        type = jsonObject.getString("type"),
                        data = jsonObject.getString("data"),
                        timestamp = jsonObject.getLong("timestamp"),
                        retryCount = jsonObject.optInt("retryCount", 0),
                    )
                    pendingActions.add(action)
                }

                _offlineState.value = _offlineState.value.copy(
                    pendingActions = pendingActions,
                )
            }
        } catch (_: Exception) {
        }
        updateCacheSize()
    }

    private fun savePendingActions() {
        try {
            val jsonArray = JSONArray()
            _offlineState.value.pendingActions.forEach { action ->
                val jsonObject = JSONObject().apply {
                    put("id", action.id)
                    put("type", action.type)
                    put("data", action.data)
                    put("timestamp", action.timestamp)
                    put("retryCount", action.retryCount)
                }
                jsonArray.put(jsonObject)
            }

            pendingActionsFile.writeText(jsonArray.toString())
        } catch (_: Exception) {
        }
    }
}
