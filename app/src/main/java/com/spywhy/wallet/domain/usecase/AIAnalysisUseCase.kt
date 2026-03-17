package com.spywhy.wallet.domain.usecase

import com.spywhy.wallet.domain.repository.MarketRepository
import javax.inject.Inject
import kotlin.math.abs

class AIAnalysisUseCase @Inject constructor(
    private val marketRepository: MarketRepository
) {
    suspend fun analyzeWithIndicators(coinId: String): TechnicalAnalysis {
        val chartData = marketRepository.getMarketChart(coinId, 90)
        val prices = chartData.map { it.second }

        if (prices.size < 26) {
            return TechnicalAnalysis(
                rsi = null, macd = null, signal = Signal.HOLD,
                confidence = 0.0, summary = "Insufficient data for analysis"
            )
        }

        val rsi = calculateRSI(prices, 14)
        val macd = calculateMACD(prices)
        val bollingerBands = calculateBollingerBands(prices, 20, 2.0)
        val sma50 = calculateSMA(prices, 50)
        val sma200 = calculateSMA(prices, 200)

        val currentPrice = prices.last()
        var bullishSignals = 0
        var bearishSignals = 0

        // RSI analysis
        if (rsi != null) {
            when {
                rsi < 30 -> bullishSignals += 2
                rsi < 40 -> bullishSignals += 1
                rsi > 70 -> bearishSignals += 2
                rsi > 60 -> bearishSignals += 1
            }
        }

        // MACD analysis
        if (macd != null) {
            if (macd.histogram > 0 && macd.histogram > macd.previousHistogram) bullishSignals += 2
            else if (macd.histogram < 0 && macd.histogram < macd.previousHistogram) bearishSignals += 2
            if (macd.macdLine > macd.signalLine) bullishSignals += 1
            else bearishSignals += 1
        }

        // Bollinger Bands
        if (bollingerBands != null) {
            when {
                currentPrice <= bollingerBands.lower -> bullishSignals += 1
                currentPrice >= bollingerBands.upper -> bearishSignals += 1
            }
        }

        // Moving average crossover
        if (sma50 != null && sma200 != null) {
            if (sma50 > sma200) bullishSignals += 1 // Golden cross zone
            else bearishSignals += 1 // Death cross zone
        }

        val totalSignals = bullishSignals + bearishSignals
        val signal = when {
            totalSignals == 0 -> Signal.HOLD
            bullishSignals > bearishSignals * 1.5 -> Signal.BUY
            bearishSignals > bullishSignals * 1.5 -> Signal.SELL
            else -> Signal.HOLD
        }

        val confidence = if (totalSignals > 0) {
            abs(bullishSignals - bearishSignals).toDouble() / totalSignals
        } else 0.0

        val summary = buildString {
            append("RSI: ${rsi?.let { "%.1f".format(it) } ?: "N/A"}")
            append(" | MACD: ${macd?.let { if (it.histogram > 0) "Bullish" else "Bearish" } ?: "N/A"}")
            append(" | Signal: ${signal.name}")
            append(" | Confidence: ${"%.0f".format(confidence * 100)}%")
        }

        return TechnicalAnalysis(
            rsi = rsi,
            macd = macd,
            signal = signal,
            confidence = confidence,
            summary = summary,
            bollingerBands = bollingerBands,
            sma50 = sma50,
            sma200 = sma200
        )
    }

    private fun calculateRSI(prices: List<Double>, period: Int): Double? {
        if (prices.size < period + 1) return null
        val changes = prices.zipWithNext { a, b -> b - a }
        val recentChanges = changes.takeLast(period)
        val gains = recentChanges.filter { it > 0 }
        val losses = recentChanges.filter { it < 0 }.map { abs(it) }
        val avgGain = if (gains.isNotEmpty()) gains.average() else 0.0
        val avgLoss = if (losses.isNotEmpty()) losses.average() else 0.0
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun calculateMACD(prices: List<Double>): MACDResult? {
        if (prices.size < 26) return null
        val ema12 = calculateEMA(prices, 12)
        val ema26 = calculateEMA(prices, 26)
        if (ema12 == null || ema26 == null) return null

        val macdValues = ema12.zip(ema26).map { (a, b) -> a - b }
        val signalLine = calculateEMA(macdValues, 9) ?: return null

        val currentMacd = macdValues.last()
        val currentSignal = signalLine.last()
        val histogram = currentMacd - currentSignal
        val previousHistogram = if (macdValues.size > 1 && signalLine.size > 1) {
            macdValues[macdValues.size - 2] - signalLine[signalLine.size - 2]
        } else histogram

        return MACDResult(currentMacd, currentSignal, histogram, previousHistogram)
    }

    private fun calculateEMA(prices: List<Double>, period: Int): List<Double>? {
        if (prices.size < period) return null
        val multiplier = 2.0 / (period + 1)
        val result = mutableListOf<Double>()
        result.add(prices.take(period).average())
        for (i in period until prices.size) {
            val ema = (prices[i] - result.last()) * multiplier + result.last()
            result.add(ema)
        }
        return result
    }

    private fun calculateBollingerBands(prices: List<Double>, period: Int, stdDevMultiplier: Double): BollingerBands? {
        if (prices.size < period) return null
        val recentPrices = prices.takeLast(period)
        val sma = recentPrices.average()
        val variance = recentPrices.map { (it - sma) * (it - sma) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        return BollingerBands(
            upper = sma + stdDevMultiplier * stdDev,
            middle = sma,
            lower = sma - stdDevMultiplier * stdDev
        )
    }

    private fun calculateSMA(prices: List<Double>, period: Int): Double? {
        if (prices.size < period) return null
        return prices.takeLast(period).average()
    }
}

data class TechnicalAnalysis(
    val rsi: Double?,
    val macd: MACDResult?,
    val signal: Signal,
    val confidence: Double,
    val summary: String,
    val bollingerBands: BollingerBands? = null,
    val sma50: Double? = null,
    val sma200: Double? = null
)

data class MACDResult(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double,
    val previousHistogram: Double
)

data class BollingerBands(
    val upper: Double,
    val middle: Double,
    val lower: Double
)

enum class Signal { BUY, SELL, HOLD }
