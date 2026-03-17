package com.spywhy.wallet.core.util

/**
 * Navigation routes and nav graph definition for SpyWhy hardware wallet.
 * All routes are defined as a sealed class hierarchy for type safety.
 */
sealed class Screen(val route: String) {

    // ── Onboarding ──────────────────────────────────────────────────────
    sealed class Onboarding(route: String) : Screen(route) {
        data object Splash : Onboarding("onboarding/splash")
        data object Welcome : Onboarding("onboarding/welcome")
        data object CreateWallet : Onboarding("onboarding/create_wallet")
        data object ImportSeed : Onboarding("onboarding/import_seed")
        data object SetPin : Onboarding("onboarding/set_pin")
        data object BiometricSetup : Onboarding("onboarding/biometric_setup")
    }

    // ── Main ────────────────────────────────────────────────────────────
    sealed class Main(route: String) : Screen(route) {
        data object Dashboard : Main("main/dashboard")
        data object WalletList : Main("main/wallet_list")
        data object CoinDetail : Main("main/coin_detail/{coinId}") {
            fun createRoute(coinId: String): String = "main/coin_detail/$coinId"
        }
        data object Send : Main("main/send/{coinId}") {
            fun createRoute(coinId: String): String = "main/send/$coinId"
        }
        data object Receive : Main("main/receive/{coinId}") {
            fun createRoute(coinId: String): String = "main/receive/$coinId"
        }
        data object TransactionHistory : Main("main/tx_history/{coinId}") {
            fun createRoute(coinId: String): String = "main/tx_history/$coinId"
        }
        data object TransactionDetail : Main("main/tx_detail/{txId}") {
            fun createRoute(txId: String): String = "main/tx_detail/$txId"
        }
    }

    // ── HW Mode (Air-gapped hardware wallet) ───────────────────────────
    sealed class HWMode(route: String) : Screen(route) {
        data object HWModeHome : HWMode("hwmode/home")
        data object ScanTxQR : HWMode("hwmode/scan_tx_qr")
        data object ReviewSign : HWMode("hwmode/review_sign")
        data object DisplaySignedQR : HWMode("hwmode/display_signed_qr")
        data object PCConnection : HWMode("hwmode/pc_connection")
    }

    // ── Market ──────────────────────────────────────────────────────────
    sealed class Market(route: String) : Screen(route) {
        data object MarketOverview : Market("market/overview")
        data object CoinChart : Market("market/coin_chart/{coinId}") {
            fun createRoute(coinId: String): String = "market/coin_chart/$coinId"
        }
        data object PriceAlerts : Market("market/price_alerts")
        data object AIAnalysis : Market("market/ai_analysis")
    }

    // ── Exchange ────────────────────────────────────────────────────────
    sealed class Exchange(route: String) : Screen(route) {
        data object SwapComparator : Exchange("exchange/swap_comparator")
        data object DEXSwap : Exchange("exchange/dex_swap")
        data object FiatConverter : Exchange("exchange/fiat_converter")
    }

    // ── Standalone screens ──────────────────────────────────────────────
    data object Portfolio : Screen("portfolio")
    data object NFTGallery : Screen("nft_gallery")

    // ── Settings ────────────────────────────────────────────────────────
    sealed class Settings(route: String) : Screen(route) {
        data object Root : Settings("settings/root")
        data object SecuritySettings : Settings("settings/security")
        data object NodeSettings : Settings("settings/nodes")
        data object Backup : Settings("settings/backup")
    }

    // ── Advanced / Power-user screens ───────────────────────────────────
    data object StealthWallet : Screen("stealth_wallet")
    data object MempoolViewer : Screen("mempool_viewer")
    data object CoinControl : Screen("coin_control/{coinId}") {
        fun createRoute(coinId: String): String = "coin_control/$coinId"
    }
    data object BatchSend : Screen("batch_send/{coinId}") {
        fun createRoute(coinId: String): String = "batch_send/$coinId"
    }
}
