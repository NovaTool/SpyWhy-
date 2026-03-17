package com.spywhy.wallet.feature.history

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.TransactionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    walletAddress: String,
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showRawHex by remember { mutableStateOf(false) }

    val isSent = transaction.fromAddress.equals(walletAddress, ignoreCase = true)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(transaction.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Transaction Details",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header: direction icon + amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSent) SpyWhyColors.AccentRed.copy(alpha = 0.12f)
                            else SpyWhyColors.AccentGreen.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSent) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = if (isSent) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isSent) "Sent" else "Received",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.TextSecondary
                    )
                    Text(
                        text = "${if (isSent) "-" else "+"}${transaction.amount.stripTrailingZeros().toPlainString()} ${transaction.blockchain.ticker}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isSent) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status badge centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val (statusText, statusColor) = when (transaction.status) {
                    TransactionStatus.CONFIRMED -> "Confirmed" to SpyWhyColors.AccentGreen
                    TransactionStatus.PENDING -> "Pending" to SpyWhyColors.AccentOrange
                    TransactionStatus.FAILED -> "Failed" to SpyWhyColors.AccentRed
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpyWhyColors.CardBackground)
                    .border(
                        width = 1.dp,
                        color = SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                // Tx Hash
                DetailField(
                    label = "Transaction Hash",
                    value = transaction.txHash,
                    isMono = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(transaction.txHash))
                    }
                )

                DetailDivider()

                // From
                DetailField(
                    label = "From",
                    value = transaction.fromAddress,
                    isMono = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(transaction.fromAddress))
                    }
                )

                DetailDivider()

                // To
                DetailField(
                    label = "To",
                    value = transaction.toAddress,
                    isMono = true,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(transaction.toAddress))
                    }
                )

                DetailDivider()

                // Amount
                DetailField(
                    label = "Amount",
                    value = "${transaction.amount.stripTrailingZeros().toPlainString()} ${transaction.blockchain.ticker}"
                )

                DetailDivider()

                // Fee
                DetailField(
                    label = "Fee",
                    value = "${transaction.fee.stripTrailingZeros().toPlainString()} ${transaction.blockchain.ticker}"
                )

                DetailDivider()

                // Confirmations
                DetailField(
                    label = "Confirmations",
                    value = "${transaction.confirmations}"
                )

                DetailDivider()

                // Timestamp
                DetailField(
                    label = "Timestamp",
                    value = dateStr
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open in explorer
            TextButton(
                onClick = {
                    val url = getExplorerUrl(transaction.blockchain, transaction.txHash)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = SpyWhyColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View in Block Explorer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextSecondary
                )
            }

            // Raw hex expandable
            if (!transaction.rawHex.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRawHex = !showRawHex }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Raw Transaction Hex",
                        style = MaterialTheme.typography.labelLarge,
                        color = SpyWhyColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (showRawHex) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (showRawHex) "Collapse" else "Expand",
                        tint = SpyWhyColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = showRawHex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpyWhyColors.SurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = transaction.rawHex,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = SpyWhyColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    isMono: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SpyWhyColors.TextDisabled
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = if (isMono) MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ) else MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = SpyWhyColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = SpyWhyColors.BorderGray)
    Spacer(modifier = Modifier.height(12.dp))
}

private fun getExplorerUrl(blockchain: Blockchain, txHash: String): String {
    return when (blockchain) {
        Blockchain.BITCOIN -> "https://mempool.space/tx/$txHash"
        Blockchain.ETHEREUM -> "https://etherscan.io/tx/$txHash"
        Blockchain.SOLANA -> "https://solscan.io/tx/$txHash"
        Blockchain.LITECOIN -> "https://blockchair.com/litecoin/transaction/$txHash"
        Blockchain.MONERO -> "https://xmrchain.net/tx/$txHash"
    }
}
