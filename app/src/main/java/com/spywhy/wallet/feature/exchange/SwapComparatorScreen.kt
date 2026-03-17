package com.spywhy.wallet.feature.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.spywhy.wallet.feature.exchange.viewmodel.SwapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapComparatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: SwapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swap / Exchange", color = SpyWhyColors.TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From / To selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("From", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var fromExpanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { fromExpanded = true }) {
                                    Text(state.fromCoin, color = SpyWhyColors.TextPrimary, fontSize = 18.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = SpyWhyColors.TextSecondary)
                                }
                                DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                                    listOf("BTC", "ETH", "SOL", "LTC", "XMR").forEach { coin ->
                                        DropdownMenuItem(
                                            text = { Text(coin) },
                                            onClick = { viewModel.setFromCoin(coin); fromExpanded = false }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = state.fromAmount,
                                onValueChange = { viewModel.setFromAmount(it) },
                                modifier = Modifier.width(150.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SpyWhyColors.TextPrimary,
                                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                                    focusedBorderColor = SpyWhyColors.AccentGreen,
                                    unfocusedBorderColor = SpyWhyColors.BorderGray
                                ),
                                placeholder = { Text("0.00", color = SpyWhyColors.TextDisabled) },
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.swapCoins() },
                                modifier = Modifier
                                    .background(SpyWhyColors.LightGray, RoundedCornerShape(50))
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.SwapVert, "Swap", tint = SpyWhyColors.AccentGreen)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("To", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var toExpanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { toExpanded = true }) {
                                    Text(state.toCoin, color = SpyWhyColors.TextPrimary, fontSize = 18.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = SpyWhyColors.TextSecondary)
                                }
                                DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                                    listOf("BTC", "ETH", "SOL", "LTC", "XMR").forEach { coin ->
                                        DropdownMenuItem(
                                            text = { Text(coin) },
                                            onClick = { viewModel.setToCoin(coin); toExpanded = false }
                                        )
                                    }
                                }
                            }
                            Text(
                                text = state.estimatedReceive,
                                color = SpyWhyColors.AccentGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !state.kycFilter,
                        onClick = { viewModel.toggleKYCFilter() },
                        label = { Text("No KYC") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SpyWhyColors.AccentGreen
                        )
                    )
                    FilterChip(
                        selected = state.sortBySpeed,
                        onClick = { viewModel.toggleSortBySpeed() },
                        label = { Text("Fastest") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.2f),
                            selectedLabelColor = SpyWhyColors.AccentOrange
                        )
                    )
                    FilterChip(
                        selected = state.sortByRate,
                        onClick = { viewModel.toggleSortByRate() },
                        label = { Text("Best Rate") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SpyWhyColors.AccentGreen
                        )
                    )
                }
            }

            // Quotes from providers
            item {
                Text("Compare Providers", color = SpyWhyColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SpyWhyColors.AccentGreen)
                    }
                }
            }

            items(state.quotes) { quote ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectQuote(quote) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (quote == state.selectedQuote) SpyWhyColors.AccentGreen.copy(alpha = 0.1f)
                        else SpyWhyColors.MediumGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(quote.provider, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Rate: ${quote.rate}", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                            Text("Fee: ${quote.fee}%", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                            if (quote.kycRequired) {
                                Text("KYC Required", color = SpyWhyColors.AccentOrange, fontSize = 11.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${quote.toAmount} ${state.toCoin}",
                                color = SpyWhyColors.AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(quote.estimatedTime, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Swap button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.executeSwap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.selectedQuote != null && !state.isSwapping,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentGreen,
                        contentColor = SpyWhyColors.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSwapping) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SpyWhyColors.Black)
                    } else {
                        Text("Swap Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
