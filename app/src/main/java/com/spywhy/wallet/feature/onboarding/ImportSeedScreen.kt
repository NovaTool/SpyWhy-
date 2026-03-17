package com.spywhy.wallet.feature.onboarding

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
fun ImportSeedScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit
) {
    var wordCount by remember { mutableStateOf(12) }
    var seedWords by remember { mutableStateOf(List(12) { "" }) }
    var currentSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Wallet", color = SpyWhyColors.TextPrimary) },
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
            Text(
                "Enter your recovery seed phrase to restore your wallet.",
                color = SpyWhyColors.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Word count selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Words: ", color = SpyWhyColors.TextSecondary)
                listOf(12, 24).forEach { count ->
                    FilterChip(
                        selected = wordCount == count,
                        onClick = {
                            wordCount = count
                            seedWords = List(count) { i -> seedWords.getOrElse(i) { "" } }
                            errorMessage = null
                        },
                        label = { Text("$count") },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.2f),
                            selectedLabelColor = SpyWhyColors.AccentOrange
                        )
                    )
                }
            }

            // Seed word inputs in 2-column grid
            for (row in 0 until wordCount / 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..1) {
                        val index = row * 2 + col
                        OutlinedTextField(
                            value = seedWords[index],
                            onValueChange = { value ->
                                seedWords = seedWords.toMutableList().apply {
                                    this[index] = value.lowercase().trim()
                                }
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("${index + 1}", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SpyWhyColors.TextPrimary,
                                unfocusedTextColor = SpyWhyColors.TextPrimary,
                                focusedBorderColor = SpyWhyColors.AccentOrange,
                                unfocusedBorderColor = SpyWhyColors.BorderGray,
                                focusedLabelColor = SpyWhyColors.AccentOrange,
                                unfocusedLabelColor = SpyWhyColors.TextSecondary
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                    }
                }
            }

            // Paste from clipboard
            OutlinedButton(
                onClick = { /* Paste full mnemonic from clipboard and split */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.TextSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentPaste, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paste from clipboard")
            }

            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, null, tint = SpyWhyColors.AccentRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = SpyWhyColors.AccentRed, fontSize = 13.sp)
                    }
                }
            }

            // Import button
            Button(
                onClick = {
                    val allFilled = seedWords.all { it.isNotBlank() }
                    if (!allFilled) {
                        errorMessage = "Please fill in all seed words"
                    } else {
                        onImportComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = seedWords.count { it.isNotBlank() } >= wordCount / 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.AccentGreen,
                    contentColor = SpyWhyColors.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Wallet", fontWeight = FontWeight.Bold)
            }

            // Warning
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SpyWhyColors.AccentOrange.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Warning, null, tint = SpyWhyColors.AccentOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Never enter your seed phrase on any website or share it with anyone.",
                        color = SpyWhyColors.AccentOrange,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
