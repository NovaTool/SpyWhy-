package com.spywhy.wallet.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateWalletUiState(
    val walletName: String = "",
    val wordCount: Int = 12,
    val seedPhrase: List<String> = emptyList(),
    val isGenerated: Boolean = false,
    val isVerifying: Boolean = false,
    val verificationWords: List<String> = emptyList(),
    val selectedVerificationWords: List<String> = emptyList(),
    val verificationIndices: List<Int> = emptyList(),
    val verificationError: Boolean = false,
    val isCreating: Boolean = false,
    val isCreated: Boolean = false
)

@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWalletUiState())
    val uiState: StateFlow<CreateWalletUiState> = _uiState.asStateFlow()

    fun updateWalletName(name: String) {
        _uiState.value = _uiState.value.copy(walletName = name)
    }

    fun setWordCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            wordCount = count,
            seedPhrase = emptyList(),
            isGenerated = false,
            isVerifying = false
        )
    }

    fun generateSeedPhrase() {
        // In production this uses a cryptographically secure BIP39 generator.
        // Placeholder implementation generates random words from a small sample list.
        val sampleWords = listOf(
            "abandon", "ability", "able", "about", "above", "absent",
            "absorb", "abstract", "absurd", "abuse", "access", "accident",
            "account", "accuse", "achieve", "acid", "acoustic", "acquire",
            "across", "act", "action", "actor", "actress", "actual"
        )
        val count = _uiState.value.wordCount
        val phrase = (1..count).map { sampleWords.random() }
        _uiState.value = _uiState.value.copy(
            seedPhrase = phrase,
            isGenerated = true,
            isVerifying = false,
            selectedVerificationWords = emptyList(),
            verificationError = false
        )
    }

    fun startVerification() {
        val phrase = _uiState.value.seedPhrase
        val shuffled = phrase.shuffled()
        _uiState.value = _uiState.value.copy(
            isVerifying = true,
            verificationWords = shuffled,
            selectedVerificationWords = emptyList(),
            verificationError = false
        )
    }

    fun selectVerificationWord(word: String) {
        val current = _uiState.value.selectedVerificationWords
        _uiState.value = _uiState.value.copy(
            selectedVerificationWords = current + word,
            verificationError = false
        )
    }

    fun removeLastVerificationWord() {
        val current = _uiState.value.selectedVerificationWords
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                selectedVerificationWords = current.dropLast(1),
                verificationError = false
            )
        }
    }

    fun verifyAndCreate() {
        val state = _uiState.value
        if (state.selectedVerificationWords == state.seedPhrase) {
            _uiState.value = state.copy(isCreating = true, verificationError = false)
            viewModelScope.launch {
                walletRepository.createWallet(
                    name = state.walletName.ifBlank { "My Wallet" },
                    seedPhrase = state.seedPhrase,
                    blockchains = Blockchain.entries.toList()
                )
                _uiState.value = _uiState.value.copy(isCreating = false, isCreated = true)
            }
        } else {
            _uiState.value = state.copy(verificationError = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateWalletScreen(
    onBack: () -> Unit,
    onWalletCreated: () -> Unit,
    viewModel: CreateWalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isCreated) {
        onWalletCreated()
        return
    }

    Scaffold(
        containerColor = SpyWhyColors.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Wallet",
                        color = SpyWhyColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SpyWhyColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpyWhyColors.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Wallet name input
            OutlinedTextField(
                value = state.walletName,
                onValueChange = viewModel::updateWalletName,
                label = { Text("Wallet Name", color = SpyWhyColors.TextSecondary) },
                placeholder = { Text("My Wallet", color = SpyWhyColors.TextDisabled) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SpyWhyColors.TextPrimary,
                    unfocusedTextColor = SpyWhyColors.TextPrimary,
                    cursorColor = SpyWhyColors.TextPrimary,
                    focusedBorderColor = SpyWhyColors.TextPrimary,
                    unfocusedBorderColor = SpyWhyColors.BorderGray,
                    focusedLabelColor = SpyWhyColors.TextPrimary,
                    unfocusedLabelColor = SpyWhyColors.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Word count selection
            Text(
                text = "Seed Phrase Length",
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(12, 24).forEach { count ->
                    FilterChip(
                        selected = state.wordCount == count,
                        onClick = { viewModel.setWordCount(count) },
                        label = {
                            Text(
                                "$count Words",
                                color = if (state.wordCount == count) SpyWhyColors.Black
                                else SpyWhyColors.TextPrimary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpyWhyColors.White,
                            containerColor = SpyWhyColors.MediumGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate button (only shown before generation)
            AnimatedVisibility(
                visible = !state.isGenerated,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = viewModel::generateSeedPhrase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpyWhyColors.White,
                        contentColor = SpyWhyColors.Black
                    )
                ) {
                    Text(
                        "Generate Seed Phrase",
                        fontWeight = FontWeight.SemiBold,
                        color = SpyWhyColors.Black
                    )
                }
            }

            // Seed phrase display
            AnimatedVisibility(
                visible = state.isGenerated && !state.isVerifying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Warning banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpyWhyColors.AccentOrange.copy(alpha = 0.15f))
                            .border(1.dp, SpyWhyColors.AccentOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SpyWhyColors.AccentOrange,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Write down these words in order and store them in a safe place. Never share your seed phrase. Anyone with these words can access your funds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.AccentOrange,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seed phrase grid (3 columns)
                    val words = state.seedPhrase
                    val rows = words.chunked(3)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpyWhyColors.CardBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rows.forEachIndexed { rowIndex, rowWords ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowWords.forEachIndexed { colIndex, word ->
                                    val index = rowIndex * 3 + colIndex + 1
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SpyWhyColors.MediumGray)
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$index.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SpyWhyColors.TextDisabled,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            text = word,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = SpyWhyColors.TextPrimary
                                        )
                                    }
                                    if (colIndex < rowWords.lastIndex) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                }
                                // Pad remaining space if row is not full
                                repeat(3 - rowWords.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::startVerification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentGreen,
                            contentColor = SpyWhyColors.White
                        )
                    ) {
                        Text(
                            "I've Written It Down - Verify",
                            fontWeight = FontWeight.SemiBold,
                            color = SpyWhyColors.White
                        )
                    }
                }
            }

            // Verification step
            AnimatedVisibility(
                visible = state.isVerifying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tap the words in the correct order to verify your seed phrase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected words display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpyWhyColors.CardBackground)
                            .border(
                                width = 1.dp,
                                color = if (state.verificationError) SpyWhyColors.AccentRed
                                else SpyWhyColors.BorderGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        if (state.selectedVerificationWords.isEmpty()) {
                            Text(
                                text = "Tap words below in order...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SpyWhyColors.TextDisabled,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.selectedVerificationWords.forEachIndexed { index, word ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SpyWhyColors.MediumGray)
                                            .clickable { viewModel.removeLastVerificationWord() }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. $word",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SpyWhyColors.TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.verificationError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Incorrect order. Please try again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.AccentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shuffled word pool
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.verificationWords.forEach { word ->
                            val alreadySelected = state.selectedVerificationWords.count { it == word } >=
                                    state.verificationWords.count { it == word }.coerceAtMost(
                                        state.seedPhrase.count { it == word }
                                    )
                            val isUsed = state.selectedVerificationWords.contains(word) && alreadySelected

                            OutlinedButton(
                                onClick = { viewModel.selectVerificationWord(word) },
                                enabled = !isUsed,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SpyWhyColors.TextPrimary,
                                    disabledContentColor = SpyWhyColors.TextDisabled
                                )
                            ) {
                                Text(word, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::verifyAndCreate,
                        enabled = state.selectedVerificationWords.size == state.seedPhrase.size && !state.isCreating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpyWhyColors.AccentGreen,
                            contentColor = SpyWhyColors.White,
                            disabledContainerColor = SpyWhyColors.LightGray,
                            disabledContentColor = SpyWhyColors.TextDisabled
                        )
                    ) {
                        Text(
                            text = if (state.isCreating) "Creating..." else "Verify & Create Wallet",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
