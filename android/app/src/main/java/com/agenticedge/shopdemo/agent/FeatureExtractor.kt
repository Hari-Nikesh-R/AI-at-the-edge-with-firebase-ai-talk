package com.agenticedge.shopdemo.agent

import com.agenticedge.shopdemo.data.event.AgentEvent
import com.agenticedge.shopdemo.data.event.EventType
import kotlin.math.min

/**
 * Converts a session's raw events into the 8-feature vector the on-device
 * TFLite model was trained on (see android/ml/train_agent_model.py for the
 * matching contract). Order and normalization here MUST match the training
 * script exactly.
 */
object FeatureExtractor {

    const val FEATURE_COUNT = 8

    fun extract(events: List<AgentEvent>): FloatArray {
        val total = events.size
        if (total == 0) return FloatArray(FEATURE_COUNT)

        fun ratio(type: String): Float = events.count { it.type == type }.toFloat() / total

        val searchRatio = ratio(EventType.SEARCH)
        val viewRatio = ratio(EventType.PRODUCT_VIEW)
        val compareRatio = ratio(EventType.COMPARE)
        val reviewRatio = ratio(EventType.REVIEW_READ)
        val cartRatio = ratio(EventType.ADD_TO_CART)

        val sorted = events.sortedBy { it.timestampMs }
        val durationSec = (sorted.last().timestampMs - sorted.first().timestampMs) / 1000.0
        val sessionDurationNorm = min(durationSec / 600.0, 1.0).toFloat()

        val gapsSec = sorted.zipWithNext { a, b -> (b.timestampMs - a.timestampMs) / 1000.0 }
        val avgDwellSec = if (gapsSec.isEmpty()) 0.0 else gapsSec.average()
        val avgDwellNorm = min(avgDwellSec / 60.0, 1.0).toFloat()

        val eventCountNorm = min(total / 30.0, 1.0).toFloat()

        return floatArrayOf(
            searchRatio,
            viewRatio,
            compareRatio,
            reviewRatio,
            cartRatio,
            sessionDurationNorm,
            avgDwellNorm,
            eventCountNorm
        )
    }
}
