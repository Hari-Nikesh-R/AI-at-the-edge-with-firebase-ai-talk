package com.agenticedge.shopdemo.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.accessibility.AccessibilityDemoScreen
import com.agenticedge.shopdemo.ui.cart.CartScreen
import com.agenticedge.shopdemo.ui.checkout.CheckoutScreen
import com.agenticedge.shopdemo.ui.dashboard.AgentDashboardScreen
import com.agenticedge.shopdemo.ui.fraud.FraudDemoScreen
import com.agenticedge.shopdemo.ui.home.HomeScreen
import com.agenticedge.shopdemo.ui.product.ProductDetailScreen
import com.agenticedge.shopdemo.ui.search.SearchScreen

private data class BottomDestination(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Screen.Home, "Home", Icons.Filled.Home),
    BottomDestination(Screen.Search, "Search", Icons.Filled.Search),
    BottomDestination(Screen.Cart, "Cart", Icons.Filled.ShoppingCart),
    BottomDestination(Screen.Dashboard, "Agent", Icons.Filled.Insights)
)

@Composable
fun EdgeShopNavHost(appViewModel: AppViewModel) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val cartItems by appViewModel.cartItems.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.screen.route,
                        onClick = {
                            navController.navigate(dest.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = {
                            val badge = if (dest.screen == Screen.Cart && cartItems.isNotEmpty()) " (${cartItems.size})" else ""
                            Text(dest.label + badge)
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    appViewModel = appViewModel,
                    onProductClick = { navController.navigate(Screen.ProductDetail.routeFor(it)) },
                    onOpenAccessibilityDemo = { navController.navigate(Screen.Accessibility.route) },
                    onOpenFraudDemo = { navController.navigate(Screen.Fraud.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    appViewModel = appViewModel,
                    onProductClick = { navController.navigate(Screen.ProductDetail.routeFor(it)) }
                )
            }
            composable(
                Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { })
            ) { backStack ->
                val productId = backStack.arguments?.getString("productId").orEmpty()
                ProductDetailScreen(
                    productId = productId,
                    appViewModel = appViewModel,
                    onBack = { navController.popBackStack() },
                    onGoToCart = {
                        navController.navigate(Screen.Cart.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    appViewModel = appViewModel,
                    onCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    appViewModel = appViewModel,
                    onOrderPlaced = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                AgentDashboardScreen(appViewModel = appViewModel)
            }
            composable(Screen.Accessibility.route) {
                AccessibilityDemoScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Fraud.route) {
                FraudDemoScreen(appViewModel = appViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
