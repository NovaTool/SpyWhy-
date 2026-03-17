package com.spywhy.wallet.feature.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
    val sheetState = rememberModalBottomSheetState()
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
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = SpyWhyColors.TextDisabled,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No price alerts set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpyWhyColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to create your first alert",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(
                    items = alerts,
                    key = { it.id }
                ) { alert ->
                    AlertItem(
                        alert = alert,
                        onToggle = { viewModel.toggleAlert(alert.id, !alert.isActive) },
                        onDelete = { viewModel.deleteAlert(alert.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Add alert bottom sheet
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState = sheetState,
                containerColor = SpyWhyColors.DarkGray,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SpyWhyColors.BorderGray)
                    )
                }
            ) {
                AddAlertContent(
                    availableCoins = uiState.coinList.map { it.coinId },
                    onCreateAlert = { coinId, price, isAbove ->
                        viewModel.createAlert(coinId, price, isAbove)
                        scope.launch {
                            sheetState.hide()
                            showAddSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AlertItem(
    alert: PriceAlert,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
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
                    text = "${if (alert.isAbove) "Above" else "Below"} $${"%,.2f".format(alert.targetPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alert.isAbove) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                )
            }
        }

        Switch(
            checked = alert.isActive,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpyWhyColors.White,
                checkedTrackColor = SpyWhyColors.AccentGreen,
                uncheckedThumbColor = SpyWhyColors.TextSecondary,
                uncheckedTrackColor = SpyWhyColors.LightGray,
                uncheckedBorderColor = SpyWhyColors.BorderGray
            )
        )

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = SpyWhyColors.AccentRed.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddAlertContent(
    availableCoins: List<String>,
    onCreateAlert: (coinId: String, price: Double, isAbove: Boolean) -> Unit
) {
    var selectedCoin by remember { mutableStateOf(availableCoins.firstOrNull() ?: "") }
    var priceInput by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }
    var coinDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "New Price Alert",
            style = MaterialTheme.typography.headlineSmall,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Select coin
        Text(
            text = "Coin",
            style = MaterialTheme.typography.labelLarge,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedTextField(
                value = selectedCoin.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { coinDropdownExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Select",
                            tint = SpyWhyColors.TextSecondary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpyWhyColors.TextPrimary,
                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                    focusedBorderColor = SpyWhyColors.BorderGray,
                    unfocusedBorderColor = SpyWhyColors.BorderGray,
                    focusedContainerColor = SpyWhyColors.CardBackground,
                    unfocusedContainerColor = SpyWhyColors.CardBackground
                )
            )

            DropdownMenu(
                expanded = coinDropdownExpanded,
                onDismissRequest = { coinDropdownExpanded = false },
                modifier = Modifier.background(SpyWhyColors.MediumGray)
            ) {
                availableCoins.forEach { coin ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = coin.replaceFirstChar { it.uppercase() },
                                color = SpyWhyColors.TextPrimary
                            )
                        },
                        onClick = {
                            selectedCoin = coin
                            coinDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            prefix = { Text(text = "$ ", color = SpyWhyColors.TextSecondary) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Direction
        Text(
            text = "Direction",
            style = MaterialTheme.typography.labelLarge,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DirectionButton(
                label = "Above",
                icon = Icons.Filled.ArrowUpward,
                isSelected = isAbove,
                color = SpyWhyColors.AccentGreen,
                onClick = { isAbove = true },
                modifier = Modifier.weight(1f)
            )
            DirectionButton(
                label = "Below",
                icon = Icons.Filled.ArrowDownward,
                isSelected = !isAbove,
                color = SpyWhyColors.AccentRed,
                onClick = { isAbove = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create button
        Button(
            onClick = {
                val price = priceInput.toDoubleOrNull() ?: return@Button
                onCreateAlert(selectedCoin, price, isAbove)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpyWhyColors.White,
                contentColor = SpyWhyColors.Black
            ),
            enabled = priceInput.toDoubleOrNull() != null && selectedCoin.isNotEmpty()
        ) {
            Text(
                text = "Create Alert",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DirectionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.15f)
                else SpyWhyColors.CardBackground
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color.copy(alpha = 0.5f)
                else SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) color else SpyWhyColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) color else SpyWhyColors.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
