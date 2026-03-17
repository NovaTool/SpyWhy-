package com.spywhy.wallet.domain.usecase

import com.spywhy.wallet.core.crypto.MnemonicGenerator
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val mnemonicGenerator: MnemonicGenerator
) {
    suspend fun generateSeedPhrase(wordCount: Int = 12): List<String> {
        return mnemonicGenerator.generateMnemonic(wordCount)
    }

    fun validateSeedPhrase(words: List<String>): Boolean {
        return mnemonicGenerator.validateMnemonic(words)
    }

    suspend fun createWallet(
        name: String,
        seedPhrase: List<String>,
        blockchains: List<Blockchain> = Blockchain.entries
    ): Long {
        require(validateSeedPhrase(seedPhrase)) { "Invalid seed phrase" }
        return walletRepository.createWallet(name, seedPhrase, blockchains)
    }

    suspend fun importWallet(
        name: String,
        seedPhrase: List<String>,
        blockchains: List<Blockchain> = Blockchain.entries
    ): Long {
        require(validateSeedPhrase(seedPhrase)) { "Invalid seed phrase" }
        return walletRepository.importWallet(name, seedPhrase, blockchains)
    }

    suspend fun createWatchOnlyWallet(
        name: String,
        addresses: Map<Blockchain, String>
    ): Long {
        require(addresses.isNotEmpty()) { "At least one address required" }
        return walletRepository.createWatchOnlyWallet(name, addresses)
    }
}
