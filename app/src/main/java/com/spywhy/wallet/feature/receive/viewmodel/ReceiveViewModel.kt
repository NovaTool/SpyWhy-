package com.spywhy.wallet.feature.receive.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.spywhy.wallet.core.crypto.AddressGenerator
import com.spywhy.wallet.domain.model.Blockchain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
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
class ReceiveViewModel @Inject constructor(
    private val addressGenerator: AddressGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiveUiState())
    val uiState: StateFlow<ReceiveUiState> = _uiState.asStateFlow()

    private val addressCache = mutableMapOf<Blockchain, MutableList<String>>()
    private var seed: ByteArray? = null

    init {
        generateSeedAndLoadAddresses()
    }

    private fun generateSeedAndLoadAddresses() {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isGenerating = true) }

            // Generate a deterministic seed from random entropy
            // In production this comes from the wallet's stored encrypted seed
            val entropy = ByteArray(32)
            SecureRandom().nextBytes(entropy)
            // Use PBKDF2 to stretch entropy into a proper seed
            val spec = javax.crypto.spec.PBEKeySpec(
                String(entropy.map { (it.toInt() and 0xFF).toChar() }.toCharArray()).toCharArray(),
                "spywhy-wallet".toByteArray(),
                2048,
                512
            )
            seed = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                .generateSecret(spec).encoded

            // Pre-generate addresses for all blockchains
            Blockchain.entries.forEach { blockchain ->
                try {
                    val addresses = addressGenerator.generateAddresses(
                        seed = seed!!,
                        blockchain = blockchain,
                        count = 5
                    )
                    addressCache[blockchain] = addresses.toMutableList()
                } catch (e: Exception) {
                    // Fallback if address generation fails for a chain
                    addressCache[blockchain] = mutableListOf("Address generation error")
                }
            }

            withContext(Dispatchers.Main) {
                loadAddress()
            }
        }
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
        val pool = addressCache[blockchain] ?: return
        val nextIndex = (_uiState.value.addressIndex + 1) % pool.size

        // Generate more if needed
        if (nextIndex >= pool.size - 1 && seed != null) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val newAddress = addressGenerator.generateAddress(
                        seed = seed!!,
                        blockchain = blockchain,
                        addressIndex = pool.size
                    )
                    pool.add(newAddress)
                } catch (_: Exception) { }
            }
        }

        _uiState.update { it.copy(addressIndex = nextIndex) }
        loadAddress()
    }

    private fun loadAddress() {
        val blockchain = _uiState.value.selectedBlockchain
        val pool = addressCache[blockchain]
        if (pool.isNullOrEmpty()) {
            _uiState.update { it.copy(currentAddress = "Generating...", isGenerating = true) }
            return
        }
        val index = _uiState.value.addressIndex.coerceIn(0, pool.size - 1)
        _uiState.update { it.copy(currentAddress = pool[index], isGenerating = false) }
        generateQr()
    }

    private fun generateQr() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.currentAddress.isBlank() || state.currentAddress == "Generating...") return@launch
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

    override fun onCleared() {
        super.onCleared()
        seed?.let { com.spywhy.wallet.core.crypto.SecureMemory.wipe(it) }
    }
}
