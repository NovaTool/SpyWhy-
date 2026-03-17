package com.spywhy.wallet.feature.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSecurity: () -> Unit,
    onNavigateToNodes: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToStealth: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = SpyWhyColors.TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Security Section
            Text("Security", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Security Settings",
                subtitle = "PIN, biometrics, auto-lock, wipe policy",
                onClick = onNavigateToSecurity
            )
            SettingsItem(
                icon = Icons.Default.VisibilityOff,
                title = "Stealth Wallet",
                subtitle = "Hidden wallet with separate PIN",
                onClick = onNavigateToStealth
            )
            SettingsItem(
                icon = Icons.Default.Warning,
                title = "Panic Mode",
                subtitle = "Emergency key deletion",
                onClick = { },
                tint = SpyWhyColors.AccentRed
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Section
            Text("Wallet", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Export seed phrase, encrypted backup",
                onClick = onNavigateToBackup
            )
            SettingsItem(
                icon = Icons.Default.Dns,
                title = "Custom Nodes",
                subtitle = "Configure blockchain nodes",
                onClick = onNavigateToNodes
            )
            SettingsItem(
                icon = Icons.Default.Key,
                title = "Export Private Keys",
                subtitle = "Export individual private keys",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.Draw,
                title = "Message Signing",
                subtitle = "Sign messages with your keys",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // General Section
            Text("General", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            SettingsItem(
                icon = Icons.Default.CurrencyExchange,
                title = "Default Currency",
                subtitle = "USD",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Price alerts, receive notifications",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.Widgets,
                title = "Widgets",
                subtitle = "Home screen widgets",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            Text("About", color = SpyWhyColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About SpyWhy",
                subtitle = "Version 1.0.0",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = SpyWhyColors.TextPrimary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = tint, fontWeight = FontWeight.Medium)
                Text(subtitle, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
        }
    }
}
