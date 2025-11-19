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

class OfflineManager(private val context: Context) {

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

    /**
     * 데이터 캐시 저장
     */
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
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 캐시된 데이터 조회
     */
    fun getCachedData(key: String, maxAge: Long = 24 * 60 * 60 * 1000): String? {
        return try {
            val cacheFile = File(cacheDir, "$key.json")
            if (!cacheFile.exists()) return null

            val jsonString = cacheFile.readText()
            val jsonObject = JSONObject(jsonString)
            val timestamp = jsonObject.getLong("timestamp")

            // 만료 시간 체크
            if (System.currentTimeMillis() - timestamp > maxAge) {
                cacheFile.delete()
                return null
            }

            jsonObject.getString("data")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 오프라인 액션 추가
     */
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

    /**
     * 오프라인 액션 제거
     */
    fun removePendingAction(actionId: String) {
        val currentActions = _offlineState.value.pendingActions.filter { it.id != actionId }
        _offlineState.value = _offlineState.value.copy(
            pendingActions = currentActions,
        )
        savePendingActions()
    }

    /**
     * 오프라인 액션 재시도
     */
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

    /**
     * 모든 대기 중인 액션 실행
     */
    suspend fun syncPendingActions(): Int {
        var syncedCount = 0
        val actionsToRemove = mutableListOf<String>()

        for (action in _offlineState.value.pendingActions) {
            try {
                val success = executePendingAction(action)
                if (success) {
                    actionsToRemove.add(action.id)
                    syncedCount++
                } else if (action.retryCount >= 3) {
                    // 최대 재시도 횟수 초과 시 제거
                    actionsToRemove.add(action.id)
                }
            } catch (e: Exception) {
                // 에러 발생 시 재시도 카운트 증가
                retryPendingAction(action.id)
            }
        }

        // 성공한 액션들 제거
        actionsToRemove.forEach { actionId ->
            removePendingAction(actionId)
        }

        _offlineState.value = _offlineState.value.copy(
            lastSyncTime = System.currentTimeMillis(),
        )

        return syncedCount
    }

    /**
     * 개별 액션 실행
     */
    private suspend fun executePendingAction(action: PendingAction): Boolean {
        return when (action.type) {
            "send_message" -> {
                // TODO: 실제 메시지 전송 API 호출
                true
            }
            "submit_assignment" -> {
                // TODO: 실제 과제 제출 API 호출
                true
            }
            "update_profile" -> {
                // TODO: 실제 프로필 업데이트 API 호출
                true
            }
            else -> false
        }
    }

    /**
     * 캐시 정리
     */
    fun clearCache(): Boolean {
        return try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".json") && file.name != "pending_actions.json") {
                    file.delete()
                }
            }
            updateCacheSize()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 오래된 캐시 정리
     */
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

    /**
     * 오프라인 상태 설정
     */
    fun setOfflineMode(isOffline: Boolean) {
        _offlineState.value = _offlineState.value.copy(
            isOffline = isOffline,
        )
    }

    /**
     * 캐시 크기 업데이트
     */
    private fun updateCacheSize() {
        val cacheSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        _offlineState.value = _offlineState.value.copy(
            cacheSize = cacheSize,
        )
    }

    /**
     * 오프라인 상태 로드
     */
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
        } catch (e: Exception) {
            // 에러 발생 시 빈 상태로 초기화
        }
        updateCacheSize()
    }

    /**
     * 대기 중인 액션 저장
     */
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
        } catch (e: Exception) {
            // 저장 실패 시 무시
        }
    }

    /**
     * 캐시 크기를 사람이 읽기 쉬운 형태로 변환
     */
    fun formatCacheSize(): String {
        val bytes = _offlineState.value.cacheSize
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
