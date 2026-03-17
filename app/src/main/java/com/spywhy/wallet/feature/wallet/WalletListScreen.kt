package com.spywhy.wallet.feature.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateWallet: () -> Unit,
    onNavigateToImportWallet: () -> Unit,
    onWalletSelected: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val wallets = remember {
        listOf(
            WalletItem("1", "Main Wallet", "HD Wallet", "$0.00", true),
            WalletItem("2", "Trading", "HD Wallet", "$0.00", false),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallets", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Wallet", tint = SpyWhyColors.AccentOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpyWhyColors.Black)
            )
        },
        containerColor = SpyWhyColors.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            items(wallets) { wallet ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (wallet.isActive) SpyWhyColors.AccentOrange.copy(alpha = 0.1f)
                        else SpyWhyColors.MediumGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onWalletSelected(wallet.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier.size(44.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (wallet.isActive) SpyWhyColors.AccentOrange.copy(alpha = 0.2f)
                                else SpyWhyColors.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    null,
                                    tint = if (wallet.isActive) SpyWhyColors.AccentOrange else SpyWhyColors.TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(wallet.name, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                                if (wallet.isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Active",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = SpyWhyColors.AccentGreen,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            Text(wallet.type, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }

                        Text(wallet.balance, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Add Watch-Only
            item {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.TextSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Watch-Only Wallet")
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Wallet", color = SpyWhyColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showAddDialog = false; onNavigateToCreateWallet() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, tint = SpyWhyColors.AccentGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Wallet", color = SpyWhyColors.TextPrimary)
                    }
                    TextButton(
                        onClick = { showAddDialog = false; onNavigateToImportWallet() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, null, tint = SpyWhyColors.AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import Seed Phrase", color = SpyWhyColors.TextPrimary)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SpyWhyColors.TextSecondary)
                }
            },
            containerColor = SpyWhyColors.MediumGray
        )
    }
}

private data class WalletItem(
    val id: String,
    val name: String,
    val type: String,
    val balance: String,
    val isActive: Boolean
)
