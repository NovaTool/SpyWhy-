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
import com.spywhy.wallet.core.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeSettingsScreen(
    settingsPreferences: SettingsPreferences,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var btcNode by remember { mutableStateOf(Constants.BTC_NODE_DEFAULT) }
    var ethNode by remember { mutableStateOf(Constants.ETH_NODE_DEFAULT) }
    var solNode by remember { mutableStateOf(Constants.SOL_NODE_DEFAULT) }
    var ltcNode by remember { mutableStateOf(Constants.LTC_NODE_DEFAULT) }
    var xmrNode by remember { mutableStateOf("node.moneroworld.com:18089") }

    LaunchedEffect(Unit) {
        launch { settingsPreferences.btcNode.collect { if (it.isNotEmpty()) btcNode = it } }
        launch { settingsPreferences.ethNode.collect { if (it.isNotEmpty()) ethNode = it } }
        launch { settingsPreferences.solNode.collect { if (it.isNotEmpty()) solNode = it } }
        launch { settingsPreferences.ltcNode.collect { if (it.isNotEmpty()) ltcNode = it } }
        launch { settingsPreferences.xmrNode.collect { xmrNode = it } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Nodes", color = SpyWhyColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            settingsPreferences.set(SettingsPreferences.BTC_NODE, btcNode)
                            settingsPreferences.set(SettingsPreferences.ETH_NODE, ethNode)
                            settingsPreferences.set(SettingsPreferences.SOL_NODE, solNode)
                            settingsPreferences.set(SettingsPreferences.LTC_NODE, ltcNode)
                            settingsPreferences.set(SettingsPreferences.XMR_NODE, xmrNode)
                        }
                        onNavigateBack()
                    }) {
                        Text("Save", color = SpyWhyColors.AccentGreen)
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
            Text(
                "Configure custom blockchain nodes for enhanced privacy and reliability.",
                color = SpyWhyColors.TextSecondary,
                fontSize = 13.sp
            )

            NodeInput("Bitcoin (BTC)", "Electrum server", btcNode) { btcNode = it }
            NodeInput("Ethereum (ETH)", "JSON-RPC endpoint", ethNode) { ethNode = it }
            NodeInput("Solana (SOL)", "RPC endpoint", solNode) { solNode = it }
            NodeInput("Litecoin (LTC)", "Electrum server", ltcNode) { ltcNode = it }
            NodeInput("Monero (XMR)", "Daemon RPC", xmrNode) { xmrNode = it }

            // Reset to defaults
            OutlinedButton(
                onClick = {
                    btcNode = Constants.BTC_NODE_DEFAULT
                    ethNode = Constants.ETH_NODE_DEFAULT
                    solNode = Constants.SOL_NODE_DEFAULT
                    ltcNode = Constants.LTC_NODE_DEFAULT
                    xmrNode = "node.moneroworld.com:18089"
                    scope.launch {
                        settingsPreferences.set(SettingsPreferences.BTC_NODE, btcNode)
                        settingsPreferences.set(SettingsPreferences.ETH_NODE, ethNode)
                        settingsPreferences.set(SettingsPreferences.SOL_NODE, solNode)
                        settingsPreferences.set(SettingsPreferences.LTC_NODE, ltcNode)
                        settingsPreferences.set(SettingsPreferences.XMR_NODE, xmrNode)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.TextSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Defaults")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NodeInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
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
                Text(label, color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Medium)
                // Connection test indicator
                Icon(
                    Icons.Default.Circle,
                    contentDescription = "Status",
                    modifier = Modifier.size(8.dp),
                    tint = SpyWhyColors.TextDisabled
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = SpyWhyColors.TextDisabled) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpyWhyColors.TextPrimary,
                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                    focusedBorderColor = SpyWhyColors.AccentGreen,
                    unfocusedBorderColor = SpyWhyColors.BorderGray
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { /* Test connection */ }) {
                        Icon(Icons.Default.NetworkCheck, "Test", tint = SpyWhyColors.TextSecondary)
                    }
                }
            )
        }
    }
}
