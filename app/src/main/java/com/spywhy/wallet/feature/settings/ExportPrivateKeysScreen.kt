package com.spywhy.wallet.feature.settings

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPrivateKeysScreen(
    onNavigateBack: () -> Unit
) {
    var authenticatedChain by remember { mutableStateOf<Blockchain?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var pendingChain by remember { mutableStateOf<Blockchain?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Private Keys", color = SpyWhyColors.TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.DarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = SpyWhyColors.AccentRed, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Never share your private keys. Anyone with your keys can steal your funds.",
                        color = SpyWhyColors.AccentRed,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Blockchain.entries.forEach { chain ->
                val isRevealed = authenticatedChain == chain
                val fakeKey = remember(chain) {
                    "5${chain.ticker.hashCode().toUInt().toString(16).padEnd(50, 'a').take(50)}"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chain.displayName, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                                Text(chain.ticker, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                            }
                            if (isRevealed) {
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(fakeKey))
                                }) {
                                    Icon(Icons.Default.ContentCopy, "Copy", tint = SpyWhyColors.AccentOrange)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (isRevealed) {
                            Text(
                                fakeKey,
                                color = SpyWhyColors.AccentOrange,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { authenticatedChain = null }) {
                                Text("Hide Key", color = SpyWhyColors.TextSecondary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    pendingChain = chain
                                    showAuthDialog = true
                                    pinInput = ""
                                    pinError = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpyWhyColors.DarkGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reveal Private Key")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Private keys are derived from your seed phrase. Keep them secure.",
                color = SpyWhyColors.TextDisabled,
                fontSize = 12.sp
            )
        }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            containerColor = SpyWhyColors.DarkGray,
            title = { Text("Enter PIN", color = SpyWhyColors.TextPrimary) },
            text = {
                Column {
                    Text("Enter your PIN to reveal the private key.", color = SpyWhyColors.TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) { pinInput = it; pinError = false } },
                        label = { Text("PIN") },
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpyWhyColors.TextPrimary,
                            unfocusedTextColor = SpyWhyColors.TextPrimary,
                            focusedBorderColor = SpyWhyColors.AccentOrange,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentOrange
                        )
                    )
                    if (pinError) {
                        Text("Invalid PIN", color = SpyWhyColors.AccentRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinInput.length >= 4) {
                        authenticatedChain = pendingChain
                        showAuthDialog = false
                    } else {
                        pinError = true
                    }
                }) {
                    Text("Confirm", color = SpyWhyColors.AccentOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Cancel", color = SpyWhyColors.TextSecondary)
                }
            }
        )
    }
}
