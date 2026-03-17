package com.spywhy.wallet.feature.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.repository.PriceAlert
import com.spywhy.wallet.feature.market.viewmodel.MarketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertScreen(
    viewModel: MarketViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val alerts = uiState.priceAlerts
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SpyWhyColors.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Price Alerts",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SpyWhyColors.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SpyWhyColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpyWhyColors.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = SpyWhyColors.White,
                contentColor = SpyWhyColors.Black,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Alert"
                )
            }
        }
    ) { paddingValues ->
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No price alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpyWhyColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to add a new alert",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpyWhyColors.TextDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(alerts, key = { it.id }) { alert ->
                    AlertItem(
                        alert = alert,
                        onToggle = { viewModel.toggleAlert(alert.id, it) },
                        onDelete = { viewModel.deleteAlert(alert.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Add alert bottom sheet
        if (showAddSheet) {
            AddAlertBottomSheet(
                sheetState = sheetState,
                availableCoins = uiState.coinList.map { it.coinId },
                onDismiss = {
                    scope.launch { sheetState.hide() }
                    showAddSheet = false
                },
                onAdd = { coinId, price, isAbove ->
                    viewModel.createAlert(coinId, price, isAbove)
                    scope.launch { sheetState.hide() }
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun AlertItem(
    alert: PriceAlert,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin icon placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = alert.coinId.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = SpyWhyColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.coinId.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (alert.isAbove) Icons.Filled.ArrowUpward
                    else Icons.Filled.ArrowDownward,
                    contentDescription = if (alert.isAbove) "Above" else "Below",
                    tint = if (alert.isAbove) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$${"%,.2f".format(alert.targetPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (alert.isAbove) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Switch(
            checked = alert.isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpyWhyColors.White,
                checkedTrackColor = SpyWhyColors.AccentGreen,
                uncheckedThumbColor = SpyWhyColors.TextSecondary,
                uncheckedTrackColor = SpyWhyColors.LightGray
            )
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = SpyWhyColors.TextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlertBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    availableCoins: List<String>,
    onDismiss: () -> Unit,
    onAdd: (coinId: String, price: Double, isAbove: Boolean) -> Unit
) {
    var selectedCoin by remember { mutableStateOf(availableCoins.firstOrNull() ?: "") }
    var priceInput by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpyWhyColors.DarkGray,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SpyWhyColors.BorderGray)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Price Alert",
                style = MaterialTheme.typography.headlineSmall,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Coin selector
            Text(
                text = "Select Coin",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCoins.take(5).forEach { coin ->
                    val isSelected = selectedCoin == coin
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) SpyWhyColors.White.copy(alpha = 0.12f)
                                else SpyWhyColors.CardBackground
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SpyWhyColors.White.copy(alpha = 0.3f)
                                else SpyWhyColors.BorderGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCoin = coin }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = coin.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) SpyWhyColors.White else SpyWhyColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Target price
            Text(
                text = "Target Price (USD)",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "0.00", color = SpyWhyColors.TextDisabled) },
                prefix = { Text(text = "$", color = SpyWhyColors.TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpyWhyColors.TextPrimary,
                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                    cursorColor = SpyWhyColors.White,
                    focusedBorderColor = SpyWhyColors.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = SpyWhyColors.BorderGray,
                    focusedContainerColor = SpyWhyColors.CardBackground,
                    unfocusedContainerColor = SpyWhyColors.CardBackground
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Direction
            Text(
                text = "Alert Direction",
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DirectionOption(
                    label = "Price Goes Above",
                    icon = Icons.Filled.ArrowUpward,
                    color = SpyWhyColors.AccentGreen,
                    isSelected = isAbove,
                    onClick = { isAbove = true },
                    modifier = Modifier.weight(1f)
                )
                DirectionOption(
                    label = "Price Goes Below",
                    icon = Icons.Filled.ArrowDownward,
                    color = SpyWhyColors.AccentRed,
                    isSelected = !isAbove,
                    onClick = { isAbove = false },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Add button
            Button(
                onClick = {
                    val price = priceInput.toDoubleOrNull()
                    if (price != null && selectedCoin.isNotEmpty()) {
                        onAdd(selectedCoin, price, isAbove)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.White,
                    contentColor = SpyWhyColors.Black
                ),
                enabled = priceInput.toDoubleOrNull() != null && selectedCoin.isNotEmpty()
            ) {
                Text(
                    text = "Add Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DirectionOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.1f) else SpyWhyColors.CardBackground
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color.copy(alpha = 0.5f) else SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) color else SpyWhyColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) color else SpyWhyColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
