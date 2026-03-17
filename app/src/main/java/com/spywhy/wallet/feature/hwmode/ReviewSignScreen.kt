package com.spywhy.wallet.feature.hwmode

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.usecase.UnsignedTxPayload
import com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSignScreen(
    viewModel: HWModeViewModel,
    onReject: () -> Unit,
    onSign: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val unsignedTx = uiState.unsignedTx
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Review Transaction",
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

        if (unsignedTx == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transaction data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpyWhyColors.TextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Suspicious amount warning
                val totalAmount = unsignedTx.outputs.sumOf { it.amount }
                val isSuspicious = totalAmount > 10_000_000_00L // > 100 BTC equivalent in satoshis

                if (isSuspicious) {
                    WarningBanner(
                        text = "Large transaction amount detected. Please verify all details carefully before signing."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Chain info
                DetailCard(label = "Chain") {
                    Text(
                        text = unsignedTx.chain.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = SpyWhyColors.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recipients
                DetailCard(label = "Recipients") {
                    unsignedTx.outputs.forEachIndexed { index, output ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = SpyWhyColors.BorderGray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Address ${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpyWhyColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = output.address,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = SpyWhyColors.TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formatSatoshis(output.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = SpyWhyColors.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fee
                DetailCard(label = "Network Fee") {
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
                            text = formatSatoshis(unsignedTx.fee),
                            style = MaterialTheme.typography.titleMedium,
                            color = SpyWhyColors.AccentOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total
                val total = totalAmount + unsignedTx.fee
                DetailCard(label = "Total") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total (incl. fee)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpyWhyColors.TextSecondary
                        )
                        Text(
                            text = formatSatoshis(total),
                            style = MaterialTheme.typography.headlineSmall,
                            color = SpyWhyColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Zero-amount warning
                if (unsignedTx.outputs.any { it.amount == 0L }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    WarningBanner(
                        text = "This transaction contains zero-amount outputs. This may be a dust attack or contract interaction."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpyWhyColors.DarkGray)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reject button
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.15f),
                        contentColor = SpyWhyColors.AccentRed
                    )
                ) {
                    Text(
                        text = "Reject",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sign button (triggers biometric auth)
                Button(
                    onClick = onSign,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentGreen,
                        contentColor = SpyWhyColors.Black
                    )
                ) {
                    Text(
                        text = "Sign",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    label: String,
    content: @Composable () -> Unit
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
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun WarningBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpyWhyColors.AccentOrange.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = SpyWhyColors.AccentOrange.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Warning",
            tint = SpyWhyColors.AccentOrange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = SpyWhyColors.AccentOrange
        )
    }
}

private fun formatSatoshis(satoshis: Long): String {
    val btc = satoshis / 100_000_000.0
    return if (btc >= 0.001) {
        "%.8f".format(btc).trimEnd('0').trimEnd('.')
    } else {
        "$satoshis sats"
    }
}
