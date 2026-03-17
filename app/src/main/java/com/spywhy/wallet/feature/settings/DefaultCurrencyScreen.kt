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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.core.util.SpyWhyColors

data class CurrencyOption(
    val code: String,
    val name: String,
    val symbol: String
)

private val currencies = listOf(
    CurrencyOption("USD", "US Dollar", "$"),
    CurrencyOption("EUR", "Euro", "\u20AC"),
    CurrencyOption("GBP", "British Pound", "\u00A3"),
    CurrencyOption("JPY", "Japanese Yen", "\u00A5"),
    CurrencyOption("CHF", "Swiss Franc", "CHF"),
    CurrencyOption("CAD", "Canadian Dollar", "CA$"),
    CurrencyOption("AUD", "Australian Dollar", "A$"),
    CurrencyOption("CNY", "Chinese Yuan", "\u00A5"),
    CurrencyOption("BTC", "Bitcoin", "\u20BF"),
    CurrencyOption("ETH", "Ethereum", "\u039E")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultCurrencyScreen(
    onNavigateBack: () -> Unit
) {
    var selectedCurrency by remember { mutableStateOf("USD") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Default Currency", color = SpyWhyColors.TextPrimary) },
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
            Text(
                "Select your preferred display currency",
                color = SpyWhyColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            currencies.forEach { currency ->
                val isSelected = selectedCurrency == currency.code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCurrency = currency.code },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SpyWhyColors.AccentOrange.copy(alpha = 0.15f)
                        else SpyWhyColors.MediumGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currency.symbol,
                            color = if (isSelected) SpyWhyColors.AccentOrange else SpyWhyColors.TextSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currency.code,
                                color = if (isSelected) SpyWhyColors.AccentOrange else SpyWhyColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(currency.name, color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = SpyWhyColors.AccentOrange)
                        }
                    }
                }
            }
        }
    }
}
