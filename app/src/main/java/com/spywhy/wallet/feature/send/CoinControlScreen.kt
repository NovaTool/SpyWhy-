package com.spywhy.wallet.feature.send

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.UTXO
import com.spywhy.wallet.feature.send.viewmodel.SendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinControlScreen(
    viewModel: SendViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalSelectedSats = uiState.selectedUtxos.sumOf { it.amount }
    val totalSelectedBtc = totalSelectedSats.toBigDecimal().movePointLeft(8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Coin Control",
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
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Select UTXOs to spend",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${uiState.availableUtxos.size} unspent outputs available",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextDisabled
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.availableUtxos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No UTXOs available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpyWhyColors.TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableUtxos, key = { it.id }) { utxo ->
                    UTXOItem(
                        utxo = utxo,
                        isSelected = uiState.selectedUtxos.any { it.id == utxo.id },
                        ticker = uiState.selectedBlockchain.ticker,
                        onToggleSelect = { viewModel.toggleUtxoSelection(utxo) },
                        onToggleLock = { viewModel.lockUtxo(utxo.id, !utxo.isLocked) }
                    )
                }
            }
        }

        // Bottom bar with total
        HorizontalDivider(color = SpyWhyColors.BorderGray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpyWhyColors.DarkGray)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary
                )
                Text(
                    text = "${uiState.selectedUtxos.size} UTXOs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary
                )
                Text(
                    text = "${totalSelectedBtc.stripTrailingZeros().toPlainString()} ${uiState.selectedBlockchain.ticker}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UTXOItem(
    utxo: UTXO,
    isSelected: Boolean,
    ticker: String,
    onToggleSelect: () -> Unit,
    onToggleLock: () -> Unit
) {
    val amountBtc = utxo.amount.toBigDecimal().movePointLeft(8)
    val truncatedHash = if (utxo.txHash.length > 16)
        "${utxo.txHash.take(8)}...${utxo.txHash.takeLast(8)}"
    else utxo.txHash

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) SpyWhyColors.White.copy(alpha = 0.06f)
                else SpyWhyColors.CardBackground
            )
            .border(
                width = 1.dp,
                color = if (isSelected) SpyWhyColors.White.copy(alpha = 0.2f)
                else SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            enabled = !utxo.isLocked,
            colors = CheckboxDefaults.colors(
                checkedColor = SpyWhyColors.AccentGreen,
                uncheckedColor = SpyWhyColors.BorderGray,
                checkmarkColor = SpyWhyColors.Black,
                disabledCheckedColor = SpyWhyColors.TextDisabled,
                disabledUncheckedColor = SpyWhyColors.TextDisabled
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = truncatedHash,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = if (utxo.isLocked) SpyWhyColors.TextDisabled
                else SpyWhyColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Output #${utxo.outputIndex}",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextDisabled
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${amountBtc.stripTrailingZeros().toPlainString()} $ticker",
                style = MaterialTheme.typography.bodyMedium,
                color = if (utxo.isLocked) SpyWhyColors.TextDisabled
                else SpyWhyColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${utxo.amount} sats",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextDisabled
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onToggleLock,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (utxo.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = if (utxo.isLocked) "Unlock UTXO" else "Lock UTXO",
                tint = if (utxo.isLocked) SpyWhyColors.AccentOrange
                else SpyWhyColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
