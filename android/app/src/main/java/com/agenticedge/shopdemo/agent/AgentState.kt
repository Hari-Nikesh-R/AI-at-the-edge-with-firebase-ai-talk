package com.agenticedge.shopdemo.agent

/** Live snapshot of what the on-device Frontend Agent currently believes about the user. */
data class AgentState(
    val persona: String = "Unknown",
    val personaConfidence: Int = 0,
    val engagement: Int = 0,
    val purchaseIntent: Int = 0,
    val predictedNextScreen: String = "Home",
    val nextScreenConfidence: Int = 0,
    val eventCount: Int = 0,
    val lastUpdatedMs: Long = 0L
) {
    val isResearcher: Boolean get() = persona == "Researcher"
    val isImpulseBuyer: Boolean get() = persona == "ImpulseBuyer"
}

/** One line of the "what did we send to Firebase" log shown on the Agent Dashboard. */
data class BackendLogEntry(
    val kind: String, // "insight" | "session-summary"
    val summary: String,
    val timestampMs: Long
)
