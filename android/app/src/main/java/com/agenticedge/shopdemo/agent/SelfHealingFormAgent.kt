package com.agenticedge.shopdemo.agent

/**
 * README capability #8, Self-Healing Forms. Watches which fields users
 * abandon on and suggests a concrete UX fix once abandonment crosses a
 * threshold, mirroring the "80% users abandon at Address Line 2" example.
 */
object SelfHealingFormAgent {

    data class Suggestion(val field: String, val action: String, val reason: String)

    private const val ABANDON_THRESHOLD = 0.6

    fun analyze(fieldAbandonCounts: Map<String, Int>, totalAttempts: Int): Suggestion? {
        if (totalAttempts == 0) return null
        val worst = fieldAbandonCounts.maxByOrNull { it.value } ?: return null
        val ratio = (worst.value.toDouble() / totalAttempts).coerceIn(0.0, 1.0)
        if (ratio < ABANDON_THRESHOLD) return null

        return Suggestion(
            field = worst.key,
            action = "Mark optional & add example hint",
            reason = "${(ratio * 100).toInt()}% of users abandon the form at \"${worst.key}\""
        )
    }
}
