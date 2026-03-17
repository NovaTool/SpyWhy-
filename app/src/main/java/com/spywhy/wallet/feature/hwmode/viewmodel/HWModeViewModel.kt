package com.spywhy.wallet.feature.hwmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.usecase.HardwareWalletUseCase
import com.spywhy.wallet.domain.usecase.SignedTxPayload
import com.spywhy.wallet.domain.usecase.UnsignedTxPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ConnectionMode {
    ONLINE, OFFLINE, AIR_GAPPED
}

enum class ConnectionMethod(val displayName: String) {
    QR_RELAY("QR Relay"),
    USB("USB"),
    WIFI_DIRECT("WiFi-Direct"),
    BLE("BLE")
}

data class HWModeUiState(
    val connectionMode: ConnectionMode = ConnectionMode.AIR_GAPPED,
    val unsignedTx: UnsignedTxPayload? = null,
    val signedTx: SignedTxPayload? = null,
    val signedQRFrames: List<String> = emptyList(),
    val scanProgress: Float = 0f,
    val totalQRFrames: Int = 0,
    val scannedQRFrames: Int = 0,
    val scannedParts: MutableMap<Int, String> = mutableMapOf(),
    val isAirGapped: Boolean = true,
    val isSigning: Boolean = false,
    val signError: String? = null,
    val isPCConnected: Boolean = false,
    val selectedConnectionMethod: ConnectionMethod? = null,
    val walletId: Long = 0L
)

@HiltViewModel
class HWModeViewModel @Inject constructor(
    private val hardwareWalletUseCase: HardwareWalletUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HWModeUiState())
    val uiState: StateFlow<HWModeUiState> = _uiState.asStateFlow()

    fun setConnectionMode(mode: ConnectionMode) {
        _uiState.update {
            it.copy(
                connectionMode = mode,
                isAirGapped = mode == ConnectionMode.AIR_GAPPED
            )
        }
    }

    fun setWalletId(walletId: Long) {
        _uiState.update { it.copy(walletId = walletId) }
    }

    /**
     * Called when the QR scanner detects a frame.
     * Handles both single-frame and multi-frame (animated) QR codes.
     */
    fun onQRFrameScanned(rawData: String) {
        // Try to parse as animated QR part
        val partRegex = Regex("""^\{"part":(\d+),"total":(\d+),"data":"(.+)"\}$""")
        val match = partRegex.matchEntire(rawData)

        if (match != null) {
            // Multi-frame animated QR
            val part = match.groupValues[1].toInt()
            val total = match.groupValues[2].toInt()
            val data = match.groupValues[3]

            _uiState.update { state ->
                val parts = state.scannedParts.toMutableMap()
                parts[part] = data
                val scanned = parts.size
                val progress = scanned.toFloat() / total

                state.copy(
                    scannedParts = parts,
                    totalQRFrames = total,
                    scannedQRFrames = scanned,
                    scanProgress = progress
                )
            }

            // Check if all parts collected
            val currentState = _uiState.value
            if (currentState.scannedParts.size == currentState.totalQRFrames) {
                val assembledParts = currentState.scannedParts.entries
                    .sortedBy { it.key }
                    .map { """{"part":${it.key},"total":${currentState.totalQRFrames},"data":"${it.value}"}""" }
                val fullData = hardwareWalletUseCase.reassembleAnimatedQR(assembledParts)
                parseAndSetUnsignedTx(fullData)
            }
        } else {
            // Single-frame QR
            _uiState.update {
                it.copy(
                    scanProgress = 1f,
                    totalQRFrames = 1,
                    scannedQRFrames = 1
                )
            }
            parseAndSetUnsignedTx(rawData)
        }
    }

    private fun parseAndSetUnsignedTx(data: String) {
        val parsed = hardwareWalletUseCase.parseUnsignedTransaction(data)
        _uiState.update { it.copy(unsignedTx = parsed) }
    }

    /**
     * Signs the currently loaded unsigned transaction.
     * In production, this should be called after biometric authentication succeeds.
     */
    fun signTransaction() {
        val unsignedTx = _uiState.value.unsignedTx ?: return
        val walletId = _uiState.value.walletId

        viewModelScope.launch {
            _uiState.update { it.copy(isSigning = true, signError = null) }
            try {
                val signedPayload = hardwareWalletUseCase.signUnsignedTransaction(
                    payload = unsignedTx,
                    walletId = walletId
                )
                val encodedJson = hardwareWalletUseCase.encodeSignedTransaction(signedPayload)
                val qrFrames = hardwareWalletUseCase.splitForAnimatedQR(encodedJson)

                _uiState.update {
                    it.copy(
                        signedTx = signedPayload,
                        signedQRFrames = qrFrames,
                        isSigning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSigning = false,
                        signError = e.message ?: "Signing failed"
                    )
                }
            }
        }
    }

    fun rejectTransaction() {
        _uiState.update {
            it.copy(
                unsignedTx = null,
                signedTx = null,
                signedQRFrames = emptyList(),
                scanProgress = 0f,
                totalQRFrames = 0,
                scannedQRFrames = 0,
                scannedParts = mutableMapOf(),
                signError = null
            )
        }
    }

    fun selectConnectionMethod(method: ConnectionMethod) {
        _uiState.update { it.copy(selectedConnectionMethod = method) }
        // In production, initiate the connection handshake for the selected method
    }

    fun resetScanState() {
        _uiState.update {
            it.copy(
                scanProgress = 0f,
                totalQRFrames = 0,
                scannedQRFrames = 0,
                scannedParts = mutableMapOf()
            )
        }
    }

    fun clearSignedData() {
        _uiState.update {
            it.copy(
                unsignedTx = null,
                signedTx = null,
                signedQRFrames = emptyList(),
                scanProgress = 0f,
                totalQRFrames = 0,
                scannedQRFrames = 0,
                scannedParts = mutableMapOf(),
                signError = null
            )
        }
    }
}
