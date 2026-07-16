package com.agenticedge.shopdemo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agenticedge.shopdemo.agent.AccessibilityAgent
import com.agenticedge.shopdemo.agent.AgentRepository
import com.agenticedge.shopdemo.agent.NotificationAgent
import com.agenticedge.shopdemo.agent.SelfHealingFormAgent
import com.agenticedge.shopdemo.data.event.AppDatabase
import com.agenticedge.shopdemo.data.event.EventType
import com.agenticedge.shopdemo.data.model.CartItem
import com.agenticedge.shopdemo.data.model.Product
import com.agenticedge.shopdemo.data.model.ProductCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared, activity-scoped state for the whole demo: the Frontend Agent
 * (via [AgentRepository]), the cart, and the lighter-weight capability
 * stubs (notifications, offline recommendations, accessibility, self-healing
 * forms) that build on top of the agent's observations.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val agentRepository: AgentRepository = AgentRepository.getInstance(application)
    private val db = AppDatabase.getInstance(application)

    val agentState = agentRepository.agentState
    val backendLog = agentRepository.backendLog
    val preloadedCart = agentRepository.preloadedCart

    // ---- Cart ----
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) {
            current[idx] = current[idx].copy(quantity = current[idx].quantity + 1)
        } else {
            current.add(CartItem(product))
        }
        _cartItems.value = current
        agentRepository.onEvent(EventType.ADD_TO_CART, "ProductDetail", product.category)
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    fun cartTotal(): Int = _cartItems.value.sumOf { it.product.discountedPrice * it.quantity }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // ---- Offline mode (README capability #6, Offline Recommendations) ----
    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    fun setOfflineMode(isOffline: Boolean) {
        _offlineMode.value = isOffline
        agentRepository.setOfflineMode(isOffline)
    }

    fun recordProductView(product: Product) {
        agentRepository.onEvent(EventType.PRODUCT_VIEW, "ProductDetail", product.category)
        viewModelScope.launch {
            db.productPreferenceDao().recordView(product.category, System.currentTimeMillis())
        }
    }

    private val _offlineRecommendations = MutableStateFlow<List<Product>>(emptyList())
    val offlineRecommendations: StateFlow<List<Product>> = _offlineRecommendations.asStateFlow()

    fun refreshOfflineRecommendations() {
        viewModelScope.launch {
            val topCategories = db.productPreferenceDao().topCategories().map { it.category }
            val ranked = if (topCategories.isEmpty()) {
                ProductCatalog.all.shuffled().take(4)
            } else {
                topCategories.flatMap { ProductCatalog.byCategory(it) }.distinct().take(4)
            }
            _offlineRecommendations.value = ranked
        }
    }

    // ---- Notifications (README capability #4) ----
    private var promosIgnored = 0
    private var educationalRead = 0

    private val _notificationSuggestion = MutableStateFlow(
        NotificationAgent.decide(0, 0, agentState.value)
    )
    val notificationSuggestion: StateFlow<NotificationAgent.Suggestion> = _notificationSuggestion.asStateFlow()

    fun recordPromoIgnored() {
        promosIgnored++
        agentRepository.onEvent(EventType.NOTIFICATION_IGNORED, "Notifications")
        refreshNotificationSuggestion()
    }

    fun recordEducationalRead() {
        educationalRead++
        agentRepository.onEvent(EventType.NOTIFICATION_ENGAGED, "Notifications")
        refreshNotificationSuggestion()
    }

    fun refreshNotificationSuggestion() {
        _notificationSuggestion.value = NotificationAgent.decide(promosIgnored, educationalRead, agentState.value)
    }

    // ---- Accessibility Agent (README capability #9) ----
    private val _accessibilityAdjustments = MutableStateFlow(0)
    val accessibilityAdjustments: StateFlow<Int> = _accessibilityAdjustments.asStateFlow()

    val largeTextMode: StateFlow<Boolean> get() = _largeTextMode
    private val _largeTextMode = MutableStateFlow(false)

    fun recordAccessibilityAdjustment() {
        val newCount = _accessibilityAdjustments.value + 1
        _accessibilityAdjustments.value = newCount
        agentRepository.onEvent(EventType.ACCESSIBILITY_ADJUSTMENT, "Accessibility")
        if (AccessibilityAgent.shouldActivateLargeTextMode(newCount)) {
            _largeTextMode.value = true
        }
    }

    fun resetAccessibility() {
        _accessibilityAdjustments.value = 0
        _largeTextMode.value = false
    }

    // ---- Self-Healing Forms (README capability #8) ----
    private val fieldAbandonCounts = mutableMapOf<String, Int>()
    private var checkoutAttempts = 0

    private val _formSuggestion = MutableStateFlow<SelfHealingFormAgent.Suggestion?>(null)
    val formSuggestion: StateFlow<SelfHealingFormAgent.Suggestion?> = _formSuggestion.asStateFlow()

    fun recordCheckoutAttempt() {
        checkoutAttempts++
    }

    fun recordFieldAbandon(field: String) {
        fieldAbandonCounts[field] = (fieldAbandonCounts[field] ?: 0) + 1
        agentRepository.onEvent(EventType.CHECKOUT_FIELD_ABANDON, "Checkout", field)
        _formSuggestion.value = SelfHealingFormAgent.analyze(fieldAbandonCounts, checkoutAttempts)
    }
}
