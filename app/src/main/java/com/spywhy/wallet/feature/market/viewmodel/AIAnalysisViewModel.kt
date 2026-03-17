package com.spywhy.wallet.feature.market.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.usecase.AIAnalysisUseCase
import com.spywhy.wallet.domain.usecase.TechnicalAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIAnalysisUiState(
    val selectedCoin: String = "",
    val analysis: TechnicalAnalysis? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AIAnalysisViewModel @Inject constructor(
    private val aiAnalysisUseCase: AIAnalysisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAnalysisUiState())
    val uiState: StateFlow<AIAnalysisUiState> = _uiState.asStateFlow()

    fun selectCoin(coinId: String) {
        _uiState.update { it.copy(selectedCoin = coinId, analysis = null, error = null) }
        analyze(coinId)
    }

    fun refreshAnalysis() {
        val coinId = _uiState.value.selectedCoin
        if (coinId.isNotEmpty()) {
            analyze(coinId)
        }
    }

    private fun analyze(coinId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            try {
                val result = aiAnalysisUseCase.analyzeWithIndicators(coinId)
                _uiState.update {
                    it.copy(
                        analysis = result,
                        isAnalyzing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = e.message ?: "Analysis failed"
                    )
                }
            }
        }
    }
}
