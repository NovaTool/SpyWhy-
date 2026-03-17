package com.spywhy.wallet.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    onNavigateBack: () -> Unit
) {
    var portfolioWidget by remember { mutableStateOf(false) }
    var priceTickerWidget by remember { mutableStateOf(false) }
    var quickSendWidget by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widgets", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpyWhyColors.Black)
            )
        },
        containerColor = SpyWhyColors.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.DarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Add widgets to your home screen for quick access to wallet info. Long-press your home screen to add widgets.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Available Widgets", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))

            WidgetCard(
                title = "Portfolio Balance",
                description = "Shows your total portfolio value on the home screen",
                icon = Icons.Default.AccountBalanceWallet,
                enabled = portfolioWidget,
                onToggle = { portfolioWidget = it }
            )

            WidgetCard(
                title = "Price Ticker",
                description = "Live price updates for your favorite coins",
                icon = Icons.Default.ShowChart,
                enabled = priceTickerWidget,
                onToggle = { priceTickerWidget = it }
            )

            WidgetCard(
                title = "Quick Send",
                description = "One-tap send to your favorite contacts",
                icon = Icons.Default.Send,
                enabled = quickSendWidget,
                onToggle = { quickSendWidget = it }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Widget data refreshes every 15 minutes to conserve battery.",
                color = SpyWhyColors.TextDisabled,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (enabled) SpyWhyColors.AccentOrange else SpyWhyColors.TextPrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                Text(description, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SpyWhyColors.AccentOrange,
                    checkedTrackColor = SpyWhyColors.AccentOrange.copy(alpha = 0.3f),
                    uncheckedThumbColor = SpyWhyColors.TextDisabled,
                    uncheckedTrackColor = SpyWhyColors.DarkGray
                )
            )
        }
    }
}
