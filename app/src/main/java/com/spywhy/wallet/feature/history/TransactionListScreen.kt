package com.spywhy.wallet.feature.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.Refresh
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
import com.spywhy.wallet.feature.history.viewmodel.TxFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel,
    walletAddress: String,
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Transactions",
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

        // Filter tabs
        val filters = TxFilter.entries.toList()
        val selectedIndex = filters.indexOf(uiState.filter)

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = SpyWhyColors.Black,
            contentColor = SpyWhyColors.TextPrimary,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = SpyWhyColors.White
                    )
                }
            },
            divider = {}
        ) {
            filters.forEachIndexed { index, filter ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { viewModel.setFilter(filter) },
                    text = {
                        Text(
                            text = filter.label,
                            color = if (index == selectedIndex) SpyWhyColors.White
                            else SpyWhyColors.TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.filteredTransactions.isEmpty() && !uiState.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpyWhyColors.TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        uiState.filteredTransactions,
                        key = { it.id }
                    ) { tx ->
                        TransactionItem(
                            transaction = tx,
                            walletAddress = walletAddress,
                            onClick = { onTransactionClick(tx.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // Refresh button
            IconButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = SpyWhyColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    walletAddress: String,
    onClick: () -> Unit
) {
    val isSent = transaction.fromAddress.equals(walletAddress, ignoreCase = true)
    val displayAddress = if (isSent) transaction.toAddress else transaction.fromAddress
    val truncatedAddress = if (displayAddress.length > 16)
        "${displayAddress.take(8)}...${displayAddress.takeLast(8)}"
    else displayAddress

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(transaction.timestamp))

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
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Direction icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSent) SpyWhyColors.AccentRed.copy(alpha = 0.12f)
                    else SpyWhyColors.AccentGreen.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSent) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = if (isSent) "Sent" else "Received",
                tint = if (isSent) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Address and timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = truncatedAddress,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = SpyWhyColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextDisabled
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount and status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isSent) "-" else "+"}${transaction.amount.stripTrailingZeros().toPlainString()} ${transaction.blockchain.ticker}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSent) SpyWhyColors.AccentRed else SpyWhyColors.AccentGreen,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status = transaction.status)
                if (transaction.status == TransactionStatus.CONFIRMED && transaction.confirmations > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${transaction.confirmations} conf",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpyWhyColors.TextDisabled
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TransactionStatus) {
    val (text, color) = when (status) {
        TransactionStatus.CONFIRMED -> "Confirmed" to SpyWhyColors.AccentGreen
        TransactionStatus.PENDING -> "Pending" to SpyWhyColors.AccentOrange
        TransactionStatus.FAILED -> "Failed" to SpyWhyColors.AccentRed
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
