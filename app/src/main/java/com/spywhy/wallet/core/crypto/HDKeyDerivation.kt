package com.spywhy.wallet.core.crypto

import com.spywhy.wallet.core.util.Constants
import com.spywhy.wallet.domain.model.Blockchain
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HDKeyDerivation {

    private const val HARDENED_OFFSET = 0x80000000.toInt()
    private const val HMAC_ALGORITHM = "HmacSHA512"
    private const val BITCOIN_SEED_KEY = "Bitcoin seed"

    data class ExtendedKey(
        val privateKey: ByteArray,
        val chainCode: ByteArray,
        val depth: Int = 0,
        val parentFingerprint: Int = 0,
        val childIndex: Int = 0
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ExtendedKey) return false
            return privateKey.contentEquals(other.privateKey) &&
                    chainCode.contentEquals(other.chainCode) &&
                    depth == other.depth
        }

        override fun hashCode(): Int {
            var result = privateKey.contentHashCode()
            result = 31 * result + chainCode.contentHashCode()
            result = 31 * result + depth
            return result
        }

        fun wipe() {
            SecureMemory.wipe(privateKey)
            SecureMemory.wipe(chainCode)
        }
    }

    fun masterKeyFromSeed(seed: ByteArray): ExtendedKey {
        val hmac = Mac.getInstance(HMAC_ALGORITHM)
        hmac.init(SecretKeySpec(BITCOIN_SEED_KEY.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM))
        val result = hmac.doFinal(seed)
        return ExtendedKey(
            privateKey = result.copyOfRange(0, 32),
            chainCode = result.copyOfRange(32, 64),
            depth = 0
        )
    }

    fun deriveChild(parent: ExtendedKey, index: Int, hardened: Boolean = false): ExtendedKey {
        val actualIndex = if (hardened) index or HARDENED_OFFSET else index

        val data = if (hardened) {
            ByteArray(37).also { buf ->
                buf[0] = 0
                parent.privateKey.copyInto(buf, 1)
                buf[33] = (actualIndex ushr 24).toByte()
                buf[34] = (actualIndex ushr 16).toByte()
                buf[35] = (actualIndex ushr 8).toByte()
                buf[36] = actualIndex.toByte()
            }
        } else {
            val pubKey = publicKeyFromPrivate(parent.privateKey)
            ByteArray(37).also { buf ->
                pubKey.copyInto(buf, 0)
                buf[33] = (actualIndex ushr 24).toByte()
                buf[34] = (actualIndex ushr 16).toByte()
                buf[35] = (actualIndex ushr 8).toByte()
                buf[36] = actualIndex.toByte()
            }
        }

        val hmac = Mac.getInstance(HMAC_ALGORITHM)
        hmac.init(SecretKeySpec(parent.chainCode, HMAC_ALGORITHM))
        val result = hmac.doFinal(data)

        return ExtendedKey(
            privateKey = result.copyOfRange(0, 32),
            chainCode = result.copyOfRange(32, 64),
            depth = parent.depth + 1,
            childIndex = actualIndex
        )
    }

    /**
     * Derive BIP44 path: m/44'/{coinType}'/0'/0/{index}
     */
    fun deriveBIP44(
        masterKey: ExtendedKey,
        blockchain: Blockchain,
        accountIndex: Int = 0,
        addressIndex: Int = 0,
        isChange: Boolean = false
    ): ExtendedKey {
        val purpose = deriveChild(masterKey, Constants.BIP44_PURPOSE, hardened = true)
        val coinType = deriveChild(purpose, blockchain.coinType, hardened = true)
        val account = deriveChild(coinType, accountIndex, hardened = true)
        val change = deriveChild(account, if (isChange) 1 else 0, hardened = false)
        val address = deriveChild(change, addressIndex, hardened = false)

        purpose.wipe()
        coinType.wipe()
        account.wipe()
        change.wipe()

        return address
    }

    fun getDerivationPath(
        blockchain: Blockchain,
        accountIndex: Int = 0,
        addressIndex: Int = 0,
        isChange: Boolean = false
    ): String {
        return "m/44'/${blockchain.coinType}'/$accountIndex'/${if (isChange) 1 else 0}/$addressIndex"
    }

    /**
     * Placeholder for EC public key derivation from private key.
     * In production, use bitcoinj or Bouncy Castle for secp256k1 point multiplication.
     */
    private fun publicKeyFromPrivate(privateKey: ByteArray): ByteArray {
        // Compressed public key placeholder (33 bytes)
        // Real implementation would use secp256k1 curve multiplication
        val hash = javax.crypto.Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(privateKey, "HmacSHA256"))
            doFinal(privateKey)
        }
        return ByteArray(33).also { buf ->
            buf[0] = 0x02
            hash.copyInto(buf, 1, 0, 32)
        }
    }
}
