package com.spywhy.wallet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spywhy.wallet.core.util.Screen
import com.spywhy.wallet.core.util.SpyWhyTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Manages auto-lock state. In a full implementation this would live in a
 * dedicated DI module; declared here as a minimal injectable contract so
 * the activity compiles with Hilt.
 */
interface SecurityManager {
    /** Record the moment the user left the foreground. */
    fun recordPauseTimestamp()

    /**
     * Returns `true` when the elapsed time since the last pause exceeds
     * [com.spywhy.wallet.core.util.Constants.AUTO_LOCK_TIMEOUT_MS].
     */
    fun shouldLock(): Boolean
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var securityManager: SecurityManager

    // ── Lifecycle ───────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Screenshot / screen-recording protection
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        setContent {
            SpyWhyTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpyWhyNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        securityManager.recordPauseTimestamp()
        Timber.d("MainActivity paused – timestamp recorded for auto-lock")
    }

    override fun onResume() {
        super.onResume()
        if (securityManager.shouldLock()) {
            Timber.d("Auto-lock threshold exceeded – navigating to PIN entry")
            // In a full build this would navigate to a LockScreen or SetPin route.
            // For now we simply log; the nav integration will be wired when the
            // lock-screen composable is implemented.
        }
    }
}

// ── NavHost ─────────────────────────────────────────────────────────────

@Composable
fun SpyWhyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.Splash.route,
        modifier = modifier,
    ) {

        // ── Onboarding ──────────────────────────────────────────────────
        composable(Screen.Onboarding.Splash.route) {
            com.spywhy.wallet.feature.onboarding.SplashScreen(
                onInitComplete = {
                    navController.navigate(Screen.Onboarding.Welcome.route) {
                        popUpTo(Screen.Onboarding.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.Welcome.route) {
            com.spywhy.wallet.feature.onboarding.WelcomeScreen(
                onCreateWallet = {
                    navController.navigate(Screen.Onboarding.CreateWallet.route)
                },
                onImportWallet = {
                    navController.navigate(Screen.Onboarding.ImportSeed.route)
                },
                onWatchOnly = {
                    navController.navigate(Screen.Main.Dashboard.route)
                }
            )
        }
        composable(Screen.Onboarding.CreateWallet.route) {
            PlaceholderScreen("Create Wallet")
        }
        composable(Screen.Onboarding.ImportSeed.route) {
            PlaceholderScreen("Import Seed")
        }
        composable(Screen.Onboarding.SetPin.route) {
            PlaceholderScreen("Set PIN")
        }
        composable(Screen.Onboarding.BiometricSetup.route) {
            PlaceholderScreen("Biometric Setup")
        }

        // ── Main ────────────────────────────────────────────────────────
        composable(Screen.Main.Dashboard.route) {
            PlaceholderScreen("Dashboard")
        }
        composable(Screen.Main.WalletList.route) {
            PlaceholderScreen("Wallet List")
        }
        composable(
            route = Screen.Main.CoinDetail.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Coin Detail: $coinId")
        }
        composable(
            route = Screen.Main.Send.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Send: $coinId")
        }
        composable(
            route = Screen.Main.Receive.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Receive: $coinId")
        }
        composable(
            route = Screen.Main.TransactionHistory.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Transaction History: $coinId")
        }
        composable(
            route = Screen.Main.TransactionDetail.route,
            arguments = listOf(navArgument("txId") { type = NavType.StringType })
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("txId").orEmpty()
            PlaceholderScreen("Transaction Detail: $txId")
        }

        // ── HW Mode ────────────────────────────────────────────────────
        composable(Screen.HWMode.HWModeHome.route) {
            PlaceholderScreen("HW Mode Home")
        }
        composable(Screen.HWMode.ScanTxQR.route) {
            PlaceholderScreen("Scan TX QR")
        }
        composable(Screen.HWMode.ReviewSign.route) {
            PlaceholderScreen("Review & Sign")
        }
        composable(Screen.HWMode.DisplaySignedQR.route) {
            PlaceholderScreen("Display Signed QR")
        }
        composable(Screen.HWMode.PCConnection.route) {
            PlaceholderScreen("PC Connection")
        }

        // ── Market ──────────────────────────────────────────────────────
        composable(Screen.Market.MarketOverview.route) {
            PlaceholderScreen("Market Overview")
        }
        composable(
            route = Screen.Market.CoinChart.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Coin Chart: $coinId")
        }
        composable(Screen.Market.PriceAlerts.route) {
            PlaceholderScreen("Price Alerts")
        }
        composable(Screen.Market.AIAnalysis.route) {
            PlaceholderScreen("AI Analysis")
        }

        // ── Exchange ────────────────────────────────────────────────────
        composable(Screen.Exchange.SwapComparator.route) {
            PlaceholderScreen("Swap Comparator")
        }
        composable(Screen.Exchange.DEXSwap.route) {
            PlaceholderScreen("DEX Swap")
        }
        composable(Screen.Exchange.FiatConverter.route) {
            PlaceholderScreen("Fiat Converter")
        }

        // ── Standalone ─────────────────────────────────────────────────
        composable(Screen.Portfolio.route) {
            PlaceholderScreen("Portfolio")
        }
        composable(Screen.NFTGallery.route) {
            PlaceholderScreen("NFT Gallery")
        }

        // ── Settings ───────────────────────────────────────────────────
        composable(Screen.Settings.Root.route) {
            PlaceholderScreen("Settings")
        }
        composable(Screen.Settings.SecuritySettings.route) {
            PlaceholderScreen("Security Settings")
        }
        composable(Screen.Settings.NodeSettings.route) {
            PlaceholderScreen("Node Settings")
        }
        composable(Screen.Settings.Backup.route) {
            PlaceholderScreen("Backup")
        }

        // ── Advanced ───────────────────────────────────────────────────
        composable(Screen.StealthWallet.route) {
            PlaceholderScreen("Stealth Wallet")
        }
        composable(Screen.MempoolViewer.route) {
            PlaceholderScreen("Mempool Viewer")
        }
        composable(
            route = Screen.CoinControl.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Coin Control: $coinId")
        }
        composable(
            route = Screen.BatchSend.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            PlaceholderScreen("Batch Send: $coinId")
        }
    }
}

// ── Placeholder composable used by every route until real UIs land ──────

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}
