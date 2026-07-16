package com.agenticedge.shopdemo.ui.theme

import androidx.compose.ui.graphics.Color

val EdgeGreen = Color(0xFF2FA86A)
val EdgeGreenDark = Color(0xFF1E7A4C)
val EdgeBackground = Color(0xFFF5F7F6)
val EdgeSurface = Color(0xFFFFFFFF)
val EdgeInk = Color(0xFF1B2430)
val EdgeMuted = Color(0xFF6B7684)

val PersonaResearcher = Color(0xFF3D6FE0)
val PersonaImpulseBuyer = Color(0xFFE0733D)
val PersonaCasual = Color(0xFF8A8F98)
val PersonaUnknown = Color(0xFFB0B5BD)

val RiskLow = Color(0xFF2FA86A)
val RiskMedium = Color(0xFFE0A83D)
val RiskHigh = Color(0xFFD9483D)

fun personaColor(persona: String): Color = when (persona) {
    "Researcher" -> PersonaResearcher
    "ImpulseBuyer" -> PersonaImpulseBuyer
    "Casual" -> PersonaCasual
    else -> PersonaUnknown
}

fun riskColor(score: Int): Color = when {
    score >= 70 -> RiskHigh
    score >= 35 -> RiskMedium
    else -> RiskLow
}
