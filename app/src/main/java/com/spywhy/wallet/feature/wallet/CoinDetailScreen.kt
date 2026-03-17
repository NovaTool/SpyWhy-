package com.spywhy.wallet.feature.wallet

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    coinTicker: String,
    onNavigateBack: () -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit
) {
    val coinName = when (coinTicker) {
        "BTC" -> "Bitcoin"
        "ETH" -> "Ethereum"
        "SOL" -> "Solana"
        "LTC" -> "Litecoin"
        "XMR" -> "Monero"
        else -> coinTicker
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(coinName, color = SpyWhyColors.TextPrimary, fontSize = 18.sp)
                        Text(coinTicker, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                },
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
            // Balance card
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
                        Text("Balance", color = SpyWhyColors.TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "0 $coinTicker",
                            color = SpyWhyColors.TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$0.00 USD",
                            color = SpyWhyColors.TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToSend,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.15f),
                            contentColor = SpyWhyColors.AccentRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send")
                    }

                    Button(
                        onClick = onNavigateToReceive,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.15f),
                            contentColor = SpyWhyColors.AccentGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Receive")
                    }
                }
            }

            // Price info
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Price", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$0.00", color = SpyWhyColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "+0.00%",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = SpyWhyColors.AccentGreen,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mini chart placeholder
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = SpyWhyColors.LightGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Price Chart", color = SpyWhyColors.TextDisabled, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Recent transactions
            item {
                Text("Recent Transactions", color = SpyWhyColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            val transactions = listOf<TransactionPreview>()

            if (transactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                null,
                                tint = SpyWhyColors.TextDisabled,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions yet", color = SpyWhyColors.TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
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
                            Icon(
                                if (tx.isSend) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                null,
                                tint = if (tx.isSend) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (tx.isSend) "Sent" else "Received",
                                    color = SpyWhyColors.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(tx.date, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                            }
                            Text(
                                "${if (tx.isSend) "-" else "+"}${tx.amount} $coinTicker",
                                color = if (tx.isSend) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Address info
            item {
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
                        Icon(Icons.Default.ContentCopy, null, tint = SpyWhyColors.TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Current Address", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                            Text("No address generated", color = SpyWhyColors.TextDisabled, fontSize = 13.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private data class TransactionPreview(
    val amount: String,
    val date: String,
    val isSend: Boolean
)
