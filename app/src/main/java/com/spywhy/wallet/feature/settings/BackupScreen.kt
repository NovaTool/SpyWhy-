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
import com.spywhy.wallet.core.util.SpyWhyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit
) {
    var showSeedDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", color = SpyWhyColors.TextPrimary) },
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
            // Warning banner
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = SpyWhyColors.AccentOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Never share your seed phrase or private keys. Anyone with access can steal your funds.",
                        color = SpyWhyColors.AccentOrange,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // View Seed Phrase
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp),
                onClick = { showSeedDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("View Seed Phrase", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Display your recovery phrase (requires authentication)", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
                }
            }

            // Export Encrypted Backup
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp),
                onClick = { showExportDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileDownload, null, tint = SpyWhyColors.AccentGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export Encrypted Backup", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Password-protected backup file", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
                }
            }

            // Import Backup
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp),
                onClick = { }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FileUpload, null, tint = SpyWhyColors.TextPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Import Backup", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Restore from encrypted backup file", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
                }
            }

            // Export Private Keys
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp),
                onClick = { }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Key, null, tint = SpyWhyColors.AccentRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export Private Keys", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Export individual chain private keys", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = SpyWhyColors.TextDisabled)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSeedDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDialog = false },
            title = { Text("Authentication Required", color = SpyWhyColors.TextPrimary) },
            text = { Text("You need to authenticate to view your seed phrase.", color = SpyWhyColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showSeedDialog = false }) {
                    Text("Authenticate", color = SpyWhyColors.AccentGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedDialog = false }) {
                    Text("Cancel", color = SpyWhyColors.TextSecondary)
                }
            },
            containerColor = SpyWhyColors.MediumGray
        )
    }
}
