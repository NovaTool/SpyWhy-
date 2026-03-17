package com.spywhy.wallet.feature.send

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.feature.send.viewmodel.FeeLevel
import com.spywhy.wallet.feature.send.viewmodel.SendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SendViewModel,
    onNavigateBack: () -> Unit,
    onScanQr: () -> Unit,
    onSelectFee: () -> Unit,
    onCoinControl: () -> Unit,
    onBatchSend: () -> Unit,
    onReviewTransaction: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Send",
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

            // Blockchain Selector
            BlockchainSelector(
                selected = uiState.selectedBlockchain,
                onSelected = { viewModel.selectBlockchain(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Address Input
            Text(
                text = "Recipient Address",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.toAddress,
                onValueChange = { viewModel.updateAddress(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Enter ${uiState.selectedBlockchain.ticker} address",
                        color = SpyWhyColors.TextDisabled
                    )
                },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { viewModel.pasteAddress() }) {
                            Icon(
                                imageVector = Icons.Filled.ContentPaste,
                                contentDescription = "Paste",
                                tint = SpyWhyColors.TextSecondary
                            )
                        }
                        IconButton(onClick = onScanQr) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = SpyWhyColors.TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = spyWhyTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // Address poisoning warning
            AnimatedVisibility(visible = uiState.addressPoisoningWarning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpyWhyColors.AccentOrange.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = SpyWhyColors.AccentOrange.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = SpyWhyColors.AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This address looks similar to a recently used address. " +
                                "Verify carefully to avoid address poisoning attacks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpyWhyColors.AccentOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "0.00", color = SpyWhyColors.TextDisabled)
                },
                trailingIcon = {
                    TextButton(
                        onClick = { viewModel.setMaxAmount() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = SpyWhyColors.AccentGreen
                        )
                    ) {
                        Text(
                            text = "MAX",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                suffix = {
                    Text(
                        text = uiState.selectedBlockchain.ticker,
                        color = SpyWhyColors.TextSecondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = spyWhyTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // USD conversion
            if (uiState.usdEquivalent.isNotEmpty()) {
                Text(
                    text = "~ ${uiState.usdEquivalent} USD",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fee Selector
            Text(
                text = "Network Fee",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FeeSelectorBar(
                selectedLevel = uiState.feeLevel,
                onLevelSelected = { viewModel.selectFeeLevel(it) },
                onCustomClick = onSelectFee
            )

            if (uiState.feeEstimateDisplay.isNotEmpty()) {
                Text(
                    text = uiState.feeEstimateDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced options row
            val isUtxoChain = uiState.selectedBlockchain == Blockchain.BITCOIN ||
                    uiState.selectedBlockchain == Blockchain.LITECOIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isUtxoChain) {
                    TextButton(onClick = onCoinControl) {
                        Text(
                            text = "Coin Control",
                            color = SpyWhyColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                TextButton(onClick = onBatchSend) {
                    Text(
                        text = "Batch Send",
                        color = SpyWhyColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Review Transaction Button
            Button(
                onClick = onReviewTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.White,
                    contentColor = SpyWhyColors.Black
                ),
                enabled = uiState.toAddress.isNotBlank() && uiState.amount.isNotBlank()
            ) {
                Text(
                    text = "Review Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SpyWhyColors.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BlockchainSelector(
    selected: Blockchain,
    onSelected: (Blockchain) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = "Blockchain",
        style = MaterialTheme.typography.labelLarge,
        color = SpyWhyColors.TextSecondary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SpyWhyColors.CardBackground)
                .border(
                    width = 1.dp,
                    color = SpyWhyColors.BorderGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${selected.displayName} (${selected.ticker})",
                style = MaterialTheme.typography.bodyLarge,
                color = SpyWhyColors.TextPrimary
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Select",
                tint = SpyWhyColors.TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SpyWhyColors.MediumGray)
        ) {
            Blockchain.entries.forEach { blockchain ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${blockchain.displayName} (${blockchain.ticker})",
                            color = if (blockchain == selected) SpyWhyColors.AccentGreen
                            else SpyWhyColors.TextPrimary
                        )
                    },
                    onClick = {
                        onSelected(blockchain)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FeeSelectorBar(
    selectedLevel: FeeLevel,
    onLevelSelected: (FeeLevel) -> Unit,
    onCustomClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val levels = listOf(FeeLevel.LOW, FeeLevel.MEDIUM, FeeLevel.HIGH, FeeLevel.CUSTOM)
        levels.forEach { level ->
            val isSelected = selectedLevel == level
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) SpyWhyColors.White.copy(alpha = 0.12f)
                        else SpyWhyColors.CardBackground
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) SpyWhyColors.White.copy(alpha = 0.3f)
                        else SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        if (level == FeeLevel.CUSTOM) onCustomClick()
                        else onLevelSelected(level)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) SpyWhyColors.White
                    else SpyWhyColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun spyWhyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SpyWhyColors.TextPrimary,
    unfocusedTextColor = SpyWhyColors.TextPrimary,
    cursorColor = SpyWhyColors.White,
    focusedBorderColor = SpyWhyColors.White.copy(alpha = 0.5f),
    unfocusedBorderColor = SpyWhyColors.BorderGray,
    focusedContainerColor = SpyWhyColors.CardBackground,
    unfocusedContainerColor = SpyWhyColors.CardBackground,
    focusedPlaceholderColor = SpyWhyColors.TextDisabled,
    unfocusedPlaceholderColor = SpyWhyColors.TextDisabled
)
