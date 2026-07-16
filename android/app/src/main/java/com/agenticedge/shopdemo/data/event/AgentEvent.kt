package com.agenticedge.shopdemo.data.event

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Event types the Frontend Agent observes, mirroring the README's shopping demo actions. */
object EventType {
    const val SEARCH = "search"
    const val PRODUCT_VIEW = "view"
    const val COMPARE = "compare"
    const val REVIEW_READ = "review"
    const val ADD_TO_CART = "cart"
    const val CHECKOUT_FIELD_ABANDON = "checkout_field_abandon"
    const val NOTIFICATION_IGNORED = "notification_ignored"
    const val NOTIFICATION_ENGAGED = "notification_engaged"
    const val ACCESSIBILITY_ADJUSTMENT = "accessibility_adjustment"
}

@Entity(tableName = "agent_events")
data class AgentEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val type: String,
    val screen: String,
    val metadata: String = "",
    val timestampMs: Long
)
