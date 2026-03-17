package com.spywhy.wallet.feature.nft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.NFT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFTViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(NFTState())
    val state: StateFlow<NFTState> = _state.asStateFlow()

    init {
        loadNFTs()
    }

    fun selectNFT(nft: NFT) {
        _state.update { it.copy(selectedNFT = nft) }
    }

    private fun loadNFTs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Load NFTs from database/blockchain
            _state.update { it.copy(isLoading = false) }
        }
    }
}

data class NFTState(
    val nfts: List<NFT> = emptyList(),
    val selectedNFT: NFT? = null,
    val isLoading: Boolean = false
)
