package com.agenticedge.shopdemo.data.remote.dto

/** Matches POST /api/insights on the mock-Firebase Node backend exactly. */
data class InsightRequest(
    val userId: String,
    val sessionId: String,
    val persona: String,
    val confidence: Int,
    val engagement: Int,
    val purchaseIntent: Int,
    val predictedNextScreen: String
)

/** Matches POST /api/session-summary on the mock-Firebase Node backend exactly. */
data class SessionSummaryRequest(
    val userId: String,
    val sessionId: String,
    val interest: String,
    val engagement: String,
    val intent: String,
    val eventCount: Int
)
