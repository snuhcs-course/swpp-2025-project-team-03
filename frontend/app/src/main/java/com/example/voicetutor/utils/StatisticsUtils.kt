package com.example.voicetutor.utils

object StatisticsUtils {
    fun getActiveAndCompletedStats(completedFlags: List<Boolean>): Pair<Float, Float> {
        if (completedFlags.isEmpty()) return 0f to 0f
        val total = completedFlags.size
        val completed = completedFlags.count { it }
        val active = total - completed
        return (active.toFloat() / total) to (completed.toFloat() / total)
    }
}
