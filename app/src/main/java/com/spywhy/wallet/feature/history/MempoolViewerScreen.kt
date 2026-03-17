package com.spywhy.wallet.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.TransactionStatus
import com.spywhy.wallet.feature.history.viewmodel.TransactionListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MempoolViewerScreen(
    viewModel: TransactionListViewModel,
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingTxs = uiState.pendingTransactions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Mempool Viewer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpyWhyColors.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpyWhyColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SpyWhyColors.Black
            )
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Summary header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpyWhyColors.CardBackground)
                    .border(
                        width = 1.dp,
                        color = SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HourglassBottom,
                        contentDescription = null,
                        tint = SpyWhyColors.AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Unconfirmed Transactions",
                            style = MaterialTheme.typography.labelLarge,
                            color = SpyWhyColors.TextPrimary
                        )
                        Text(
                            text = "Your wallet's pending transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.TextDisabled
                        )
                    }
                }
                Text(
                    text = "${pendingTxs.size}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpyWhyColors.AccentOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pendingTxs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = SpyWhyColors.TextDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No pending transactions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpyWhyColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All transactions have been confirmed",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpyWhyColors.TextDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingTxs, key = { it.id }) { tx ->
                    MempoolTxItem(
                        transaction = tx,
                        onClick = { onTransactionClick(tx.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun MempoolTxItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val truncatedHash = if (transaction.txHash.length > 20)
        "${transaction.txHash.take(10)}...${transaction.txHash.takeLast(10)}"
    else transaction.txHash

    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(transaction.timestamp))

    // Simulated fee rate and size based on fee amount
    val feeRateSatVb = transaction.fee.multiply(java.math.BigDecimal("100000000"))
        .divide(java.math.BigDecimal("250"), 1, java.math.RoundingMode.HALF_UP)
    val txSizeBytes = 250 // Average tx size placeholder

    // Priority color based on simulated fee rate
    val priorityColor = when {
        feeRateSatVb >= java.math.BigDecimal("50") -> SpyWhyColors.AccentGreen
        feeRateSatVb >= java.math.BigDecimal("20") -> SpyWhyColors.AccentOrange
        else -> SpyWhyColors.AccentRed
    }

    val priorityLabel = when {
        feeRateSatVb >= java.math.BigDecimal("50") -> "High"
        feeRateSatVb >= java.math.BigDecimal("20") -> "Medium"
        else -> "Low"
    }

    // Priority progress (0f to 1f)
    val priorityProgress = (feeRateSatVb.toFloat() / 100f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SpyWhyColors.AccentOrange)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = truncatedHash,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SpyWhyColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextDisabled
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Fee Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextDisabled
                )
                Text(
                    text = "${feeRateSatVb.stripTrailingZeros().toPlainString()} sat/vB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Size",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextDisabled
                )
                Text(
                    text = "$txSizeBytes bytes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextDisabled
                )
                Text(
                    text = "${transaction.amount.stripTrailingZeros().toPlainString()} ${transaction.blockchain.ticker}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Priority visualization bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Priority: $priorityLabel",
                style = MaterialTheme.typography.bodySmall,
                color = priorityColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(100.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { priorityProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = priorityColor,
                trackColor = SpyWhyColors.SurfaceVariant,
            )
        }
    }
}
