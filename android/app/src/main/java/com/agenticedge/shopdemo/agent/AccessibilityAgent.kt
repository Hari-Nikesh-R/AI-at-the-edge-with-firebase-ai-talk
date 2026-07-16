package com.agenticedge.shopdemo.agent

/**
 * README capability #9, Accessibility Agent. Observes repeated font-size
 * increases / zoom actions and decides when to auto-switch the whole app
 * into a larger-text, higher-contrast layout.
 */
object AccessibilityAgent {

    private const val ADJUSTMENT_THRESHOLD = 3

    fun shouldActivateLargeTextMode(adjustmentCount: Int): Boolean =
        adjustmentCount >= ADJUSTMENT_THRESHOLD
}
