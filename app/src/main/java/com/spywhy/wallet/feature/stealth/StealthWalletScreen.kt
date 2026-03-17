package com.spywhy.wallet.feature.stealth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StealthWalletScreen(
    onNavigateBack: () -> Unit
) {
    var isSetup by remember { mutableStateOf(false) }
    var stealthPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stealth Wallet", color = SpyWhyColors.TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SpyWhyColors.AccentOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Hidden Wallet",
                        color = SpyWhyColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create a hidden wallet with a separate PIN. When you enter the stealth PIN at the lock screen, a different wallet with separate funds will open instead of your main wallet.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // How it works
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How it works", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    StepItem("1", "Set a different PIN for the stealth wallet")
                    StepItem("2", "Enter the stealth PIN at the lock screen")
                    StepItem("3", "A separate wallet opens with its own seed & funds")
                    StepItem("4", "Your main wallet remains completely hidden")
                }
            }

            // Setup
            if (!isSetup) {
                Text("Setup Stealth PIN", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = if (isConfirmStep) confirmPin else stealthPin,
                    onValueChange = { if (it.length <= 8) { if (isConfirmStep) confirmPin = it else stealthPin = it } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isConfirmStep) "Confirm Stealth PIN" else "Enter Stealth PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SpyWhyColors.TextPrimary,
                        unfocusedTextColor = SpyWhyColors.TextPrimary,
                        focusedBorderColor = SpyWhyColors.AccentOrange,
                        unfocusedBorderColor = SpyWhyColors.BorderGray,
                        focusedLabelColor = SpyWhyColors.AccentOrange,
                        unfocusedLabelColor = SpyWhyColors.TextSecondary
                    ),
                    singleLine = true
                )

                Text(
                    "Must be different from your main PIN (6-8 digits)",
                    color = SpyWhyColors.TextSecondary,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        if (!isConfirmStep) {
                            isConfirmStep = true
                        } else if (stealthPin == confirmPin) {
                            isSetup = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = (if (isConfirmStep) confirmPin.length >= 6 else stealthPin.length >= 6),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentOrange,
                        contentColor = SpyWhyColors.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isConfirmStep) "Activate Stealth Wallet" else "Next",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Stealth wallet is active
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SpyWhyColors.AccentGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Stealth wallet is active", color = SpyWhyColors.AccentGreen, fontWeight = FontWeight.Medium)
                    }
                }

                OutlinedButton(
                    onClick = { isSetup = false; stealthPin = ""; confirmPin = ""; isConfirmStep = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Disable Stealth Wallet")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepItem(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(28.dp),
            colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(number, color = SpyWhyColors.AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = SpyWhyColors.TextSecondary, fontSize = 13.sp)
    }
}
