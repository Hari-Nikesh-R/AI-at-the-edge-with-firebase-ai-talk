package com.agenticedge.shopdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.agenticedge.shopdemo.ui.AppViewModel
import com.agenticedge.shopdemo.ui.nav.EdgeShopNavHost
import com.agenticedge.shopdemo.ui.theme.EdgeShopTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val largeTextMode by appViewModel.largeTextMode.collectAsState()
            EdgeShopTheme(largeTextMode = largeTextMode) {
                EdgeShopNavHost(appViewModel = appViewModel)
            }
        }
    }
}
