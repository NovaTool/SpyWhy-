package com.spywhy.wallet.domain.model

enum class Blockchain(
    val coinType: Int,
    val ticker: String,
    val displayName: String
) {
    BITCOIN(coinType = 0, ticker = "BTC", displayName = "Bitcoin"),
    ETHEREUM(coinType = 60, ticker = "ETH", displayName = "Ethereum"),
    SOLANA(coinType = 501, ticker = "SOL", displayName = "Solana"),
    LITECOIN(coinType = 2, ticker = "LTC", displayName = "Litecoin"),
    MONERO(coinType = 128, ticker = "XMR", displayName = "Monero");

    companion object {
        fun fromTicker(ticker: String): Blockchain? =
            entries.find { it.ticker.equals(ticker, ignoreCase = true) }

        fun fromCoinType(coinType: Int): Blockchain? =
            entries.find { it.coinType == coinType }
    }
}
