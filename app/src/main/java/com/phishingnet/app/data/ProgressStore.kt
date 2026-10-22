package com.phishingnet.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf

/**
 * Saves the best quiz score for each course part on the device.
 * Backed by SharedPreferences and mirrored into Compose state so the UI updates instantly.
 */
class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    /** moduleId -> best score in percent (0..100). Missing = not attempted. */
    val bestScores = mutableStateMapOf<Int, Int>().apply {
        CourseData.modules.forEach { m ->
            val v = prefs.getInt(key(m.id), -1)
            if (v >= 0) put(m.id, v)
        }
    }

    fun recordScore(moduleId: Int, percent: Int) {
        val previous = bestScores[moduleId] ?: -1
        if (percent > previous) {
            bestScores[moduleId] = percent
            prefs.edit().putInt(key(moduleId), percent).apply()
        }
    }

    fun isPassed(moduleId: Int): Boolean =
        (bestScores[moduleId] ?: -1) >= CourseData.PASS_PERCENT

    /** The first part the user hasn't passed yet (or null when everything is passed). */
    fun nextUnfinished() = CourseData.modules.firstOrNull { !isPassed(it.id) }

    fun passedCount(): Int = CourseData.modules.count { isPassed(it.id) }

    fun reset() {
        bestScores.clear()
        prefs.edit().clear().apply()
    }

    private fun key(id: Int) = "best_$id"
}
