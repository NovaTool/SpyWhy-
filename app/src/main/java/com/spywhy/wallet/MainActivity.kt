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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.spywhy.wallet.core.database.dao.WalletDao
import com.spywhy.wallet.core.update.UpdateDialog
import com.spywhy.wallet.core.update.UpdateInfo
import com.spywhy.wallet.core.update.UpdateManager
import com.spywhy.wallet.core.update.UpdateState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spywhy.wallet.core.util.Screen
import com.spywhy.wallet.core.util.SpyWhyTheme
import com.spywhy.wallet.feature.exchange.DEXSwapScreen
import com.spywhy.wallet.feature.exchange.FiatConverterScreen
import com.spywhy.wallet.feature.exchange.SwapComparatorScreen
import com.spywhy.wallet.feature.history.MempoolViewerScreen
import com.spywhy.wallet.feature.history.TransactionListScreen
import com.spywhy.wallet.feature.hwmode.DisplaySignedQRScreen
import com.spywhy.wallet.feature.hwmode.HWModeScreen
import com.spywhy.wallet.feature.hwmode.PCConnectionScreen
import com.spywhy.wallet.feature.hwmode.ReviewSignScreen
import com.spywhy.wallet.feature.hwmode.ScanTxQRScreen
import com.spywhy.wallet.feature.market.AIAnalysisScreen
import com.spywhy.wallet.feature.market.CoinChartScreen
import com.spywhy.wallet.feature.market.MarketOverviewScreen
import com.spywhy.wallet.feature.market.PriceAlertScreen
import com.spywhy.wallet.feature.nft.NFTGalleryScreen
import com.spywhy.wallet.feature.onboarding.BiometricSetupScreen
import com.spywhy.wallet.feature.onboarding.CreateWalletScreen
import com.spywhy.wallet.feature.onboarding.ImportSeedScreen
import com.spywhy.wallet.feature.onboarding.SetPinScreen
import com.spywhy.wallet.feature.onboarding.SplashScreen
import com.spywhy.wallet.feature.onboarding.WelcomeScreen
import com.spywhy.wallet.feature.portfolio.PortfolioScreen
import com.spywhy.wallet.feature.receive.ReceiveScreen
import com.spywhy.wallet.feature.send.BatchSendScreen
import com.spywhy.wallet.feature.send.CoinControlScreen
import com.spywhy.wallet.feature.send.ConfirmTxScreen
import com.spywhy.wallet.feature.send.FeeSelectionScreen
import com.spywhy.wallet.feature.send.SendScreen
import com.spywhy.wallet.feature.settings.AboutScreen
import com.spywhy.wallet.feature.settings.BackupScreen
import com.spywhy.wallet.feature.settings.DefaultCurrencyScreen
import com.spywhy.wallet.feature.settings.ExportPrivateKeysScreen
import com.spywhy.wallet.feature.settings.MessageSigningScreen
import com.spywhy.wallet.feature.settings.NodeSettingsScreen
import com.spywhy.wallet.feature.settings.NotificationsScreen
import com.spywhy.wallet.feature.settings.PanicModeScreen
import com.spywhy.wallet.feature.settings.SecuritySettingsScreen
import com.spywhy.wallet.feature.settings.SettingsScreen
import com.spywhy.wallet.feature.settings.WidgetsScreen
import com.spywhy.wallet.feature.stealth.StealthWalletScreen
import com.spywhy.wallet.feature.wallet.AddressScreen
import com.spywhy.wallet.feature.wallet.CoinDetailScreen
import com.spywhy.wallet.feature.wallet.DashboardScreen
import com.spywhy.wallet.feature.wallet.WalletListScreen
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
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var securityManager: SecurityManager

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var walletDao: WalletDao

    @Inject
    lateinit var settingsPreferences: com.spywhy.wallet.core.settings.SettingsPreferences

    private var isLocked by mutableStateOf(false)
    private var biometricEnabled by mutableStateOf(true)
    private var pendingUpdate by mutableStateOf<UpdateInfo?>(null)
    private var hasExistingWallet by mutableStateOf(false)

    // ── Lifecycle ───────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Screenshot / screen-recording protection
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        // Check if a wallet already exists
        lifecycleScope.launch {
            val count = walletDao.getWalletCount()
            hasExistingWallet = count > 0
            Timber.d("Wallet count on startup: $count")
        }

        // Check for updates on launch
        lifecycleScope.launch {
            val update = updateManager.checkForUpdate()
            if (update != null) {
                Timber.d("Update available: ${update.versionName}")
                pendingUpdate = update
            }
        }

        setContent {
            SpyWhyTheme {
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    SpyWhyNavHost(
                        navController = navController,
                        hasExistingWallet = hasExistingWallet,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isLocked) {
                        BiometricLockOverlay(
                            onUnlockRequest = { promptBiometric() }
                        )
                    }

                    // Update dialog
                    pendingUpdate?.let { update ->
                        val currentUpdateState by updateManager.updateState.collectAsState()
                        val currentProgress by updateManager.downloadProgress.collectAsState()
                        val currentError by updateManager.errorMessage.collectAsState()

                        UpdateDialog(
                            updateInfo = update,
                            updateState = currentUpdateState,
                            downloadProgress = currentProgress,
                            errorMessage = currentError,
                            onUpdate = {
                                updateManager.downloadAndInstall(update)
                            },
                            onDismiss = {
                                updateManager.resetState()
                                pendingUpdate = null
                            }
                        )
                    }
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
        if (securityManager.shouldLock() && biometricEnabled) {
            Timber.d("Auto-lock threshold exceeded – requesting biometric")
            isLocked = true
            promptBiometric()
        }
    }

    fun checkForUpdateManual(onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            val update = updateManager.checkForUpdate()
            if (update != null) {
                Timber.d("Manual update check: update available ${update.versionName}")
                pendingUpdate = update
                onResult(true)
            } else {
                Timber.d("Manual update check: already up to date")
                onResult(false)
            }
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun promptBiometric() {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: androidx.biometric.BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    isLocked = false
                    Timber.d("Biometric unlock succeeded")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.w("Biometric error: $errString")
                    // Keep locked – user can tap to retry
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("Biometric auth failed – try again")
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("SpyWhy Wallet")
            .setSubtitle("Authenticate to unlock")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

// ── NavHost ─────────────────────────────────────────────────────────────

@Composable
fun SpyWhyNavHost(
    navController: NavHostController,
    hasExistingWallet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.Splash.route,
        modifier = modifier,
    ) {

        // ── Onboarding ──────────────────────────────────────────────────
        composable(Screen.Onboarding.Splash.route) {
            SplashScreen(
                hasExistingWallet = hasExistingWallet,
                onInitComplete = {
                    navController.navigate(Screen.Onboarding.Welcome.route) {
                        popUpTo(Screen.Onboarding.Splash.route) { inclusive = true }
                    }
                },
                onWalletExists = {
                    navController.navigate(Screen.Main.Dashboard.createRoute()) {
                        popUpTo(Screen.Onboarding.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.Welcome.route) {
            WelcomeScreen(
                onCreateWallet = {
                    navController.navigate(Screen.Onboarding.CreateWallet.route)
                },
                onImportWallet = {
                    navController.navigate(Screen.Onboarding.ImportSeed.route)
                },
                onWatchOnly = {
                    navController.navigate(Screen.Main.Dashboard.createRoute(watchOnly = true)) {
                        popUpTo(Screen.Onboarding.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.CreateWallet.route) {
            CreateWalletScreen(
                onBack = { navController.popBackStack() },
                onWalletCreated = {
                    navController.navigate(Screen.Onboarding.SetPin.route) {
                        popUpTo(Screen.Onboarding.Welcome.route)
                    }
                }
            )
        }
        composable(Screen.Onboarding.ImportSeed.route) {
            ImportSeedScreen(
                onNavigateBack = { navController.popBackStack() },
                onImportComplete = {
                    navController.navigate(Screen.Onboarding.SetPin.route) {
                        popUpTo(Screen.Onboarding.Welcome.route)
                    }
                }
            )
        }
        composable(Screen.Onboarding.SetPin.route) {
            SetPinScreen(
                onNavigateBack = { navController.popBackStack() },
                onPinSet = {
                    navController.navigate(Screen.Onboarding.BiometricSetup.route)
                }
            )
        }
        composable(Screen.Onboarding.BiometricSetup.route) {
            BiometricSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = {
                    navController.navigate(Screen.Main.Dashboard.createRoute()) {
                        popUpTo(Screen.Onboarding.Splash.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Main.Dashboard.createRoute()) {
                        popUpTo(Screen.Onboarding.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main ────────────────────────────────────────────────────────
        composable(
            route = Screen.Main.Dashboard.route,
            arguments = listOf(navArgument("watchOnly") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val watchOnly = backStackEntry.arguments?.getBoolean("watchOnly") ?: false
            DashboardScreen(
                onNavigateToSend = { navController.navigate(Screen.Main.Send.createRoute("BTC")) },
                onNavigateToReceive = { navController.navigate(Screen.Main.Receive.createRoute("BTC")) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.Root.route) },
                onNavigateToCoinDetail = { coinId -> navController.navigate(Screen.Main.CoinDetail.createRoute(coinId)) },
                onNavigateToHWMode = { navController.navigate(Screen.HWMode.HWModeHome.route) },
                onNavigateToMarket = { navController.navigate(Screen.Market.MarketOverview.route) },
                onNavigateToPortfolio = { navController.navigate(Screen.Portfolio.route) },
                isWatchOnly = watchOnly
            )
        }
        composable(Screen.Main.WalletList.route) {
            WalletListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateWallet = { navController.navigate(Screen.Onboarding.CreateWallet.route) },
                onNavigateToImportWallet = { navController.navigate(Screen.Onboarding.ImportSeed.route) },
                onWalletSelected = { walletId -> navController.navigate(Screen.Main.Dashboard.createRoute()) }
            )
        }
        composable(
            route = Screen.Main.CoinDetail.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            CoinDetailScreen(
                coinTicker = coinId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSend = { navController.navigate(Screen.Main.Send.createRoute(coinId)) },
                onNavigateToReceive = { navController.navigate(Screen.Main.Receive.createRoute(coinId)) }
            )
        }
        composable(
            route = Screen.Main.Send.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) {
            val viewModel: com.spywhy.wallet.feature.send.viewmodel.SendViewModel = hiltViewModel()
            SendScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onScanQr = { },
                onSelectFee = { navController.navigate("send_fee") },
                onCoinControl = { navController.navigate(Screen.CoinControl.createRoute("BTC")) },
                onBatchSend = { navController.navigate(Screen.BatchSend.createRoute("BTC")) },
                onReviewTransaction = { navController.navigate("send_confirm") }
            )
        }
        composable("send_fee") {
            val parentEntry = navController.getBackStackEntry(Screen.Main.Send.route)
            val viewModel: com.spywhy.wallet.feature.send.viewmodel.SendViewModel = hiltViewModel(parentEntry)
            FeeSelectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("send_confirm") {
            val parentEntry = navController.getBackStackEntry(Screen.Main.Send.route)
            val viewModel: com.spywhy.wallet.feature.send.viewmodel.SendViewModel = hiltViewModel(parentEntry)
            ConfirmTxScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onBiometricAuth = { onSuccess -> onSuccess() },
                onDone = {
                    navController.navigate(Screen.Main.Dashboard.createRoute()) {
                        popUpTo(Screen.Main.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Main.Receive.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) {
            val viewModel: com.spywhy.wallet.feature.receive.viewmodel.ReceiveViewModel = hiltViewModel()
            ReceiveScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Main.TransactionHistory.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) {
            val viewModel: com.spywhy.wallet.feature.history.viewmodel.TransactionListViewModel = hiltViewModel()
            TransactionListScreen(
                viewModel = viewModel,
                walletAddress = "",
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { txId ->
                    navController.navigate(Screen.Main.TransactionDetail.createRoute(txId))
                }
            )
        }
        composable(
            route = Screen.Main.TransactionDetail.route,
            arguments = listOf(navArgument("txId") { type = NavType.StringType })
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("txId").orEmpty()
            PlaceholderScreen("Transaction: $txId")
        }

        // ── HW Mode ────────────────────────────────────────────────────
        composable(Screen.HWMode.HWModeHome.route) {
            val viewModel: com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel = hiltViewModel()
            HWModeScreen(
                viewModel = viewModel,
                onSignTransaction = { navController.navigate(Screen.HWMode.ScanTxQR.route) },
                onShareAddress = { },
                onPCConnection = { navController.navigate(Screen.HWMode.PCConnection.route) }
            )
        }
        composable(Screen.HWMode.ScanTxQR.route) {
            val viewModel: com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel = hiltViewModel()
            ScanTxQRScreen(
                viewModel = viewModel,
                onCancel = { navController.popBackStack() },
                onScanned = { navController.navigate(Screen.HWMode.ReviewSign.route) }
            )
        }
        composable(Screen.HWMode.ReviewSign.route) {
            val viewModel: com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel = hiltViewModel()
            ReviewSignScreen(
                viewModel = viewModel,
                onReject = { navController.popBackStack() },
                onSign = { navController.navigate(Screen.HWMode.DisplaySignedQR.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.HWMode.DisplaySignedQR.route) {
            val viewModel: com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel = hiltViewModel()
            DisplaySignedQRScreen(
                viewModel = viewModel,
                onDone = {
                    navController.navigate(Screen.HWMode.HWModeHome.route) {
                        popUpTo(Screen.HWMode.HWModeHome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.HWMode.PCConnection.route) {
            val viewModel: com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel = hiltViewModel()
            PCConnectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Market ──────────────────────────────────────────────────────
        composable(Screen.Market.MarketOverview.route) {
            val viewModel: com.spywhy.wallet.feature.market.viewmodel.MarketViewModel = hiltViewModel()
            MarketOverviewScreen(
                viewModel = viewModel,
                onCoinClick = { coinId ->
                    navController.navigate(Screen.Market.CoinChart.createRoute(coinId))
                }
            )
        }
        composable(
            route = Screen.Market.CoinChart.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId").orEmpty()
            val viewModel: com.spywhy.wallet.feature.market.viewmodel.MarketViewModel = hiltViewModel()
            CoinChartScreen(
                coinId = coinId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSetAlert = { navController.navigate(Screen.Market.PriceAlerts.route) }
            )
        }
        composable(Screen.Market.PriceAlerts.route) {
            val viewModel: com.spywhy.wallet.feature.market.viewmodel.MarketViewModel = hiltViewModel()
            PriceAlertScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Market.AIAnalysis.route) {
            val viewModel: com.spywhy.wallet.feature.market.viewmodel.AIAnalysisViewModel = hiltViewModel()
            AIAnalysisScreen(
                viewModel = viewModel,
                availableCoins = listOf("BTC", "ETH", "SOL", "LTC", "XMR"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Exchange ────────────────────────────────────────────────────
        composable(Screen.Exchange.SwapComparator.route) {
            SwapComparatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Exchange.DEXSwap.route) {
            DEXSwapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Exchange.FiatConverter.route) {
            FiatConverterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Standalone ─────────────────────────────────────────────────
        composable(Screen.Portfolio.route) {
            PortfolioScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NFTGallery.route) {
            NFTGalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Settings ───────────────────────────────────────────────────
        composable(Screen.Settings.Root.route) {
            val activity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
            SettingsScreen(
                onNavigateToSecurity = { navController.navigate(Screen.Settings.SecuritySettings.route) },
                onNavigateToNodes = { navController.navigate(Screen.Settings.NodeSettings.route) },
                onNavigateToBackup = { navController.navigate(Screen.Settings.Backup.route) },
                onNavigateToStealth = { navController.navigate(Screen.StealthWallet.route) },
                onNavigateToExportKeys = { navController.navigate(Screen.Settings.ExportPrivateKeys.route) },
                onNavigateToMessageSigning = { navController.navigate(Screen.Settings.MessageSigning.route) },
                onNavigateToDefaultCurrency = { navController.navigate(Screen.Settings.DefaultCurrency.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Settings.Notifications.route) },
                onNavigateToWidgets = { navController.navigate(Screen.Settings.Widgets.route) },
                onNavigateToPanicMode = { navController.navigate(Screen.Settings.PanicMode.route) },
                onNavigateToAbout = { navController.navigate(Screen.Settings.About.route) },
                onNavigateBack = { navController.popBackStack() },
                onCheckForUpdate = { onResult ->
                    activity?.checkForUpdateManual(onResult)
                },
                currentVersion = activity?.getCurrentVersionName() ?: "1.0.0"
            )
        }
        composable(Screen.Settings.SecuritySettings.route) {
            val act = androidx.compose.ui.platform.LocalContext.current as MainActivity
            SecuritySettingsScreen(
                settingsPreferences = act.settingsPreferences,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.NodeSettings.route) {
            val act = androidx.compose.ui.platform.LocalContext.current as MainActivity
            NodeSettingsScreen(
                settingsPreferences = act.settingsPreferences,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.ExportPrivateKeys.route) {
            ExportPrivateKeysScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.MessageSigning.route) {
            MessageSigningScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.DefaultCurrency.route) {
            val act = androidx.compose.ui.platform.LocalContext.current as MainActivity
            DefaultCurrencyScreen(
                settingsPreferences = act.settingsPreferences,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.Notifications.route) {
            val act = androidx.compose.ui.platform.LocalContext.current as MainActivity
            NotificationsScreen(
                settingsPreferences = act.settingsPreferences,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.Widgets.route) {
            val act = androidx.compose.ui.platform.LocalContext.current as MainActivity
            WidgetsScreen(
                settingsPreferences = act.settingsPreferences,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.PanicMode.route) {
            PanicModeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.About.route) {
            val activity = androidx.compose.ui.platform.LocalContext.current as? MainActivity
            AboutScreen(
                currentVersion = activity?.getCurrentVersionName() ?: "1.0.0",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Advanced ───────────────────────────────────────────────────
        composable(Screen.StealthWallet.route) {
            StealthWalletScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MempoolViewer.route) {
            val viewModel: com.spywhy.wallet.feature.history.viewmodel.TransactionListViewModel = hiltViewModel()
            MempoolViewerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { txId ->
                    navController.navigate(Screen.Main.TransactionDetail.createRoute(txId))
                }
            )
        }
        composable(
            route = Screen.CoinControl.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) {
            val viewModel: com.spywhy.wallet.feature.send.viewmodel.SendViewModel = hiltViewModel()
            CoinControlScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BatchSend.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) {
            val viewModel: com.spywhy.wallet.feature.send.viewmodel.SendViewModel = hiltViewModel()
            BatchSendScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSendAll = {
                    navController.navigate(Screen.Main.Dashboard.createRoute()) {
                        popUpTo(Screen.Main.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

// ── Biometric lock overlay ───────────────────────────────────────────────

@Composable
fun BiometricLockOverlay(onUnlockRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                tint = com.spywhy.wallet.core.util.SpyWhyColors.AccentOrange,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Wallet Locked",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Tap to unlock with fingerprint",
                style = MaterialTheme.typography.bodyMedium,
                color = com.spywhy.wallet.core.util.SpyWhyColors.TextSecondary,
            )
            OutlinedButton(
                onClick = onUnlockRequest,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = com.spywhy.wallet.core.util.SpyWhyColors.AccentOrange
                )
            ) {
                Text("Unlock")
            }
        }
    }
}

// ── Placeholder for screens requiring complex data passing ──────────────

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
