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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanicModeScreen(
    onNavigateBack: () -> Unit
) {
    var understood by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var wiped by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panic Mode", color = SpyWhyColors.AccentRed) },
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
                Icons.Default.Warning,
                null,
                tint = SpyWhyColors.AccentRed,
                modifier = Modifier.size(64.dp)
            )

            Text(
                "Emergency Data Wipe",
                color = SpyWhyColors.AccentRed,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This will permanently destroy:",
                        color = SpyWhyColors.AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                    WipeItem("All wallet data and private keys")
                    WipeItem("Seed phrases and encrypted backups")
                    WipeItem("Transaction history")
                    WipeItem("All app settings and preferences")
                    WipeItem("PIN and biometric data")
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.DarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "This action is IRREVERSIBLE. Make sure you have a backup of your seed phrase before proceeding. Without your seed phrase, your funds will be permanently lost.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Checkbox(
                    checked = understood,
                    onCheckedChange = { understood = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SpyWhyColors.AccentRed,
                        uncheckedColor = SpyWhyColors.TextDisabled,
                        checkmarkColor = SpyWhyColors.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "I understand that this will permanently delete all my wallet data",
                    color = SpyWhyColors.TextPrimary,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            if (wiped) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SpyWhyColors.AccentRed)
                        Spacer(Modifier.width(12.dp))
                        Text("All data has been wiped.", color = SpyWhyColors.AccentRed, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = understood,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentRed,
                        disabledContainerColor = SpyWhyColors.AccentRed.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("WIPE ALL DATA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = SpyWhyColors.DarkGray,
            title = { Text("Confirm Wipe", color = SpyWhyColors.AccentRed) },
            text = {
                Column {
                    Text(
                        "Enter your PIN to confirm the data wipe.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 13.sp
                    )
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
                            focusedBorderColor = SpyWhyColors.AccentRed,
                            unfocusedBorderColor = SpyWhyColors.TextDisabled,
                            cursorColor = SpyWhyColors.AccentRed
                        )
                    )
                    if (pinError) {
                        Text("PIN must be at least 4 digits", color = SpyWhyColors.AccentRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinInput.length >= 4) {
                        showConfirmDialog = false
                        wiped = true
                    } else {
                        pinError = true
                    }
                }) {
                    Text("WIPE", color = SpyWhyColors.AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = SpyWhyColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun WipeItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Close, null, tint = SpyWhyColors.AccentRed, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = SpyWhyColors.TextPrimary, fontSize = 13.sp)
    }
}
