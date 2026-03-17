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
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    var priceAlerts by remember { mutableStateOf(true) }
    var txNotifications by remember { mutableStateOf(true) }
    var dailySummary by remember { mutableStateOf(false) }
    var largeTransactions by remember { mutableStateOf(true) }
    var largeThreshold by remember { mutableStateOf("1000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = SpyWhyColors.TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Alerts", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            NotificationToggle(
                title = "Price Alerts",
                subtitle = "Get notified when prices hit your targets",
                icon = Icons.Default.TrendingUp,
                checked = priceAlerts,
                onCheckedChange = { priceAlerts = it }
            )

            NotificationToggle(
                title = "Transaction Notifications",
                subtitle = "Notify on incoming and outgoing transactions",
                icon = Icons.Default.SwapVert,
                checked = txNotifications,
                onCheckedChange = { txNotifications = it }
            )

            NotificationToggle(
                title = "Daily Portfolio Summary",
                subtitle = "Daily overview of your portfolio performance",
                icon = Icons.Default.Assessment,
                checked = dailySummary,
                onCheckedChange = { dailySummary = it }
            )

            Spacer(Modifier.height(16.dp))
            Text("Thresholds", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            NotificationToggle(
                title = "Large Transaction Alerts",
                subtitle = "Alert when transactions exceed threshold",
                icon = Icons.Default.AccountBalanceWallet,
                checked = largeTransactions,
                onCheckedChange = { largeTransactions = it }
            )

            if (largeTransactions) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Alert Threshold (USD)", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = largeThreshold,
                            onValueChange = { largeThreshold = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("$", color = SpyWhyColors.TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpyWhyColors.TextPrimary,
                                unfocusedTextColor = SpyWhyColors.TextPrimary,
                                focusedBorderColor = SpyWhyColors.AccentOrange,
                                unfocusedBorderColor = SpyWhyColors.TextDisabled,
                                cursorColor = SpyWhyColors.AccentOrange
                            )
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("100", "500", "1000", "5000").forEach { preset ->
                                FilterChip(
                                    selected = largeThreshold == preset,
                                    onClick = { largeThreshold = preset },
                                    label = { Text("$$preset", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.2f),
                                        selectedLabelColor = SpyWhyColors.AccentOrange
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            Icon(icon, null, tint = SpyWhyColors.TextPrimary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
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
