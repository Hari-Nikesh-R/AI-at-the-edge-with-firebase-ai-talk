package com.agenticedge.shopdemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = EdgeGreen,
    onPrimary = EdgeSurface,
    secondary = EdgeGreenDark,
    background = EdgeBackground,
    surface = EdgeSurface,
    onBackground = EdgeInk,
    onSurface = EdgeInk
)

/**
 * README capability #9, Accessibility Agent: when [largeTextMode] is on, the
 * whole app scales up type and increases contrast, simulating the agent
 * having detected repeated zoom/font-increase behavior.
 */
@Composable
fun EdgeShopTheme(largeTextMode: Boolean = false, content: @Composable () -> Unit) {
    val scale = if (largeTextMode) 1.35f else 1f

    fun style(size: Int, weight: androidx.compose.ui.text.font.FontWeight) = TextStyle(
        fontSize = (size * scale).sp,
        fontWeight = weight
    )

    val typography = Typography(
        headlineSmall = style(22, androidx.compose.ui.text.font.FontWeight.Bold),
        titleLarge = style(20, androidx.compose.ui.text.font.FontWeight.SemiBold),
        titleMedium = style(17, androidx.compose.ui.text.font.FontWeight.SemiBold),
        bodyLarge = style(16, androidx.compose.ui.text.font.FontWeight.Normal),
        bodyMedium = style(14, androidx.compose.ui.text.font.FontWeight.Normal),
        labelLarge = style(14, androidx.compose.ui.text.font.FontWeight.Medium),
        labelSmall = style(12, androidx.compose.ui.text.font.FontWeight.Medium)
    )

    val colors = if (largeTextMode) {
        LightColors.copy(
            onBackground = EdgeInk,
            onSurface = EdgeInk,
            surfaceVariant = EdgeBackground
        )
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}
