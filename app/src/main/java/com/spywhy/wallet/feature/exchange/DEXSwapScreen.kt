package com.spywhy.wallet.feature.exchange

import androidx.compose.foundation.background
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
fun DEXSwapScreen(
    onNavigateBack: () -> Unit
) {
    var selectedDEX by remember { mutableStateOf("Uniswap") }
    var fromToken by remember { mutableStateOf("ETH") }
    var toToken by remember { mutableStateOf("USDC") }
    var amount by remember { mutableStateOf("") }
    var slippage by remember { mutableStateOf("1.0") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DEX Swap", color = SpyWhyColors.TextPrimary) },
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
            // DEX Selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Uniswap", "1inch").forEach { dex ->
                    FilterChip(
                        selected = selectedDEX == dex,
                        onClick = { selectedDEX = dex },
                        label = { Text(dex) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SpyWhyColors.AccentGreen
                        )
                    )
                }
            }

            // From Token
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("You Pay", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("0.0", color = SpyWhyColors.TextDisabled) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpyWhyColors.TextPrimary,
                                unfocusedTextColor = SpyWhyColors.TextPrimary,
                                focusedBorderColor = SpyWhyColors.AccentGreen,
                                unfocusedBorderColor = SpyWhyColors.BorderGray
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(fromToken, color = SpyWhyColors.TextPrimary) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = SpyWhyColors.TextSecondary) }
                        )
                    }
                }
            }

            // Swap Arrow
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        val temp = fromToken; fromToken = toToken; toToken = temp
                    },
                    modifier = Modifier
                        .background(SpyWhyColors.LightGray, RoundedCornerShape(50))
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.SwapVert, "Swap", tint = SpyWhyColors.AccentGreen)
                }
            }

            // To Token
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("You Receive", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("~0.00", color = SpyWhyColors.TextPrimary, fontSize = 24.sp, modifier = Modifier.weight(1f))
                        AssistChip(
                            onClick = { },
                            label = { Text(toToken, color = SpyWhyColors.TextPrimary) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = SpyWhyColors.TextSecondary) }
                        )
                    }
                }
            }

            // Slippage
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Slippage Tolerance", color = SpyWhyColors.TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("0.5", "1.0", "2.0").forEach { s ->
                            FilterChip(
                                selected = slippage == s,
                                onClick = { slippage = s },
                                label = { Text("$s%", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = SpyWhyColors.AccentGreen
                                )
                            )
                        }
                    }
                }
            }

            // Route info
            Card(
                colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rate", color = SpyWhyColors.TextSecondary)
                        Text("1 $fromToken = -- $toToken", color = SpyWhyColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Network Fee", color = SpyWhyColors.TextSecondary)
                        Text("~$0.00", color = SpyWhyColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Route", color = SpyWhyColors.TextSecondary)
                        Text("$fromToken > $toToken", color = SpyWhyColors.AccentGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Execute DEX swap */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.AccentGreen,
                    contentColor = SpyWhyColors.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Swap on $selectedDEX", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
