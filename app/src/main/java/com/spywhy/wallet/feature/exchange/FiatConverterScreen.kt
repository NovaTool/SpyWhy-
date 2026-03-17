package com.spywhy.wallet.feature.exchange

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
fun FiatConverterScreen(
    onNavigateBack: () -> Unit
) {
    var cryptoAmount by remember { mutableStateOf("1.0") }
    var selectedCrypto by remember { mutableStateOf("BTC") }
    var selectedFiat by remember { mutableStateOf("USD") }

    val cryptos = listOf("BTC", "ETH", "SOL", "LTC", "XMR")
    val fiats = listOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Converter", color = SpyWhyColors.TextPrimary) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crypto input
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Crypto", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = cryptoAmount,
                            onValueChange = { cryptoAmount = it },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpyWhyColors.TextPrimary,
                                unfocusedTextColor = SpyWhyColors.TextPrimary,
                                focusedBorderColor = SpyWhyColors.AccentGreen,
                                unfocusedBorderColor = SpyWhyColors.BorderGray
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            AssistChip(
                                onClick = { expanded = true },
                                label = { Text(selectedCrypto, color = SpyWhyColors.TextPrimary) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                cryptos.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = { selectedCrypto = c; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Equals
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DragHandle, "equals", tint = SpyWhyColors.TextSecondary)
            }

            // Fiat result
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fiat", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "~\$0.00",
                            color = SpyWhyColors.AccentGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        var fiatExpanded by remember { mutableStateOf(false) }
                        Box {
                            AssistChip(
                                onClick = { fiatExpanded = true },
                                label = { Text(selectedFiat, color = SpyWhyColors.TextPrimary) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                            )
                            DropdownMenu(expanded = fiatExpanded, onDismissRequest = { fiatExpanded = false }) {
                                fiats.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f) },
                                        onClick = { selectedFiat = f; fiatExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick conversion table
            Text("Quick Reference", color = SpyWhyColors.TextPrimary, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    listOf("0.001", "0.01", "0.1", "1.0", "10.0").forEach { amt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$amt $selectedCrypto", color = SpyWhyColors.TextSecondary)
                            Text("~\$-.--", color = SpyWhyColors.TextPrimary)
                        }
                        if (amt != "10.0") {
                            HorizontalDivider(color = SpyWhyColors.BorderGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}
