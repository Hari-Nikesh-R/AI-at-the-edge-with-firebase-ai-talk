package com.agenticedge.shopdemo.agent

import android.content.Context
import android.util.Log
import com.agenticedge.shopdemo.data.event.AgentEvent
import com.agenticedge.shopdemo.data.event.AppDatabase
import com.agenticedge.shopdemo.data.remote.RetrofitClient
import com.agenticedge.shopdemo.data.remote.dto.InsightRequest
import com.agenticedge.shopdemo.data.remote.dto.SessionSummaryRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * The Frontend Agent: observes user actions, runs on-device inference after
 * every event, adapts app state accordingly, and only sends compact
 * *insights* (never raw events) to the mock-Firebase backend.
 */
class AgentRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val model = FrontendAgentModel(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val userId: String = "user-" + UUID.randomUUID().toString().take(6)
    var sessionId: String = newSessionId()
        private set

    private val _agentState = MutableStateFlow(AgentState())
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _backendLog = MutableStateFlow<List<BackendLogEntry>>(emptyList())
    val backendLog: StateFlow<List<BackendLogEntry>> = _backendLog.asStateFlow()

    private val _preloadedCart = MutableStateFlow(false)
    /** True once the agent has predicted Cart is next and preloaded its data. */
    val preloadedCart: StateFlow<Boolean> = _preloadedCart.asStateFlow()

    private var offline = false

    fun setOfflineMode(isOffline: Boolean) {
        offline = isOffline
    }

    fun isOffline(): Boolean = offline

    fun onEvent(type: String, screen: String, metadata: String = "") {
        scope.launch {
            db.agentEventDao().insert(
                AgentEvent(
                    sessionId = sessionId,
                    type = type,
                    screen = screen,
                    metadata = metadata,
                    timestampMs = System.currentTimeMillis()
                )
            )
            recompute()
        }
    }

    private suspend fun recompute() {
        val events = db.agentEventDao().eventsForSession(sessionId)
        val features = FeatureExtractor.extract(events)
        val inference = model.infer(features)

        val newState = AgentState(
            persona = inference.persona,
            personaConfidence = inference.personaConfidence,
            engagement = inference.engagement,
            purchaseIntent = inference.purchaseIntent,
            predictedNextScreen = inference.predictedNextScreen,
            nextScreenConfidence = inference.nextScreenConfidence,
            eventCount = events.size,
            lastUpdatedMs = System.currentTimeMillis()
        )
        _agentState.value = newState

        // Predictive navigation (README capability #3): preload Cart data
        // before the user actually navigates there.
        if (newState.predictedNextScreen == "Cart" && newState.nextScreenConfidence >= 60) {
            _preloadedCart.value = true
        }

        sendInsight(newState)
    }

    private suspend fun sendInsight(state: AgentState) {
        if (offline) return
        runCatching {
            RetrofitClient.api.postInsight(
                InsightRequest(
                    userId = userId,
                    sessionId = sessionId,
                    persona = state.persona,
                    confidence = state.personaConfidence,
                    engagement = state.engagement,
                    purchaseIntent = state.purchaseIntent,
                    predictedNextScreen = state.predictedNextScreen
                )
            )
            appendLog(
                BackendLogEntry(
                    kind = "insight",
                    summary = "${state.persona} (${state.personaConfidence}%) · engagement=${state.engagement} · intent=${state.purchaseIntent}",
                    timestampMs = System.currentTimeMillis()
                )
            )
        }.onFailure { Log.w("AgentRepository", "postInsight failed: ${it.message}") }
    }

    /** README capability #5, Session Summarization: send one rich summary instead of raw events. */
    fun endSession(topCategory: String) {
        scope.launch {
            val events = db.agentEventDao().eventsForSession(sessionId)
            val state = _agentState.value
            val engagementBucket = when {
                state.engagement >= 70 -> "High"
                state.engagement >= 40 -> "Medium"
                else -> "Low"
            }
            val intentText = when {
                state.purchaseIntent >= 70 -> "Buy $topCategory soon"
                state.purchaseIntent >= 40 -> "Considering $topCategory"
                else -> "Browsing $topCategory"
            }

            if (!offline) {
                runCatching {
                    RetrofitClient.api.postSessionSummary(
                        SessionSummaryRequest(
                            userId = userId,
                            sessionId = sessionId,
                            interest = topCategory,
                            engagement = engagementBucket,
                            intent = intentText,
                            eventCount = events.size
                        )
                    )
                    appendLog(
                        BackendLogEntry(
                            kind = "session-summary",
                            summary = "$topCategory · $engagementBucket engagement · \"$intentText\" · ${events.size} events",
                            timestampMs = System.currentTimeMillis()
                        )
                    )
                }.onFailure { Log.w("AgentRepository", "postSessionSummary failed: ${it.message}") }
            }

            sessionId = newSessionId()
            _preloadedCart.value = false
        }
    }

    fun countEventsOfType(type: String, callback: (Int) -> Unit) {
        scope.launch {
            val count = db.agentEventDao().countByType(sessionId, type)
            callback(count)
        }
    }

    private fun appendLog(entry: BackendLogEntry) {
        _backendLog.value = (listOf(entry) + _backendLog.value).take(20)
    }

    private fun newSessionId(): String = "session-" + UUID.randomUUID().toString().take(8)

    companion object {
        @Volatile private var instance: AgentRepository? = null

        fun getInstance(context: Context): AgentRepository {
            return instance ?: synchronized(this) {
                instance ?: AgentRepository(context).also { instance = it }
            }
        }
    }
}
