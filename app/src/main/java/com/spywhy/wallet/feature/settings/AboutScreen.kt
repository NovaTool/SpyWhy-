package com.spywhy.wallet.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    currentVersion: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About SpyWhy", color = SpyWhyColors.TextPrimary) },
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
            Spacer(Modifier.height(16.dp))

            Icon(
                Icons.Default.Security,
                null,
                tint = SpyWhyColors.AccentOrange,
                modifier = Modifier.size(72.dp)
            )

            Text(
                "SpyWhy",
                color = SpyWhyColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Version $currentVersion",
                color = SpyWhyColors.AccentOrange,
                fontSize = 14.sp
            )

            Text(
                "Privacy-first hardware wallet",
                color = SpyWhyColors.TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // Supported blockchains
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Supported Blockchains", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Blockchain.entries.forEach { chain ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CurrencyBitcoin, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(chain.displayName, color = SpyWhyColors.TextPrimary, modifier = Modifier.weight(1f))
                            Text(chain.ticker, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Features
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Key Features", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    FeatureItem("Air-gapped transaction signing")
                    FeatureItem("Multi-chain support")
                    FeatureItem("Stealth wallet mode")
                    FeatureItem("Panic mode emergency wipe")
                    FeatureItem("Custom node configuration")
                    FeatureItem("Hardware wallet via QR codes")
                }
            }

            // Legal
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Disclaimer",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "SpyWhy is provided as-is. You are solely responsible for the security of your private keys and funds. Always backup your seed phrase in a secure location.",
                        color = SpyWhyColors.TextDisabled,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Made with privacy in mind",
                color = SpyWhyColors.TextDisabled,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = SpyWhyColors.AccentGreen, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = SpyWhyColors.TextPrimary, fontSize = 13.sp)
    }
}
