package com.spywhy.wallet.feature.send

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.send.viewmodel.FeeLevel
import com.spywhy.wallet.feature.send.viewmodel.SendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeSelectionScreen(
    viewModel: SendViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var customFeeInput by remember { mutableStateOf(uiState.customFee) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Network Fee",
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select transaction priority",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Low fee card
            FeeOptionCard(
                title = "Low Priority",
                fee = uiState.feeEstimate?.low?.toPlainString() ?: "--",
                ticker = uiState.selectedBlockchain.ticker,
                estimatedTime = uiState.feeEstimate?.estimatedTimeLow ?: "~60 min",
                icon = Icons.Filled.AccessTime,
                isSelected = uiState.feeLevel == FeeLevel.LOW,
                onClick = {
                    viewModel.selectFeeLevel(FeeLevel.LOW)
                    onNavigateBack()
                }
            )

            // Medium fee card
            FeeOptionCard(
                title = "Medium Priority",
                fee = uiState.feeEstimate?.medium?.toPlainString() ?: "--",
                ticker = uiState.selectedBlockchain.ticker,
                estimatedTime = uiState.feeEstimate?.estimatedTimeMedium ?: "~30 min",
                icon = Icons.Filled.LocalGasStation,
                isSelected = uiState.feeLevel == FeeLevel.MEDIUM,
                onClick = {
                    viewModel.selectFeeLevel(FeeLevel.MEDIUM)
                    onNavigateBack()
                }
            )

            // High fee card
            FeeOptionCard(
                title = "High Priority",
                fee = uiState.feeEstimate?.high?.toPlainString() ?: "--",
                ticker = uiState.selectedBlockchain.ticker,
                estimatedTime = uiState.feeEstimate?.estimatedTimeHigh ?: "~10 min",
                icon = Icons.Filled.Speed,
                isSelected = uiState.feeLevel == FeeLevel.HIGH,
                onClick = {
                    viewModel.selectFeeLevel(FeeLevel.HIGH)
                    onNavigateBack()
                }
            )

            // Custom fee card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.feeLevel == FeeLevel.CUSTOM)
                            SpyWhyColors.White.copy(alpha = 0.08f)
                        else SpyWhyColors.CardBackground
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.feeLevel == FeeLevel.CUSTOM)
                            SpyWhyColors.White.copy(alpha = 0.3f)
                        else SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.selectFeeLevel(FeeLevel.CUSTOM) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = if (uiState.feeLevel == FeeLevel.CUSTOM)
                                SpyWhyColors.White else SpyWhyColors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Custom Fee",
                            style = MaterialTheme.typography.titleMedium,
                            color = SpyWhyColors.TextPrimary
                        )
                    }
                    if (uiState.feeLevel == FeeLevel.CUSTOM) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = SpyWhyColors.AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (uiState.feeLevel == FeeLevel.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customFeeInput,
                        onValueChange = {
                            customFeeInput = it
                            viewModel.setCustomFee(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter fee amount",
                                color = SpyWhyColors.TextDisabled
                            )
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
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Estimated confirmation info
            if (uiState.feeEstimateDisplay.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SpyWhyColors.SurfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.feeEstimateDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.TextSecondary
                    )
                }
            }

            // Apply button for custom
            if (uiState.feeLevel == FeeLevel.CUSTOM) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.White,
                        contentColor = SpyWhyColors.Black
                    ),
                    enabled = customFeeInput.isNotBlank()
                ) {
                    Text(
                        text = "Apply Custom Fee",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SpyWhyColors.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeeOptionCard(
    title: String,
    fee: String,
    ticker: String,
    estimatedTime: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) SpyWhyColors.White.copy(alpha = 0.08f)
                else SpyWhyColors.CardBackground
            )
            .border(
                width = 1.dp,
                color = if (isSelected) SpyWhyColors.White.copy(alpha = 0.3f)
                else SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) SpyWhyColors.White else SpyWhyColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.TextPrimary
                )
                Text(
                    text = estimatedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$fee $ticker",
                style = MaterialTheme.typography.bodyLarge,
                color = SpyWhyColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = SpyWhyColors.AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
