package com.spywhy.wallet.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.portfolio.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateBack: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio", color = SpyWhyColors.TextPrimary) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Portfolio Value
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Value", color = SpyWhyColors.TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "\$${state.totalValue}",
                            color = SpyWhyColors.TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val pnlColor = if (state.totalPnlPercent >= 0) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                        val pnlSign = if (state.totalPnlPercent >= 0) "+" else ""
                        Text(
                            "$pnlSign${state.totalPnlPercent}% ($pnlSign\$${state.totalPnl})",
                            color = pnlColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // PNL Timeframe selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("24H", "7D", "30D", "1Y", "ALL").forEach { period ->
                        FilterChip(
                            selected = state.selectedPeriod == period,
                            onClick = { viewModel.setPeriod(period) },
                            label = { Text(period, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                                selectedLabelColor = SpyWhyColors.AccentGreen
                            )
                        )
                    }
                }
            }

            // Holdings
            item {
                Text("Holdings", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(state.holdings) { holding ->
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
                        // Coin icon placeholder
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpyWhyColors.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(holding.symbol.take(1), color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(holding.name, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("${holding.amount} ${holding.symbol}", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("\$${holding.valueUsd}", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            val color = if (holding.pnlPercent >= 0) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                            val sign = if (holding.pnlPercent >= 0) "+" else ""
                            Text("$sign${holding.pnlPercent}%", color = color, fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
