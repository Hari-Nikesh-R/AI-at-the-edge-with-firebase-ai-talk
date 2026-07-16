package com.agenticedge.shopdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.agenticedge.shopdemo.agent.AgentState
import com.agenticedge.shopdemo.ui.theme.personaColor

/**
 * Persistent strip showing what the on-device Frontend Agent currently
 * believes, so the live demo can visibly show the persona/engagement/intent
 * numbers change in real time as the presenter acts out the shopping flow.
 */
@Composable
fun AgentStatusBar(state: AgentState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(personaColor(state.persona))
            )
            Text(
                text = "  ${state.persona} · ${state.personaConfidence}%",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = "Engagement ${state.engagement} · Intent ${state.purchaseIntent}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
