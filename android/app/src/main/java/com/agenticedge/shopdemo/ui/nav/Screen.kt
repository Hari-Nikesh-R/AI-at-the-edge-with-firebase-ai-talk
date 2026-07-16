package com.agenticedge.shopdemo.ui.nav

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object ProductDetail : Screen("product/{productId}") {
        fun routeFor(productId: String) = "product/$productId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Dashboard : Screen("dashboard")
    object Accessibility : Screen("accessibility")
    object Fraud : Screen("fraud")
}
