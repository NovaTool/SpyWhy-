package com.spywhy.wallet.feature.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    coinTicker: String,
    onNavigateBack: () -> Unit
) {
    var currentAddress by remember { mutableStateOf("") }
    var addressCopied by remember { mutableStateOf(false) }

    val coinName = when (coinTicker) {
        "BTC" -> "Bitcoin"
        "ETH" -> "Ethereum"
        "SOL" -> "Solana"
        "LTC" -> "Litecoin"
        "XMR" -> "Monero"
        else -> coinTicker
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$coinName Address", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpyWhyColors.Black)
            )
        },
        containerColor = SpyWhyColors.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // QR Code placeholder
            Card(
                modifier = Modifier.size(240.dp),
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.TextPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (currentAddress.isNotEmpty()) {
                        Text("QR Code", color = SpyWhyColors.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QrCode2,
                                null,
                                tint = SpyWhyColors.MediumGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text("No address", color = SpyWhyColors.MediumGray, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Address display
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$coinName Address",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (currentAddress.isNotEmpty()) currentAddress else "Generate an address to receive $coinTicker",
                        color = if (currentAddress.isNotEmpty()) SpyWhyColors.TextPrimary else SpyWhyColors.TextDisabled,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = if (currentAddress.isNotEmpty()) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Copy address to clipboard
                        if (currentAddress.isNotEmpty()) {
                            addressCopied = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpyWhyColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentAddress.isNotEmpty()
                ) {
                    Icon(
                        if (addressCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (addressCopied) "Copied" else "Copy")
                }

                OutlinedButton(
                    onClick = { /* Share address via intent */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpyWhyColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentAddress.isNotEmpty()
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }

            // Generate new address
            Button(
                onClick = {
                    currentAddress = "Generating..."
                    addressCopied = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.AccentOrange,
                    contentColor = SpyWhyColors.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate New Address", fontWeight = FontWeight.Bold)
            }

            // Address poisoning warning
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Shield, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Always verify the full address before sending funds. Watch out for address poisoning attacks.",
                        color = SpyWhyColors.AccentOrange,
                        fontSize = 12.sp
                    )
                }
            }

            // Previous addresses
            Text(
                "Previous Addresses",
                color = SpyWhyColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No previous addresses", color = SpyWhyColors.TextDisabled, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
