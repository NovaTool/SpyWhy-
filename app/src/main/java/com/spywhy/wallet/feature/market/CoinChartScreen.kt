package com.spywhy.wallet.feature.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.market.viewmodel.MarketViewModel
import com.spywhy.wallet.feature.market.viewmodel.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinChartScreen(
    coinId: String,
    viewModel: MarketViewModel,
    onNavigateBack: () -> Unit,
    onSetAlert: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coinPrice = uiState.coinList.find { it.coinId == coinId }
    val chartData = uiState.chartData
    val selectedTimeRange = uiState.selectedTimeRange
    val isFavorite = uiState.favoriteIds.contains(coinId)
    val scrollState = rememberScrollState()

    // Touch interaction state
    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchPrice by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpyWhyColors.TextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleFavorite(coinId) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) SpyWhyColors.AccentRed else SpyWhyColors.TextSecondary
                    )
                }
                IconButton(onClick = { onSetAlert(coinId) }) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsNone,
                        contentDescription = "Set Alert",
                        tint = SpyWhyColors.TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SpyWhyColors.Black
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Coin header
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SpyWhyColors.SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coinId.take(2).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = SpyWhyColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = coinId.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineSmall,
                            color = SpyWhyColors.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = coinId.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                val displayPrice = touchPrice ?: coinPrice?.priceUsd
                Text(
                    text = displayPrice?.let { formatCoinPrice(it) } ?: "--",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpyWhyColors.White,
                    fontWeight = FontWeight.Bold
                )

                // 24h change
                if (coinPrice != null) {
                    val change = coinPrice.change24h
                    val changeColor = if (change >= 0) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                    Text(
                        text = "${if (change >= 0) "+" else ""}${"%.2f".format(change)}% (24h)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive chart
            if (chartData.isNotEmpty()) {
                val minValue = chartData.minOf { it.second }
                val maxValue = chartData.maxOf { it.second }
                val range = (maxValue - minValue).coerceAtLeast(0.0001)
                val changePositive = chartData.last().second >= chartData.first().second
                val lineColor = if (changePositive) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 8.dp)
                        .pointerInput(chartData) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val index = ((offset.x / size.width) * (chartData.size - 1))
                                        .toInt()
                                        .coerceIn(0, chartData.size - 1)
                                    touchX = offset.x
                                    touchPrice = chartData[index].second
                                    tryAwaitRelease()
                                    touchX = -1f
                                    touchPrice = null
                                }
                            )
                        }
                        .drawBehind {
                            val path = Path()
                            val widthStep = size.width / (chartData.size - 1)

                            chartData.forEachIndexed { index, (_, price) ->
                                val x = index * widthStep
                                val y = size.height - ((price - minValue) / range * size.height).toFloat()
                                if (index == 0) path.moveTo(x, y)
                                else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = 2.5f)
                            )

                            // Touch crosshair
                            if (touchX >= 0f) {
                                drawLine(
                                    color = SpyWhyColors.TextSecondary.copy(alpha = 0.5f),
                                    start = Offset(touchX, 0f),
                                    end = Offset(touchX, size.height),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(8f, 6f)
                                    )
                                )
                            }
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading chart...",
                        color = SpyWhyColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time range tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeRange.entries.forEach { range ->
                    val isSelected = selectedTimeRange == range
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) SpyWhyColors.White else SpyWhyColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) SpyWhyColors.LightGray else Color.Transparent
                            )
                            .clickable { viewModel.selectTimeRange(range, coinId) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Stats grid
            if (coinPrice != null) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpyWhyColors.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Market Cap",
                            value = formatLargeNumber(coinPrice.marketCap),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Volume 24h",
                            value = formatLargeNumber(coinPrice.volume24h),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    StatCard(
                        label = "Circulating Supply",
                        value = if (coinPrice.priceUsd > 0) {
                            formatLargeNumber(coinPrice.marketCap / coinPrice.priceUsd)
                        } else "--",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCoinPrice(price: Double): String {
    return when {
        price >= 1000 -> "$${"%,.2f".format(price)}"
        price >= 1 -> "$${"%,.4f".format(price)}"
        else -> "$${"%,.8f".format(price)}"
    }
}

private fun formatLargeNumber(value: Double): String {
    return when {
        value >= 1_000_000_000_000 -> "$${"%,.1f".format(value / 1_000_000_000_000)}T"
        value >= 1_000_000_000 -> "$${"%,.1f".format(value / 1_000_000_000)}B"
        value >= 1_000_000 -> "$${"%,.1f".format(value / 1_000_000)}M"
        value >= 1_000 -> "$${"%,.0f".format(value)}"
        else -> "${"%.2f".format(value)}"
    }
}
