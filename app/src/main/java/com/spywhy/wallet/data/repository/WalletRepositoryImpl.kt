package com.spywhy.wallet.data.repository

import com.spywhy.wallet.core.crypto.AddressGenerator
import com.spywhy.wallet.core.database.dao.AccountDao
import com.spywhy.wallet.core.database.dao.ActiveAddressDao
import com.spywhy.wallet.core.database.dao.WalletDao
import com.spywhy.wallet.core.database.entity.AccountEntity
import com.spywhy.wallet.core.database.entity.ActiveAddressEntity
import com.spywhy.wallet.core.database.entity.WalletEntity
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Wallet
import com.spywhy.wallet.domain.model.WalletType
import com.spywhy.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val accountDao: AccountDao,
    private val activeAddressDao: ActiveAddressDao,
    private val addressGenerator: AddressGenerator,
    private val encryptedPrefs: android.content.SharedPreferences
) : WalletRepository {

    override fun getAllWallets(): Flow<List<Wallet>> {
        return walletDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWalletById(id: Long): Flow<Wallet?> {
        return walletDao.getAll().map { entities ->
            entities.find { it.id == id }?.toDomain()
        }
    }

    override fun getHiddenWallets(): Flow<List<Wallet>> {
        return walletDao.getHidden().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createWallet(
        name: String,
        seedPhrase: List<String>,
        blockchains: List<Blockchain>
    ): Long {
        val entity = WalletEntity(
            name = name,
            type = WalletType.HD.name,
            blockchains = blockchains.joinToString(",") { it.ticker }
        )
        val walletId = walletDao.insert(entity)

        // Store encrypted seed phrase
        encryptedPrefs.edit()
            .putString("seed_$walletId", seedPhrase.joinToString(" "))
            .apply()

        // Generate initial addresses for each blockchain
        val seed = deriveSeed(seedPhrase)
        blockchains.forEach { blockchain ->
            try {
                val address = addressGenerator.generateAddress(seed, blockchain, 0)

                // Save as account
                accountDao.insert(
                    AccountEntity(
                        walletId = walletId,
                        blockchain = blockchain.ticker,
                        address = address,
                        derivationIndex = 0
                    )
                )

                // Save as active address
                activeAddressDao.insert(
                    ActiveAddressEntity(
                        walletId = walletId,
                        blockchain = blockchain.ticker,
                        address = address,
                        derivationIndex = 0
                    )
                )
            } catch (_: Exception) {
                // Skip chains that fail address generation
            }
        }

        return walletId
    }

    override suspend fun importWallet(
        name: String,
        seedPhrase: List<String>,
        blockchains: List<Blockchain>
    ): Long {
        return createWallet(name, seedPhrase, blockchains)
    }

    override suspend fun createWatchOnlyWallet(
        name: String,
        addresses: Map<Blockchain, String>
    ): Long {
        val blockchains = addresses.keys.toList()
        val entity = WalletEntity(
            name = name,
            type = WalletType.WATCH_ONLY.name,
            blockchains = blockchains.joinToString(",") { it.ticker }
        )
        val walletId = walletDao.insert(entity)

        addresses.forEach { (blockchain, address) ->
            accountDao.insert(
                AccountEntity(
                    walletId = walletId,
                    blockchain = blockchain.ticker,
                    address = address,
                    derivationIndex = 0
                )
            )
            activeAddressDao.insert(
                ActiveAddressEntity(
                    walletId = walletId,
                    blockchain = blockchain.ticker,
                    address = address,
                    derivationIndex = 0
                )
            )
        }

        return walletId
    }

    override suspend fun deleteWallet(walletId: Long) {
        val entity = walletDao.getById(walletId) ?: return
        walletDao.delete(entity)
        encryptedPrefs.edit().remove("seed_$walletId").apply()
    }

    override suspend fun renameWallet(walletId: Long, newName: String) {
        val entity = walletDao.getById(walletId) ?: return
        walletDao.update(entity.copy(name = newName))
    }

    override suspend fun toggleHidden(walletId: Long, isHidden: Boolean) {
        val entity = walletDao.getById(walletId) ?: return
        walletDao.update(entity.copy(isHidden = isHidden))
    }

    override suspend fun getDecryptedSeedPhrase(walletId: Long): List<String> {
        val stored = encryptedPrefs.getString("seed_$walletId", null) ?: return emptyList()
        return stored.split(" ")
    }

    override suspend fun exportEncryptedBackup(walletId: Long): ByteArray {
        val seed = encryptedPrefs.getString("seed_$walletId", null) ?: return byteArrayOf()
        return seed.toByteArray(Charsets.UTF_8)
    }

    override suspend fun importEncryptedBackup(data: ByteArray, password: String): Long {
        val seedPhrase = String(data, Charsets.UTF_8).split(" ")
        return createWallet("Restored Wallet", seedPhrase, Blockchain.entries.toList())
    }

    private fun deriveSeed(seedPhrase: List<String>): ByteArray {
        val passphrase = seedPhrase.joinToString(" ")
        val spec = javax.crypto.spec.PBEKeySpec(
            passphrase.toCharArray(),
            "mnemonic".toByteArray(),
            2048,
            512
        )
        return javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            .generateSecret(spec).encoded
    }

    private fun WalletEntity.toDomain(): Wallet {
        val blockchainList = blockchains.split(",").mapNotNull { Blockchain.fromTicker(it.trim()) }
        return Wallet(
            id = id,
            name = name,
            type = try { WalletType.valueOf(type) } catch (_: Exception) { WalletType.HD },
            blockchains = blockchainList,
            isHidden = isHidden,
            createdAt = createdAt
        )
    }
}
