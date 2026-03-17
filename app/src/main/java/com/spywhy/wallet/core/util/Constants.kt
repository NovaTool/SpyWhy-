package com.spywhy.wallet.core.util

object Constants {
    const val DATABASE_NAME = "spywhy_wallet_db"
    const val KEYSTORE_ALIAS = "spywhy_master_key"
    const val PIN_KEYSTORE_ALIAS = "spywhy_pin_key"
    const val ENCRYPTED_PREFS_NAME = "spywhy_secure_prefs"

    const val MAX_PIN_ATTEMPTS = 10
    const val AUTO_LOCK_TIMEOUT_MS = 60_000L
    const val PIN_MIN_LENGTH = 6
    const val PIN_MAX_LENGTH = 8

    const val DUST_THRESHOLD_BTC = 546L
    const val DUST_THRESHOLD_ETH = 1000L

    // BIP44 coin types
    const val BTC_COIN_TYPE = 0
    const val ETH_COIN_TYPE = 60
    const val SOL_COIN_TYPE = 501
    const val LTC_COIN_TYPE = 2
    const val XMR_COIN_TYPE = 128

    // BIP44 path template: m/44'/{coin_type}'/0'/0/{index}
    const val BIP44_PURPOSE = 44

    // API URLs
    const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"
    const val SIMPLESWAP_BASE_URL = "https://api.simpleswap.io/"
    const val CHANGENOW_BASE_URL = "https://api.changenow.io/v2/"
    const val SIDESHIFT_BASE_URL = "https://sideshift.ai/api/v2/"
    const val ONEINCH_BASE_URL = "https://api.1inch.dev/swap/v6.0/"

    // Auto-update
    const val UPDATE_VERSION_URL = "https://raw.githubusercontent.com/NovaTool/SpyWhy-/claude/hardware-wallet-apk-2s5gN/version.json"
    const val UPDATE_CHECK_INTERVAL_MS = 3_600_000L // 1 hour

    // QR Protocol
    const val QR_PROTOCOL_VERSION = 1
    const val QR_MAX_BYTES_SINGLE = 2953
    const val QR_ANIMATION_DELAY_MS = 300L

    // Blockchain nodes (defaults)
    const val BTC_NODE_DEFAULT = "electrum.blockstream.info:50002"
    const val ETH_NODE_DEFAULT = "https://eth-mainnet.g.alchemy.com/v2/demo"
    const val SOL_NODE_DEFAULT = "https://api.mainnet-beta.solana.com"
    const val LTC_NODE_DEFAULT = "electrum-ltc.bysh.me:50002"
}
