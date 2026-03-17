package com.spywhy.wallet.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.TransactionStatus
import com.spywhy.wallet.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TxFilter(val label: String) {
    ALL("All"),
    SENT("Sent"),
    RECEIVED("Received"),
    PENDING("Pending")
}

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val filter: TxFilter = TxFilter.ALL,
    val isRefreshing: Boolean = false,
    val walletAddress: String = "",
    val accountId: Long = 0L,
    val blockchain: Blockchain = Blockchain.BITCOIN,
    val pendingTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
        observePendingTransactions()
    }

    fun initialize(accountId: Long, blockchain: Blockchain, walletAddress: String) {
        _uiState.update {
            it.copy(
                accountId = accountId,
                blockchain = blockchain,
                walletAddress = walletAddress
            )
        }
        observeTransactions()
        refresh()
    }

    fun setFilter(filter: TxFilter) {
        _uiState.update { it.copy(filter = filter) }
        applyFilter()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                val state = _uiState.value
                transactionRepository.refreshTransactions(
                    accountId = state.accountId,
                    blockchain = state.blockchain,
                    address = state.walletAddress
                )
            } catch (_: Exception) {
                // Refresh may fail silently
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getTransactionsByAccount(_uiState.value.accountId)
                .catch { /* ignore errors */ }
                .collect { txList ->
                    _uiState.update { it.copy(transactions = txList) }
                    applyFilter()
                }
        }
    }

    private fun observePendingTransactions() {
        viewModelScope.launch {
            transactionRepository.getPendingTransactions()
                .catch { /* ignore errors */ }
                .collect { pending ->
                    _uiState.update { it.copy(pendingTransactions = pending) }
                }
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val filtered = when (state.filter) {
            TxFilter.ALL -> state.transactions
            TxFilter.SENT -> state.transactions.filter {
                it.fromAddress.equals(state.walletAddress, ignoreCase = true)
            }
            TxFilter.RECEIVED -> state.transactions.filter {
                it.toAddress.equals(state.walletAddress, ignoreCase = true)
            }
            TxFilter.PENDING -> state.transactions.filter {
                it.status == TransactionStatus.PENDING
            }
        }
        _uiState.update { it.copy(filteredTransactions = filtered) }
    }
}
