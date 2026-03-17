package com.spywhy.wallet.feature.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.market.viewmodel.ChartTimeRange
import com.spywhy.wallet.feature.market.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinChartScreen(
    coinId: String,
    viewModel: MarketViewModel,
    onNavigateBack: () -> Unit,
    onSetAlert: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coinData = uiState.selectedCoinPrice
    val chartData = uiState.chartData
    val selectedRange = uiState.selectedTimeRange
    val isFavorite = uiState.favoriteIds.contains(coinId)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {},
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
                // Favorite star
                IconButton(onClick = { viewModel.toggleFavorite(coinId) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) SpyWhyColors.AccentYellow else SpyWhyColors.TextSecondary
                    )
                }
                // Alert bell
                IconButton(onClick = { onSetAlert(coinId) }) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
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
                Text(
                    text = coinData?.name?.ifBlank { coinId.replaceFirstChar { it.uppercase() } }
                        ?: coinId.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpyWhyColors.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = coinData?.symbol?.ifBlank { coinId.take(4).uppercase() }
                        ?: coinId.take(4).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (coinData != null) {
                    val changeColor = if (coinData.change24h >= 0) SpyWhyColors.AccentGreen
                    else SpyWhyColors.AccentRed

                    Text(
                        text = formatPriceDetail(coinData.priceUsd),
                        style = MaterialTheme.typography.headlineLarge,
                        color = SpyWhyColors.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${if (coinData.change24h >= 0) "+" else ""}${"%.2f".format(coinData.change24h)}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = changeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "24h",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive chart
            if (chartData.isNotEmpty()) {
                InteractiveLineChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 8.dp),
                    lineColor = if ((coinData?.change24h ?: 0.0) >= 0) SpyWhyColors.AccentGreen
                    else SpyWhyColors.AccentRed
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading chart data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time range tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartTimeRange.entries.forEach { range ->
                    val isSelected = selectedRange == range
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) SpyWhyColors.White.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SpyWhyColors.White.copy(alpha = 0.3f)
                                else SpyWhyColors.BorderGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { viewModel.selectTimeRange(range, coinId) }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) SpyWhyColors.White
                            else SpyWhyColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Market stats
            if (coinData != null) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Market Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpyWhyColors.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    StatCard(
                        label = "Market Cap",
                        value = formatLargeNumber(coinData.marketCap)
                    )
                    StatCard(
                        label = "Volume 24h",
                        value = formatLargeNumber(coinData.volume24h)
                    )
                    StatCard(
                        label = "7d Change",
                        value = "${if (coinData.change7d >= 0) "+" else ""}${"%.2f".format(coinData.change7d)}%",
                        valueColor = if (coinData.change7d >= 0) SpyWhyColors.AccentGreen
                        else SpyWhyColors.AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InteractiveLineChart(
    data: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color
) {
    if (data.size < 2) return

    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchedValue by remember { mutableStateOf<String?>(null) }

    // Downsample to max 200 points to avoid rendering lag
    val sampledData = remember(data) {
        if (data.size <= 200) data
        else {
            val step = data.size.toFloat() / 200f
            (0 until 200).map { i -> data[(i * step).toInt().coerceAtMost(data.size - 1)] }
        }
    }

    Column(modifier = modifier) {
        // Touch value display
        if (touchedValue != null) {
            Text(
                text = touchedValue!!,
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> touchX = offset.x },
                        onDragEnd = {
                            touchX = -1f
                            touchedValue = null
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            touchX += dragAmount
                            val index = ((touchX / size.width) * (sampledData.size - 1))
                                .toInt()
                                .coerceIn(0, sampledData.size - 1)
                            touchedValue = formatPriceDetail(sampledData[index].second)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val index = ((offset.x / size.width) * (sampledData.size - 1))
                            .toInt()
                            .coerceIn(0, sampledData.size - 1)
                        touchedValue = formatPriceDetail(sampledData[index].second)
                        touchX = offset.x
                    }
                }
        ) {
            val prices = sampledData.map { it.second }
            val minPrice = prices.min()
            val maxPrice = prices.max()
            val range = (maxPrice - minPrice).coerceAtLeast(0.001)
            val stepX = size.width / (prices.size - 1)
            val paddingY = 8f

            val path = Path()
            val fillPath = Path()

            prices.forEachIndexed { index, price ->
                val x = index * stepX
                val y = paddingY + (size.height - 2 * paddingY) * (1 - (price - minPrice) / range).toFloat()
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Fill gradient
            fillPath.lineTo(size.width, size.height)
            fillPath.lineTo(0f, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.15f),
                        lineColor.copy(alpha = 0.0f)
                    )
                )
            )

            // Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5f)
            )

            // Touch crosshair
            if (touchX >= 0 && touchX <= size.width) {
                drawLine(
                    color = SpyWhyColors.TextSecondary.copy(alpha = 0.5f),
                    start = Offset(touchX, 0f),
                    end = Offset(touchX, size.height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )

                val index = ((touchX / size.width) * (prices.size - 1))
                    .toInt()
                    .coerceIn(0, prices.size - 1)
                val price = prices[index]
                val dotY = paddingY + (size.height - 2 * paddingY) * (1 - (price - minPrice) / range).toFloat()

                drawCircle(
                    color = lineColor,
                    radius = 6f,
                    center = Offset(touchX, dotY)
                )
                drawCircle(
                    color = SpyWhyColors.Black,
                    radius = 3f,
                    center = Offset(touchX, dotY)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color = SpyWhyColors.White
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatPriceDetail(price: Double): String {
    return when {
        price >= 1000 -> "$${"%,.2f".format(price)}"
        price >= 1 -> "$${"%,.4f".format(price)}"
        else -> "$${"%,.8f".format(price)}"
    }
}

private fun formatLargeNumber(value: Double): String {
    return when {
        value >= 1_000_000_000_000 -> "$${"%,.2f".format(value / 1_000_000_000_000)}T"
        value >= 1_000_000_000 -> "$${"%,.2f".format(value / 1_000_000_000)}B"
        value >= 1_000_000 -> "$${"%,.2f".format(value / 1_000_000)}M"
        value >= 1000 -> "$${"%,.0f".format(value)}"
        else -> "$${"%,.2f".format(value)}"
    }
}
