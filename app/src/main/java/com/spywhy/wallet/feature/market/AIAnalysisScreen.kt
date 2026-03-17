package com.spywhy.wallet.feature.market

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.usecase.Signal
import com.spywhy.wallet.domain.usecase.TechnicalAnalysis
import com.spywhy.wallet.feature.market.viewmodel.AIAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(
    viewModel: AIAnalysisViewModel,
    availableCoins: List<String>,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "AI Analysis",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Coin selector dropdown
            CoinSelector(
                selectedCoin = uiState.selectedCoin,
                availableCoins = availableCoins,
                onCoinSelected = { viewModel.selectCoin(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = SpyWhyColors.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing ${uiState.selectedCoin.replaceFirstChar { it.uppercase() }}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpyWhyColors.TextSecondary
                        )
                    }
                }
            } else if (uiState.analysis != null) {
                val analysis = uiState.analysis!!

                // Signal card
                SignalCard(analysis = analysis)

                Spacer(modifier = Modifier.height(20.dp))

                // RSI gauge
                RSIGauge(rsi = analysis.rsi)

                Spacer(modifier = Modifier.height(20.dp))

                // MACD chart
                if (analysis.macd != null) {
                    MACDCard(
                        macdLine = analysis.macd.macdLine,
                        signalLine = analysis.macd.signalLine,
                        histogram = analysis.macd.histogram
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bollinger Bands
                if (analysis.bollingerBands != null) {
                    BollingerBandsCard(
                        upper = analysis.bollingerBands.upper,
                        middle = analysis.bollingerBands.middle,
                        lower = analysis.bollingerBands.lower
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SMA lines
                if (analysis.sma50 != null || analysis.sma200 != null) {
                    SMACard(sma50 = analysis.sma50, sma200 = analysis.sma200)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Summary text
                AnalysisDetailCard(label = "Summary") {
                    Text(
                        text = analysis.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a coin to analyze",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpyWhyColors.TextSecondary
                    )
                }
            }

            // Refresh button
            Button(
                onClick = { viewModel.refreshAnalysis() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.White,
                    contentColor = SpyWhyColors.Black
                ),
                enabled = uiState.selectedCoin.isNotEmpty() && !uiState.isAnalyzing
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Refresh Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CoinSelector(
    selectedCoin: String,
    availableCoins: List<String>,
    onCoinSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = "Select Coin",
        style = MaterialTheme.typography.labelLarge,
        color = SpyWhyColors.TextSecondary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box {
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
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (selectedCoin.isNotEmpty()) selectedCoin.replaceFirstChar { it.uppercase() }
                else "Choose a coin...",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedCoin.isNotEmpty()) SpyWhyColors.TextPrimary
                else SpyWhyColors.TextDisabled
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Select",
                tint = SpyWhyColors.TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SpyWhyColors.MediumGray)
        ) {
            availableCoins.forEach { coin ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = coin.replaceFirstChar { it.uppercase() },
                            color = if (coin == selectedCoin) SpyWhyColors.AccentGreen
                            else SpyWhyColors.TextPrimary
                        )
                    },
                    onClick = {
                        onCoinSelected(coin)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SignalCard(analysis: TechnicalAnalysis) {
    val signalColor = when (analysis.signal) {
        Signal.BUY -> SpyWhyColors.AccentGreen
        Signal.SELL -> SpyWhyColors.AccentRed
        Signal.HOLD -> SpyWhyColors.AccentYellow
    }
    val confidencePercent = (analysis.confidence * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(signalColor.copy(alpha = 0.08f))
            .border(
                width = 2.dp,
                color = signalColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Signal",
            style = MaterialTheme.typography.labelMedium,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = analysis.signal.name,
            style = MaterialTheme.typography.headlineLarge,
            color = signalColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Confidence: $confidencePercent%",
            style = MaterialTheme.typography.titleMedium,
            color = signalColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confidence bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SpyWhyColors.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(analysis.confidence.toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(signalColor)
            )
        }
    }
}

@Composable
private fun RSIGauge(rsi: Double?) {
    AnalysisDetailCard(label = "RSI (Relative Strength Index)") {
        if (rsi == null) {
            Text(
                text = "Insufficient data",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Semi-circular gauge
                Box(
                    modifier = Modifier
                        .size(180.dp, 100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.size(180.dp, 100.dp)) {
                        val strokeWidth = 14f
                        val radius = size.width / 2 - strokeWidth
                        val arcSize = Size(radius * 2, radius * 2)
                        val topLeft = Offset(
                            (size.width - arcSize.width) / 2,
                            size.height - radius
                        )

                        // Background arc
                        drawArc(
                            color = SpyWhyColors.LightGray,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Oversold zone (green, 0-30)
                        drawArc(
                            color = SpyWhyColors.AccentGreen.copy(alpha = 0.3f),
                            startAngle = 180f,
                            sweepAngle = 54f, // 30/100 * 180
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Overbought zone (red, 70-100)
                        drawArc(
                            color = SpyWhyColors.AccentRed.copy(alpha = 0.3f),
                            startAngle = 306f, // 180 + 70/100*180
                            sweepAngle = 54f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Value indicator
                        val rsiColor = when {
                            rsi < 30 -> SpyWhyColors.AccentGreen
                            rsi > 70 -> SpyWhyColors.AccentRed
                            else -> SpyWhyColors.AccentYellow
                        }
                        val sweepAngle = (rsi / 100.0 * 180.0).toFloat()
                        drawArc(
                            color = rsiColor,
                            startAngle = 180f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = "%.1f".format(rsi),
                        style = MaterialTheme.typography.headlineMedium,
                        color = when {
                            rsi < 30 -> SpyWhyColors.AccentGreen
                            rsi > 70 -> SpyWhyColors.AccentRed
                            else -> SpyWhyColors.AccentYellow
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0", style = MaterialTheme.typography.bodySmall, color = SpyWhyColors.TextSecondary)
                    Text(
                        text = when {
                            rsi < 30 -> "Oversold"
                            rsi > 70 -> "Overbought"
                            else -> "Neutral"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            rsi < 30 -> SpyWhyColors.AccentGreen
                            rsi > 70 -> SpyWhyColors.AccentRed
                            else -> SpyWhyColors.AccentYellow
                        }
                    )
                    Text(text = "100", style = MaterialTheme.typography.bodySmall, color = SpyWhyColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MACDCard(macdLine: Double, signalLine: Double, histogram: Double) {
    AnalysisDetailCard(label = "MACD") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IndicatorRow("MACD Line", "%.4f".format(macdLine), SpyWhyColors.TextPrimary)
            IndicatorRow("Signal Line", "%.4f".format(signalLine), SpyWhyColors.AccentOrange)
            IndicatorRow(
                "Histogram",
                "%.4f".format(histogram),
                if (histogram >= 0) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Simple histogram bar
            val maxVal = maxOf(kotlin.math.abs(histogram), 0.001)
            val fraction = (histogram / maxVal).toFloat().coerceIn(-1f, 1f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Negative side
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    if (fraction < 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxWidth(kotlin.math.abs(fraction))
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SpyWhyColors.AccentRed.copy(alpha = 0.6f))
                        )
                    }
                }

                // Center divider
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(SpyWhyColors.TextSecondary)
                )

                // Positive side
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    if (fraction > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(fraction)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SpyWhyColors.AccentGreen.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BollingerBandsCard(upper: Double, middle: Double, lower: Double) {
    AnalysisDetailCard(label = "Bollinger Bands") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IndicatorRow("Upper Band", "$${"%,.2f".format(upper)}", SpyWhyColors.AccentRed.copy(alpha = 0.8f))
            IndicatorRow("Middle (SMA)", "$${"%,.2f".format(middle)}", SpyWhyColors.TextPrimary)
            IndicatorRow("Lower Band", "$${"%,.2f".format(lower)}", SpyWhyColors.AccentGreen.copy(alpha = 0.8f))

            Spacer(modifier = Modifier.height(4.dp))

            // Band width visualization
            val bandWidth = upper - lower
            val relativePosition = if (bandWidth > 0) ((middle - lower) / bandWidth).toFloat() else 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpyWhyColors.SurfaceVariant)
            ) {
                // Upper band (red zone)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    SpyWhyColors.AccentGreen.copy(alpha = 0.2f),
                                    SpyWhyColors.AccentYellow.copy(alpha = 0.2f),
                                    SpyWhyColors.AccentRed.copy(alpha = 0.2f)
                                )
                            )
                        )
                )

                // Middle position indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (relativePosition * 300).dp.coerceIn(4.dp, 296.dp))
                        .size(4.dp, 24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SpyWhyColors.White)
                )
            }
        }
    }
}

@Composable
private fun SMACard(sma50: Double?, sma200: Double?) {
    AnalysisDetailCard(label = "Moving Averages") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sma50 != null) {
                IndicatorRow("SMA 50", "$${"%,.2f".format(sma50)}", SpyWhyColors.AccentOrange)
            }
            if (sma200 != null) {
                IndicatorRow("SMA 200", "$${"%,.2f".format(sma200)}", SpyWhyColors.AccentYellow)
            }

            if (sma50 != null && sma200 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val crossType = if (sma50 > sma200) "Golden Cross" else "Death Cross"
                val crossColor = if (sma50 > sma200) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(crossColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = crossType,
                        style = MaterialTheme.typography.labelLarge,
                        color = crossColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sma50 > sma200) "(Bullish)" else "(Bearish)",
                        style = MaterialTheme.typography.bodySmall,
                        color = crossColor
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun AnalysisDetailCard(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SpyWhyColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
