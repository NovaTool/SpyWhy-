package com.spywhy.wallet.feature.send

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.send.viewmodel.SendViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSendScreen(
    viewModel: SendViewModel,
    onNavigateBack: () -> Unit,
    onSendAll: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalAmount = uiState.batchRecipients.sumOf { recipient ->
        try {
            BigDecimal(recipient.amount)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }
    val totalWithFee = totalAmount + uiState.currentFeeAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Batch Send",
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
                text = "Send ${uiState.selectedBlockchain.ticker} to multiple recipients",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(uiState.batchRecipients) { index, recipient ->
                RecipientRow(
                    index = index,
                    address = recipient.address,
                    amount = recipient.amount,
                    ticker = uiState.selectedBlockchain.ticker,
                    canRemove = uiState.batchRecipients.size > 1,
                    onAddressChanged = { viewModel.updateBatchRecipient(index, address = it) },
                    onAmountChanged = { viewModel.updateBatchRecipient(index, amount = it) },
                    onRemove = { viewModel.removeBatchRecipient(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.addBatchRecipient() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = SpyWhyColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Recipient",
                        color = SpyWhyColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom summary
        HorizontalDivider(color = SpyWhyColors.BorderGray)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpyWhyColors.DarkGray)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recipients",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextSecondary
                )
                Text(
                    text = "${uiState.batchRecipients.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextSecondary
                )
                Text(
                    text = "${totalAmount.stripTrailingZeros().toPlainString()} ${uiState.selectedBlockchain.ticker}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fee",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextSecondary
                )
                Text(
                    text = "${uiState.currentFeeAmount.stripTrailingZeros().toPlainString()} ${uiState.selectedBlockchain.ticker}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = SpyWhyColors.BorderGray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total + Fees",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${totalWithFee.stripTrailingZeros().toPlainString()} ${uiState.selectedBlockchain.ticker}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.White,
                    contentColor = SpyWhyColors.Black
                ),
                enabled = uiState.batchRecipients.all {
                    it.address.isNotBlank() && it.amount.isNotBlank()
                }
            ) {
                Text(
                    text = "Send All",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SpyWhyColors.Black
                )
            }
        }
    }
}

@Composable
private fun RecipientRow(
    index: Int,
    address: String,
    amount: String,
    ticker: String,
    canRemove: Boolean,
    onAddressChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onRemove: () -> Unit
) {
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
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipient #${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove recipient",
                        tint = SpyWhyColors.AccentRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Recipient address",
                    color = SpyWhyColors.TextDisabled
                )
            },
            singleLine = true,
            colors = spyWhyTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChanged(it.filter { c -> c.isDigit() || c == '.' }) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "0.00", color = SpyWhyColors.TextDisabled)
            },
            suffix = {
                Text(text = ticker, color = SpyWhyColors.TextSecondary)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = spyWhyTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
