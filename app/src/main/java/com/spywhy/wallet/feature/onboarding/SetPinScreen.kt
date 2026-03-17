package com.spywhy.wallet.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinScreen(
    onNavigateBack: () -> Unit,
    onPinSet: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentPin = if (isConfirmStep) confirmPin else pin
    val maxLength = 8

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set PIN", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isConfirmStep) {
                            isConfirmStep = false
                            confirmPin = ""
                            errorMessage = null
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = SpyWhyColors.AccentOrange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (isConfirmStep) "Confirm your PIN" else "Create a PIN",
                    color = SpyWhyColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (isConfirmStep) "Enter your PIN again to confirm"
                    else "Choose a 6-8 digit PIN to secure your wallet",
                    color = SpyWhyColors.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until maxLength) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < currentPin.length) SpyWhyColors.AccentOrange
                                    else SpyWhyColors.BorderGray
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${currentPin.length}/$maxLength digits",
                    color = SpyWhyColors.TextDisabled,
                    fontSize = 12.sp
                )

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error, color = SpyWhyColors.AccentRed, fontSize = 13.sp)
                }
            }

            // Number pad
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "delete")
                )

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "" -> Spacer(modifier = Modifier.size(72.dp))
                                "delete" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (isConfirmStep && confirmPin.isNotEmpty()) {
                                                    confirmPin = confirmPin.dropLast(1)
                                                } else if (!isConfirmStep && pin.isNotEmpty()) {
                                                    pin = pin.dropLast(1)
                                                }
                                                errorMessage = null
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Backspace,
                                            "Delete",
                                            tint = SpyWhyColors.TextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(SpyWhyColors.MediumGray)
                                            .clickable {
                                                if (currentPin.length < maxLength) {
                                                    if (isConfirmStep) {
                                                        confirmPin += key
                                                    } else {
                                                        pin += key
                                                    }
                                                    errorMessage = null
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            key,
                                            color = SpyWhyColors.TextPrimary,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isConfirmStep) {
                            if (pin.length < 6) {
                                errorMessage = "PIN must be at least 6 digits"
                            } else {
                                isConfirmStep = true
                                errorMessage = null
                            }
                        } else {
                            if (pin == confirmPin) {
                                onPinSet()
                            } else {
                                errorMessage = "PINs do not match"
                                confirmPin = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = currentPin.length >= 6,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.AccentOrange,
                        contentColor = SpyWhyColors.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isConfirmStep) "Confirm PIN" else "Next",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
