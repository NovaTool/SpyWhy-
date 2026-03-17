package com.spywhy.wallet.feature.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun DashboardScreen(
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCoinDetail: (String) -> Unit,
    onNavigateToHWMode: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    isWatchOnly: Boolean = false
) {
    var totalBalance by remember { mutableStateOf("$0.00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SpyWhy", color = SpyWhyColors.AccentOrange, fontWeight = FontWeight.Bold)
                },
                actions = {
                    if (!isWatchOnly) {
                        IconButton(onClick = onNavigateToHWMode) {
                            Icon(Icons.Default.Usb, "HW Mode", tint = SpyWhyColors.TextSecondary)
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = SpyWhyColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpyWhyColors.Black)
            )
        },
        containerColor = SpyWhyColors.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total balance card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Balance", color = SpyWhyColors.TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            totalBalance,
                            color = SpyWhyColors.TextPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "+0.00% today",
                            color = SpyWhyColors.AccentGreen,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!isWatchOnly) {
                        DashboardAction(Icons.Default.ArrowUpward, "Send", SpyWhyColors.AccentRed) { onNavigateToSend() }
                    }
                    DashboardAction(Icons.Default.ArrowDownward, "Receive", SpyWhyColors.AccentGreen) { onNavigateToReceive() }
                    if (!isWatchOnly) {
                        DashboardAction(Icons.Default.SwapHoriz, "Swap", SpyWhyColors.AccentOrange) { }
                    }
                    DashboardAction(Icons.Default.ShowChart, "Market", SpyWhyColors.TextPrimary) { onNavigateToMarket() }
                }
            }

            // Quick links
            item {
                val quickLinks = if (isWatchOnly)
                    listOf("Portfolio", "NFTs", "Alerts")
                else
                    listOf("Portfolio", "NFTs", "HW Mode", "Alerts")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickLinks) { label ->
                        AssistChip(
                            onClick = {
                                when (label) {
                                    "Portfolio" -> onNavigateToPortfolio()
                                    "HW Mode" -> onNavigateToHWMode()
                                }
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = SpyWhyColors.LightGray,
                                labelColor = SpyWhyColors.TextPrimary
                            )
                        )
                    }
                }
            }

            // Assets header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assets", color = SpyWhyColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { }) {
                        Text("See all", color = SpyWhyColors.AccentOrange, fontSize = 13.sp)
                    }
                }
            }

            // Asset list
            val assets = listOf(
                AssetItem("Bitcoin", "BTC", "$0.00", "0 BTC", "+0.00%", true),
                AssetItem("Ethereum", "ETH", "$0.00", "0 ETH", "+0.00%", true),
                AssetItem("Solana", "SOL", "$0.00", "0 SOL", "+0.00%", true),
                AssetItem("Litecoin", "LTC", "$0.00", "0 LTC", "+0.00%", false),
                AssetItem("Monero", "XMR", "$0.00", "0 XMR", "+0.00%", false)
            )

            items(assets) { asset ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCoinDetail(asset.ticker) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coin icon placeholder
                        Card(
                            modifier = Modifier.size(40.dp),
                            colors = CardDefaults.cardColors(containerColor = SpyWhyColors.LightGray),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(asset.ticker.take(1), color = SpyWhyColors.AccentOrange, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(asset.name, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text(asset.balance, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(asset.value, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text(
                                asset.change,
                                color = if (asset.positive) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private data class AssetItem(
    val name: String,
    val ticker: String,
    val value: String,
    val balance: String,
    val change: String,
    val positive: Boolean
)

@Composable
private fun DashboardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.size(56.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
    }
}
