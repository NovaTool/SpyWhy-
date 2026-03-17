package com.spywhy.wallet.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun BiometricSetupScreen(
    onNavigateBack: () -> Unit,
    onSetupComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var enrollmentState by remember { mutableStateOf(BiometricEnrollmentState.PROMPT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Setup", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Skip", color = SpyWhyColors.TextSecondary)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (enrollmentState) {
                BiometricEnrollmentState.PROMPT -> {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = SpyWhyColors.AccentOrange
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Enable Biometric Unlock",
                        color = SpyWhyColors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Use your fingerprint or face to quickly unlock your wallet without entering your PIN every time.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Benefits
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            BiometricBenefit(
                                icon = Icons.Default.Speed,
                                title = "Quick Access",
                                description = "Unlock in under a second"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            BiometricBenefit(
                                icon = Icons.Default.Security,
                                title = "Secure",
                                description = "Hardware-backed biometric verification"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            BiometricBenefit(
                                icon = Icons.Default.Visibility,
                                title = "PIN Fallback",
                                description = "Your PIN always works as backup"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            // Trigger biometric enrollment
                            enrollmentState = BiometricEnrollmentState.SUCCESS
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
                        Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Biometrics", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onSkip) {
                        Text("Maybe later", color = SpyWhyColors.TextSecondary)
                    }
                }

                BiometricEnrollmentState.SUCCESS -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = SpyWhyColors.AccentGreen
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Biometrics Enabled",
                        color = SpyWhyColors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "You can now unlock your wallet with your fingerprint or face.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onSetupComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentGreen,
                            contentColor = SpyWhyColors.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue to Wallet", fontWeight = FontWeight.Bold)
                    }
                }

                BiometricEnrollmentState.NOT_AVAILABLE -> {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = SpyWhyColors.AccentOrange
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Biometrics Not Available",
                        color = SpyWhyColors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Your device does not support biometric authentication or no biometrics are enrolled in system settings.",
                        color = SpyWhyColors.TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentOrange,
                            contentColor = SpyWhyColors.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue with PIN only", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private enum class BiometricEnrollmentState {
    PROMPT, SUCCESS, NOT_AVAILABLE
}

@Composable
private fun BiometricBenefit(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(description, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
        }
    }
}
