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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.settings.SettingsPreferences
import com.spywhy.wallet.core.util.SpyWhyColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    settingsPreferences: SettingsPreferences,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var biometricEnabled by remember { mutableStateOf(true) }
    var screenshotProtection by remember { mutableStateOf(true) }
    var rootDetection by remember { mutableStateOf(true) }
    var autoLockTimeout by remember { mutableStateOf("60") }
    var maxPinAttempts by remember { mutableStateOf("10") }
    var clipboardClearDelay by remember { mutableStateOf("30") }

    // Load saved values
    LaunchedEffect(Unit) {
        launch { settingsPreferences.biometricEnabled.collect { biometricEnabled = it } }
        launch { settingsPreferences.screenshotProtection.collect { screenshotProtection = it } }
        launch { settingsPreferences.rootDetection.collect { rootDetection = it } }
        launch { settingsPreferences.autoLockTimeout.collect { autoLockTimeout = it } }
        launch { settingsPreferences.maxPinAttempts.collect { maxPinAttempts = it } }
        launch { settingsPreferences.clipboardClearDelay.collect { clipboardClearDelay = it } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security", color = SpyWhyColors.TextPrimary) },
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
            // Authentication
            Text("Authentication", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Change PIN", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Change your wallet PIN", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SpyWhyColors.BorderGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric Unlock", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Fingerprint / Face unlock", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                scope.launch { settingsPreferences.set(SettingsPreferences.BIOMETRIC_ENABLED, it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpyWhyColors.AccentGreen,
                                checkedTrackColor = SpyWhyColors.AccentGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Auto-Lock
            Text("Auto-Lock", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lock timeout (seconds)", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("15", "30", "60", "120", "300").forEach { timeout ->
                            FilterChip(
                                selected = autoLockTimeout == timeout,
                                onClick = {
                                    autoLockTimeout = timeout
                                    scope.launch { settingsPreferences.set(SettingsPreferences.AUTO_LOCK_TIMEOUT, timeout) }
                                },
                                label = { Text("${timeout}s", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = SpyWhyColors.AccentGreen
                                )
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SpyWhyColors.BorderGray)
                    Text("Max PIN attempts before wipe", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("3", "5", "10", "20").forEach { attempts ->
                            FilterChip(
                                selected = maxPinAttempts == attempts,
                                onClick = {
                                    maxPinAttempts = attempts
                                    scope.launch { settingsPreferences.set(SettingsPreferences.MAX_PIN_ATTEMPTS, attempts) }
                                },
                                label = { Text(attempts, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpyWhyColors.AccentRed.copy(alpha = 0.2f),
                                    selectedLabelColor = SpyWhyColors.AccentRed
                                )
                            )
                        }
                    }
                }
            }

            // Protection
            Text("Protection", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Screenshot Protection", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Block screenshots and screen recording", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = screenshotProtection,
                            onCheckedChange = {
                                screenshotProtection = it
                                scope.launch { settingsPreferences.set(SettingsPreferences.SCREENSHOT_PROTECTION, it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpyWhyColors.AccentGreen,
                                checkedTrackColor = SpyWhyColors.AccentGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SpyWhyColors.BorderGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Root Detection", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Warn if device is rooted", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = rootDetection,
                            onCheckedChange = {
                                rootDetection = it
                                scope.launch { settingsPreferences.set(SettingsPreferences.ROOT_DETECTION, it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpyWhyColors.AccentGreen,
                                checkedTrackColor = SpyWhyColors.AccentGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SpyWhyColors.BorderGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Clipboard auto-clear", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Clear clipboard after ${clipboardClearDelay}s", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("15", "30", "60").forEach { delay ->
                                FilterChip(
                                    selected = clipboardClearDelay == delay,
                                    onClick = {
                                        clipboardClearDelay = delay
                                        scope.launch { settingsPreferences.set(SettingsPreferences.CLIPBOARD_CLEAR_DELAY, delay) }
                                    },
                                    label = { Text("${delay}s", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                                        selectedLabelColor = SpyWhyColors.AccentGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
