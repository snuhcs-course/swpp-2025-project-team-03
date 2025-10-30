package com.example.voicetutor.utils

object StatisticsUtils {
    /**
     * 단순 예시: 완료 여부(Boolean) 리스트를 받아 활성/완료 비율을 계산합니다.
     * 반환 Pair는 (activeRatio, completedRatio)이며 합은 1.0 또는 0.0(빈 입력)입니다.
     */
    fun getActiveAndCompletedStats(completedFlags: List<Boolean>): Pair<Float, Float> {
        if (completedFlags.isEmpty()) return 0f to 0f
        val total = completedFlags.size
        val completed = completedFlags.count { it }
        val active = total - completed
        return (active.toFloat() / total) to (completed.toFloat() / total)
    }
}


