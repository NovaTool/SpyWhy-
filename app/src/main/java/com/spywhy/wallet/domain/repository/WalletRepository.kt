package com.spywhy.wallet.domain.repository

import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getAllWallets(): Flow<List<Wallet>>
    fun getWalletById(id: Long): Flow<Wallet?>
    fun getHiddenWallets(): Flow<List<Wallet>>
    suspend fun createWallet(name: String, seedPhrase: List<String>, blockchains: List<Blockchain>): Long
    suspend fun importWallet(name: String, seedPhrase: List<String>, blockchains: List<Blockchain>): Long
    suspend fun createWatchOnlyWallet(name: String, addresses: Map<Blockchain, String>): Long
    suspend fun deleteWallet(walletId: Long)
    suspend fun renameWallet(walletId: Long, newName: String)
    suspend fun toggleHidden(walletId: Long, isHidden: Boolean)
    suspend fun getDecryptedSeedPhrase(walletId: Long): List<String>
    suspend fun exportEncryptedBackup(walletId: Long): ByteArray
    suspend fun importEncryptedBackup(data: ByteArray, password: String): Long
}
