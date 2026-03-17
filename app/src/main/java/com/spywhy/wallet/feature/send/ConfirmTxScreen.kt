package com.spywhy.wallet.feature.send

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.send.viewmodel.SendViewModel
import com.spywhy.wallet.feature.send.viewmodel.TxResult
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmTxScreen(
    viewModel: SendViewModel,
    onNavigateBack: () -> Unit,
    onBiometricAuth: (onSuccess: () -> Unit) -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val amount = try {
        BigDecimal(uiState.amount)
    } catch (_: Exception) {
        BigDecimal.ZERO
    }
    val fee = uiState.currentFeeAmount
    val total = amount + fee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        if (uiState.txResult == TxResult.IDLE || uiState.txResult == TxResult.LOADING) {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirm Transaction",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SpyWhyColors.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = uiState.txResult != TxResult.LOADING
                    ) {
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
        }

        AnimatedContent(
            targetState = uiState.txResult,
            label = "tx_result"
        ) { result ->
            when (result) {
                TxResult.IDLE -> {
                    TransactionSummaryContent(
                        blockchain = uiState.selectedBlockchain.displayName,
                        ticker = uiState.selectedBlockchain.ticker,
                        toAddress = uiState.toAddress,
                        amount = amount,
                        fee = fee,
                        total = total,
                        onSignAndSend = {
                            onBiometricAuth {
                                viewModel.signAndBroadcast()
                            }
                        }
                    )
                }

                TxResult.LOADING -> {
                    LoadingContent()
                }

                TxResult.SUCCESS -> {
                    SuccessContent(
                        txHash = uiState.txHash,
                        ticker = uiState.selectedBlockchain.ticker,
                        amount = amount,
                        onDone = {
                            viewModel.resetTxResult()
                            onDone()
                        }
                    )
                }

                TxResult.FAILURE -> {
                    FailureContent(
                        errorMessage = uiState.txErrorMessage,
                        onRetry = {
                            viewModel.resetTxResult()
                        },
                        onDone = {
                            viewModel.resetTxResult()
                            onDone()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionSummaryContent(
    blockchain: String,
    ticker: String,
    toAddress: String,
    amount: BigDecimal,
    fee: BigDecimal,
    total: BigDecimal,
    onSignAndSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Blockchain badge
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(SpyWhyColors.SurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = SpyWhyColors.TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = blockchain,
                    style = MaterialTheme.typography.labelLarge,
                    color = SpyWhyColors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Amount display
        Text(
            text = "${amount.stripTrailingZeros().toPlainString()} $ticker",
            style = MaterialTheme.typography.headlineLarge,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Transaction details card
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
            DetailRow(label = "To", value = toAddress, isAddress = true)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SpyWhyColors.BorderGray)
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                label = "Amount",
                value = "${amount.stripTrailingZeros().toPlainString()} $ticker"
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SpyWhyColors.BorderGray)
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                label = "Network Fee",
                value = "${fee.stripTrailingZeros().toPlainString()} $ticker"
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SpyWhyColors.BorderGray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${total.stripTrailingZeros().toPlainString()} $ticker",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Sign & Send button
        Button(
            onClick = onSignAndSend,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpyWhyColors.White,
                contentColor = SpyWhyColors.Black
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = SpyWhyColors.Black,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign & Send",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SpyWhyColors.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isAddress: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = if (isAddress) MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ) else MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextPrimary,
            textAlign = TextAlign.End,
            maxLines = if (isAddress) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = SpyWhyColors.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Broadcasting transaction...",
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please wait while the transaction is signed and sent to the network.",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

@Composable
private fun SuccessContent(
    txHash: String,
    ticker: String,
    amount: BigDecimal,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.AccentGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = SpyWhyColors.AccentGreen,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Transaction Sent",
            style = MaterialTheme.typography.headlineMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${amount.stripTrailingZeros().toPlainString()} $ticker sent successfully",
            style = MaterialTheme.typography.bodyLarge,
            color = SpyWhyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tx hash
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SpyWhyColors.SurfaceVariant)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Transaction Hash",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = txHash,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = SpyWhyColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpyWhyColors.AccentGreen,
                contentColor = SpyWhyColors.Black
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SpyWhyColors.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FailureContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.AccentRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Failed",
                tint = SpyWhyColors.AccentRed,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Transaction Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = SpyWhyColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpyWhyColors.White,
                contentColor = SpyWhyColors.Black
            )
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SpyWhyColors.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onDone) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
