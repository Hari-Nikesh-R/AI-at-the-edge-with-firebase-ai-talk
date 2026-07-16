package com.agenticedge.shopdemo.agent

/**
 * README capability #10, Edge Fraud Detection. A deterministic weighted-rule
 * risk score computed entirely on-device before a transaction would ever
 * reach a backend — no TFLite model needed for this stub, just a transparent
 * scoring rule so the demo can show exactly why a score is high or low.
 */
object FraudRiskEngine {

    data class RiskResult(val score: Int, val reasons: List<String>)

    fun computeRiskScore(
        unknownDevice: Boolean,
        vpnOrUnknownLocation: Boolean,
        rapidTransactions: Boolean
    ): RiskResult {
        var score = 5
        val reasons = mutableListOf<String>()

        if (unknownDevice) {
            score += 40
            reasons += "Unknown device"
        }
        if (vpnOrUnknownLocation) {
            score += 35
            reasons += "VPN / unrecognized location"
        }
        if (rapidTransactions) {
            score += 20
            reasons += "Rapid transaction volume"
        }
        if (reasons.isEmpty()) {
            reasons += "Known device, familiar location, normal volume"
        }

        return RiskResult(score.coerceIn(0, 100), reasons)
    }
}
