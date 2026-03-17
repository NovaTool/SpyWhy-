package com.spywhy.wallet.feature.receive

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.feature.receive.viewmodel.ReceiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    viewModel: ReceiveViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Receive",
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

        // Blockchain selector tabs
        val blockchains = Blockchain.entries.toList()
        val selectedIndex = blockchains.indexOf(uiState.selectedBlockchain)

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
            blockchains.forEachIndexed { index, blockchain ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { viewModel.selectBlockchain(blockchain) },
                    text = {
                        Text(
                            text = blockchain.ticker,
                            color = if (index == selectedIndex) SpyWhyColors.White
                            else SpyWhyColors.TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // QR Code
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpyWhyColors.Black)
                    .border(
                        width = 1.dp,
                        color = SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                uiState.qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code for ${uiState.currentAddress}",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } ?: Text(
                    text = "Generating...",
                    color = SpyWhyColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Address display
            Text(
                text = uiState.selectedBlockchain.displayName + " Address",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpyWhyColors.CardBackground)
                    .border(
                        width = 1.dp,
                        color = SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        clipboardManager.setText(AnnotatedString(uiState.currentAddress))
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.currentAddress,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SpyWhyColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Copy button
                ActionChip(
                    label = "Copy",
                    icon = Icons.Filled.ContentCopy,
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.currentAddress))
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Share button
                ActionChip(
                    label = "Share",
                    icon = Icons.Filled.Share,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, uiState.currentAddress)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share address")
                        context.startActivity(shareIntent)
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // New Address button
                ActionChip(
                    label = "New Address",
                    icon = Icons.Filled.Refresh,
                    onClick = { viewModel.rotateAddress() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment amount input (optional)
            Text(
                text = "Request Amount (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.paymentAmount,
                onValueChange = { viewModel.setPaymentAmount(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "0.00", color = SpyWhyColors.TextDisabled)
                },
                suffix = {
                    Text(
                        text = uiState.selectedBlockchain.ticker,
                        color = SpyWhyColors.TextSecondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpyWhyColors.TextPrimary,
                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                    cursorColor = SpyWhyColors.White,
                    focusedBorderColor = SpyWhyColors.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = SpyWhyColors.BorderGray,
                    focusedContainerColor = SpyWhyColors.CardBackground,
                    unfocusedContainerColor = SpyWhyColors.CardBackground,
                    focusedPlaceholderColor = SpyWhyColors.TextDisabled,
                    unfocusedPlaceholderColor = SpyWhyColors.TextDisabled
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.paymentAmount.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "QR code encodes the payment amount for easy scanning",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextDisabled,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SpyWhyColors.SurfaceVariant,
            contentColor = SpyWhyColors.TextPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SpyWhyColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SpyWhyColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
