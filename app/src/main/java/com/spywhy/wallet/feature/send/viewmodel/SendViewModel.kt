package com.spywhy.wallet.feature.send.viewmodel

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.core.database.dao.TransactionDao
import com.spywhy.wallet.core.database.entity.TransactionEntity
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.UTXO
import com.spywhy.wallet.domain.repository.FeeEstimate
import com.spywhy.wallet.domain.repository.TransactionRepository
import com.spywhy.wallet.domain.usecase.SendTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class FeeLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CUSTOM("Custom")
}

enum class TxResult {
    IDLE,
    LOADING,
    SUCCESS,
    FAILURE
}

data class BatchRecipient(
    val address: String = "",
    val amount: String = ""
)

data class SendUiState(
    val selectedBlockchain: Blockchain = Blockchain.BITCOIN,
    val toAddress: String = "",
    val amount: String = "",
    val usdEquivalent: String = "",
    val feeLevel: FeeLevel = FeeLevel.MEDIUM,
    val customFee: String = "",
    val feeEstimate: FeeEstimate? = null,
    val feeEstimateDisplay: String = "",
    val selectedUtxos: List<UTXO> = emptyList(),
    val availableUtxos: List<UTXO> = emptyList(),
    val isLoading: Boolean = false,
    val txResult: TxResult = TxResult.IDLE,
    val txHash: String = "",
    val txErrorMessage: String = "",
    val addressPoisoningWarning: Boolean = false,
    val recentAddresses: List<String> = emptyList(),
    val availableBalance: BigDecimal = BigDecimal.ZERO,
    val batchRecipients: List<BatchRecipient> = listOf(BatchRecipient()),
    val currentFeeAmount: BigDecimal = BigDecimal.ZERO,
    val accountId: Long = 0L,
    val walletId: Long = 0L
)

@HiltViewModel
class SendViewModel @Inject constructor(
    private val sendTransactionUseCase: SendTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val transactionDao: TransactionDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendUiState())
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()

    init {
        loadFeeEstimates()
        loadUtxos()
    }

    fun selectBlockchain(blockchain: Blockchain) {
        _uiState.update {
            it.copy(
                selectedBlockchain = blockchain,
                toAddress = "",
                amount = "",
                usdEquivalent = "",
                feeEstimate = null,
                feeEstimateDisplay = "",
                selectedUtxos = emptyList(),
                addressPoisoningWarning = false
            )
        }
        loadFeeEstimates()
        loadUtxos()
    }

    fun updateAddress(address: String) {
        val isPoisoned = _uiState.value.recentAddresses.any { recent ->
            sendTransactionUseCase.isAddressSimilar(address, recent)
        }
        _uiState.update {
            it.copy(
                toAddress = address,
                addressPoisoningWarning = isPoisoned
            )
        }
    }

    fun pasteAddress() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pasted = clip.getItemAt(0).text?.toString().orEmpty()
            updateAddress(pasted)
        }
    }

    fun updateAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        val usd = try {
            val value = BigDecimal(filtered)
            // Placeholder conversion rate
            val rate = getUsdRate(_uiState.value.selectedBlockchain)
            value.multiply(rate).setScale(2, RoundingMode.HALF_UP).toPlainString()
        } catch (_: Exception) {
            ""
        }
        _uiState.update { it.copy(amount = filtered, usdEquivalent = usd) }
    }

    fun setMaxAmount() {
        val fee = _uiState.value.currentFeeAmount
        val max = (_uiState.value.availableBalance - fee).max(BigDecimal.ZERO)
        updateAmount(max.stripTrailingZeros().toPlainString())
    }

    fun selectFeeLevel(level: FeeLevel) {
        _uiState.update { state ->
            val fee = when (level) {
                FeeLevel.LOW -> state.feeEstimate?.low ?: BigDecimal.ZERO
                FeeLevel.MEDIUM -> state.feeEstimate?.medium ?: BigDecimal.ZERO
                FeeLevel.HIGH -> state.feeEstimate?.high ?: BigDecimal.ZERO
                FeeLevel.CUSTOM -> try {
                    BigDecimal(state.customFee)
                } catch (_: Exception) {
                    BigDecimal.ZERO
                }
            }
            val timeDisplay = when (level) {
                FeeLevel.LOW -> "Est: ${state.feeEstimate?.estimatedTimeLow ?: "~60 min"} | Fee: ${fee.toPlainString()} ${state.selectedBlockchain.ticker}"
                FeeLevel.MEDIUM -> "Est: ${state.feeEstimate?.estimatedTimeMedium ?: "~30 min"} | Fee: ${fee.toPlainString()} ${state.selectedBlockchain.ticker}"
                FeeLevel.HIGH -> "Est: ${state.feeEstimate?.estimatedTimeHigh ?: "~10 min"} | Fee: ${fee.toPlainString()} ${state.selectedBlockchain.ticker}"
                FeeLevel.CUSTOM -> "Custom fee: ${fee.toPlainString()} ${state.selectedBlockchain.ticker}"
            }
            state.copy(
                feeLevel = level,
                currentFeeAmount = fee,
                feeEstimateDisplay = timeDisplay
            )
        }
    }

    fun setCustomFee(fee: String) {
        val filtered = fee.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(customFee = filtered) }
        if (_uiState.value.feeLevel == FeeLevel.CUSTOM) {
            selectFeeLevel(FeeLevel.CUSTOM)
        }
    }

    fun toggleUtxoSelection(utxo: UTXO) {
        _uiState.update { state ->
            val current = state.selectedUtxos.toMutableList()
            if (current.any { it.id == utxo.id }) {
                current.removeAll { it.id == utxo.id }
            } else {
                current.add(utxo)
            }
            state.copy(selectedUtxos = current)
        }
    }

    fun lockUtxo(utxoId: Long, locked: Boolean) {
        viewModelScope.launch {
            transactionRepository.lockUTXO(utxoId, locked)
            loadUtxos()
        }
    }

    // Batch send
    fun addBatchRecipient() {
        _uiState.update {
            it.copy(batchRecipients = it.batchRecipients + BatchRecipient())
        }
    }

    fun removeBatchRecipient(index: Int) {
        _uiState.update {
            val list = it.batchRecipients.toMutableList()
            if (list.size > 1) list.removeAt(index)
            it.copy(batchRecipients = list)
        }
    }

    fun updateBatchRecipient(index: Int, address: String? = null, amount: String? = null) {
        _uiState.update { state ->
            val list = state.batchRecipients.toMutableList()
            if (index < list.size) {
                list[index] = list[index].copy(
                    address = address ?: list[index].address,
                    amount = amount ?: list[index].amount
                )
            }
            state.copy(batchRecipients = list)
        }
    }

    fun signAndBroadcast() {
        viewModelScope.launch {
            _uiState.update { it.copy(txResult = TxResult.LOADING) }
            try {
                val state = _uiState.value
                val amount = BigDecimal(state.amount)
                val fee = state.currentFeeAmount
                val utxos = state.selectedUtxos.ifEmpty { null }

                val signedTx = sendTransactionUseCase.createAndSign(
                    accountId = state.accountId,
                    walletId = state.walletId,
                    blockchain = state.selectedBlockchain,
                    toAddress = state.toAddress,
                    amount = amount,
                    fee = fee,
                    selectedUtxos = utxos
                )
                val txHash = sendTransactionUseCase.broadcast(state.selectedBlockchain, signedTx)

                // Persist transaction to local DB
                transactionDao.insert(
                    TransactionEntity(
                        id = txHash,
                        accountId = state.accountId,
                        txHash = txHash,
                        fromAddress = "",
                        toAddress = state.toAddress,
                        amount = amount.toPlainString(),
                        fee = fee.toPlainString(),
                        status = "PENDING",
                        timestamp = System.currentTimeMillis(),
                        blockchain = state.selectedBlockchain.ticker,
                        rawHex = signedTx
                    )
                )

                _uiState.update {
                    it.copy(txResult = TxResult.SUCCESS, txHash = txHash)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        txResult = TxResult.FAILURE,
                        txErrorMessage = e.message ?: "Transaction failed"
                    )
                }
            }
        }
    }

    fun resetTxResult() {
        _uiState.update { it.copy(txResult = TxResult.IDLE, txHash = "", txErrorMessage = "") }
    }

    private fun loadFeeEstimates() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val estimate = sendTransactionUseCase.estimateFees(
                    blockchain = state.selectedBlockchain,
                    fromAddress = "",
                    toAddress = state.toAddress,
                    amount = try { BigDecimal(state.amount) } catch (_: Exception) { BigDecimal.ZERO }
                )
                _uiState.update { it.copy(feeEstimate = estimate) }
                selectFeeLevel(_uiState.value.feeLevel)
            } catch (_: Exception) {
                // Fee estimation may fail silently
            }
        }
    }

    private fun loadUtxos() {
        viewModelScope.launch {
            val state = _uiState.value
            val blockchain = state.selectedBlockchain
            if (blockchain == Blockchain.BITCOIN || blockchain == Blockchain.LITECOIN) {
                transactionRepository.getUnspentUTXOs(state.accountId).collect { utxos ->
                    _uiState.update { it.copy(availableUtxos = utxos) }
                }
            }
        }
    }

    private fun getUsdRate(blockchain: Blockchain): BigDecimal {
        // Placeholder rates - in production these come from MarketRepository
        return when (blockchain) {
            Blockchain.BITCOIN -> BigDecimal("67000")
            Blockchain.ETHEREUM -> BigDecimal("3500")
            Blockchain.SOLANA -> BigDecimal("150")
            Blockchain.LITECOIN -> BigDecimal("85")
            Blockchain.MONERO -> BigDecimal("160")
        }
    }
}
