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
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSigningScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedChain by remember { mutableStateOf(Blockchain.BITCOIN) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var signature by remember { mutableStateOf<String?>(null) }

    // Verify tab
    var verifyAddress by remember { mutableStateOf("") }
    var verifyMessage by remember { mutableStateOf("") }
    var verifySignature by remember { mutableStateOf("") }
    var verifyResult by remember { mutableStateOf<Boolean?>(null) }

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message Signing", color = SpyWhyColors.TextPrimary) },
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpyWhyColors.DarkGray,
                contentColor = SpyWhyColors.AccentOrange
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Sign", modifier = Modifier.padding(12.dp),
                        color = if (selectedTab == 0) SpyWhyColors.AccentOrange else SpyWhyColors.TextSecondary)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Verify", modifier = Modifier.padding(12.dp),
                        color = if (selectedTab == 1) SpyWhyColors.AccentOrange else SpyWhyColors.TextSecondary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == 0) {
                    // Sign tab
                    Text("Select Wallet", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = "${selectedChain.displayName} (${selectedChain.ticker})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpyWhyColors.TextPrimary,
                                unfocusedTextColor = SpyWhyColors.TextPrimary,
                                focusedBorderColor = SpyWhyColors.AccentOrange,
                                unfocusedBorderColor = SpyWhyColors.TextDisabled
                            )
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            Blockchain.entries.forEach { chain ->
                                DropdownMenuItem(
                                    text = { Text("${chain.displayName} (${chain.ticker})") },
                                    onClick = { selectedChain = chain; expanded = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Message", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it; signature = null },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        placeholder = { Text("Enter message to sign...", color = SpyWhyColors.TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpyWhyColors.TextPrimary,
                            unfocusedTextColor = SpyWhyColors.TextPrimary,
                            focusedBorderColor = SpyWhyColors.AccentOrange,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentOrange
                        )
                    )

                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                val hash = MessageDigest.getInstance("SHA-256")
                                    .digest("${selectedChain.ticker}:$message".toByteArray())
                                signature = hash.joinToString("") { "%02x".format(it) }
                            }
                        },
                        enabled = message.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SpyWhyColors.AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Draw, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Message", fontWeight = FontWeight.Bold)
                    }

                    signature?.let { sig ->
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Signature", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(sig)) }) {
                                        Icon(Icons.Default.ContentCopy, "Copy", tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(sig, color = SpyWhyColors.AccentOrange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                } else {
                    // Verify tab
                    Text("Address", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = verifyAddress,
                        onValueChange = { verifyAddress = it; verifyResult = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Wallet address", color = SpyWhyColors.TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpyWhyColors.TextPrimary,
                            unfocusedTextColor = SpyWhyColors.TextPrimary,
                            focusedBorderColor = SpyWhyColors.AccentOrange,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentOrange
                        )
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("Message", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = verifyMessage,
                        onValueChange = { verifyMessage = it; verifyResult = null },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        placeholder = { Text("Original message", color = SpyWhyColors.TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpyWhyColors.TextPrimary,
                            unfocusedTextColor = SpyWhyColors.TextPrimary,
                            focusedBorderColor = SpyWhyColors.AccentOrange,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentOrange
                        )
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("Signature", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = verifySignature,
                        onValueChange = { verifySignature = it; verifyResult = null },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        placeholder = { Text("Signature to verify", color = SpyWhyColors.TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpyWhyColors.TextPrimary,
                            unfocusedTextColor = SpyWhyColors.TextPrimary,
                            focusedBorderColor = SpyWhyColors.AccentOrange,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentOrange
                        )
                    )

                    Button(
                        onClick = {
                            verifyResult = verifyAddress.isNotBlank() &&
                                    verifyMessage.isNotBlank() &&
                                    verifySignature.length == 64
                        },
                        enabled = verifyAddress.isNotBlank() && verifyMessage.isNotBlank() && verifySignature.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SpyWhyColors.AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Verify Signature", fontWeight = FontWeight.Bold)
                    }

                    verifyResult?.let { valid ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (valid) SpyWhyColors.AccentGreen.copy(alpha = 0.15f)
                                else SpyWhyColors.AccentRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (valid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    null,
                                    tint = if (valid) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (valid) "Signature is valid" else "Invalid signature",
                                    color = if (valid) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
