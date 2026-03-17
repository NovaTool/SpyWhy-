package com.spywhy.wallet.feature.receive.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.spywhy.wallet.domain.model.Blockchain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ReceiveUiState(
    val selectedBlockchain: Blockchain = Blockchain.BITCOIN,
    val currentAddress: String = "",
    val paymentAmount: String = "",
    val qrBitmap: Bitmap? = null,
    val addressIndex: Int = 0,
    val isGenerating: Boolean = false
)

@HiltViewModel
class ReceiveViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiveUiState())
    val uiState: StateFlow<ReceiveUiState> = _uiState.asStateFlow()

    // Placeholder addresses per blockchain - in production these come from the wallet's HD key derivation
    private val addressPool = mapOf(
        Blockchain.BITCOIN to listOf(
            "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4",
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
            "bc1q42lja79elem0anu8q860g3ua5ld6czmg7v39ml"
        ),
        Blockchain.ETHEREUM to listOf(
            "0x742d35Cc6634C0532925a3b844Bc9e7595f2bD18",
            "0x8ba1f109551bD432803012645Hc136D7eF64ABf1"
        ),
        Blockchain.SOLANA to listOf(
            "7nYhGF2DM8RYzD3a1PBvxGmFqD1sVL8Y3cN4sGr5tJwE"
        ),
        Blockchain.LITECOIN to listOf(
            "ltc1qhfj5xr7m3pqa8mrzm0cc2rj5l9mfvn97a8wsp"
        ),
        Blockchain.MONERO to listOf(
            "44AFFq5kSiGBoZ4NMDwYtN18obc8AemS33DBLWs3H7otXft3XjrpDtQGv7SqSsaBYBb98uNbr2VBBEt7f2wfn3RVGQBEP3A"
        )
    )

    init {
        loadAddress()
    }

    fun selectBlockchain(blockchain: Blockchain) {
        _uiState.update {
            it.copy(
                selectedBlockchain = blockchain,
                addressIndex = 0,
                paymentAmount = ""
            )
        }
        loadAddress()
    }

    fun setPaymentAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(paymentAmount = filtered) }
        generateQr()
    }

    fun rotateAddress() {
        val blockchain = _uiState.value.selectedBlockchain
        val pool = addressPool[blockchain] ?: return
        val nextIndex = (_uiState.value.addressIndex + 1) % pool.size
        _uiState.update { it.copy(addressIndex = nextIndex) }
        loadAddress()
    }

    private fun loadAddress() {
        val blockchain = _uiState.value.selectedBlockchain
        val pool = addressPool[blockchain] ?: return
        val index = _uiState.value.addressIndex.coerceIn(0, pool.size - 1)
        val address = pool[index]
        _uiState.update { it.copy(currentAddress = address) }
        generateQr()
    }

    private fun generateQr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val state = _uiState.value
            val qrContent = buildQrContent(
                blockchain = state.selectedBlockchain,
                address = state.currentAddress,
                amount = state.paymentAmount
            )
            val bitmap = withContext(Dispatchers.Default) {
                generateQrBitmap(qrContent, 512)
            }
            _uiState.update { it.copy(qrBitmap = bitmap, isGenerating = false) }
        }
    }

    private fun buildQrContent(
        blockchain: Blockchain,
        address: String,
        amount: String
    ): String {
        if (amount.isBlank()) return address
        return when (blockchain) {
            Blockchain.BITCOIN -> "bitcoin:$address?amount=$amount"
            Blockchain.LITECOIN -> "litecoin:$address?amount=$amount"
            Blockchain.ETHEREUM -> "ethereum:$address?value=$amount"
            Blockchain.SOLANA -> "solana:$address?amount=$amount"
            Blockchain.MONERO -> "monero:$address?tx_amount=$amount"
        }
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.WHITE else Color.BLACK)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
