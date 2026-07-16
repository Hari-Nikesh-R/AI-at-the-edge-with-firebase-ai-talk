package com.agenticedge.shopdemo.agent

/**
 * README capability #4, Smart Notification Agent. No real push notifications
 * (no FCM wiring needed for the demo) — this decides what an in-app
 * notification banner should say based on how the user has been treating
 * past suggestions.
 */
object NotificationAgent {

    data class Suggestion(val title: String, val body: String)

    fun decide(promosIgnored: Int, educationalRead: Int, state: AgentState): Suggestion {
        return when {
            promosIgnored >= 2 && educationalRead == 0 ->
                Suggestion(
                    "Promotions paused",
                    "You've skipped the last $promosIgnored offers, so we stopped sending promotional pushes."
                )
            educationalRead >= 1 || state.isResearcher ->
                Suggestion(
                    "Specs & guides prioritized",
                    "You read specs and reviews, so we're surfacing buying guides instead of discount pushes."
                )
            state.isImpulseBuyer ->
                Suggestion(
                    "Flash deal nearby",
                    "Your activity suggests you respond well to timely offers — showing a deal now."
                )
            else ->
                Suggestion(
                    "Observing your activity",
                    "Not enough signal yet — the agent is still learning what you care about."
                )
        }
    }
}
