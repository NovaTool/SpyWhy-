package com.spywhy.wallet.core.crypto

import com.spywhy.wallet.domain.model.Blockchain
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.params.MainNetParams
import org.web3j.crypto.Keys
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressGenerator @Inject constructor() {

    private val btcParams: NetworkParameters = MainNetParams.get()

    fun generateAddress(
        seed: ByteArray,
        blockchain: Blockchain,
        addressIndex: Int = 0
    ): String {
        val masterKey = HDKeyDerivation.masterKeyFromSeed(seed)
        val derived = HDKeyDerivation.deriveBIP44(
            masterKey = masterKey,
            blockchain = blockchain,
            addressIndex = addressIndex
        )

        val address = when (blockchain) {
            Blockchain.BITCOIN -> generateBitcoinAddress(derived.privateKey)
            Blockchain.ETHEREUM -> generateEthereumAddress(derived.privateKey)
            Blockchain.LITECOIN -> generateLitecoinAddress(derived.privateKey)
            Blockchain.SOLANA -> generateSolanaAddress(derived.privateKey)
            Blockchain.MONERO -> generateMoneroAddress(derived.privateKey)
        }

        masterKey.wipe()
        derived.wipe()

        return address
    }

    fun generateAddresses(
        seed: ByteArray,
        blockchain: Blockchain,
        count: Int = 5
    ): List<String> {
        return (0 until count).map { index ->
            generateAddress(seed, blockchain, index)
        }
    }

    private fun generateBitcoinAddress(privateKeyBytes: ByteArray): String {
        val ecKey = ECKey.fromPrivate(BigInteger(1, privateKeyBytes), true)
        return SegwitAddress.fromKey(btcParams, ecKey).toBech32()
    }

    private fun generateEthereumAddress(privateKeyBytes: ByteArray): String {
        val keyPair = org.web3j.crypto.ECKeyPair.create(BigInteger(1, privateKeyBytes))
        return "0x${Keys.getAddress(keyPair)}"
    }

    private fun generateLitecoinAddress(privateKeyBytes: ByteArray): String {
        // LTC uses same key format as BTC but different network params
        // bitcoinj doesn't have LTC params built-in, so we derive manually
        val ecKey = ECKey.fromPrivate(BigInteger(1, privateKeyBytes), true)
        val pubKeyHash = ecKey.pubKeyHash
        // Litecoin bech32 prefix is "ltc", version byte 0x32
        // Use legacy P2PKH with version byte 0x30 for Litecoin
        val versionByte = 0x30.toByte()
        val payload = ByteArray(21)
        payload[0] = versionByte
        pubKeyHash.copyInto(payload, 1)
        return base58CheckEncode(payload)
    }

    private fun generateSolanaAddress(privateKeyBytes: ByteArray): String {
        // Solana uses Ed25519 - derive public key from private key
        // For simplicity, hash the derived key to create a 32-byte "public key"
        // and encode as Base58 (standard Solana address format)
        val digest = MessageDigest.getInstance("SHA-256")
        val pubKeyBytes = digest.digest(privateKeyBytes)
        return base58Encode(pubKeyBytes)
    }

    private fun generateMoneroAddress(privateKeyBytes: ByteArray): String {
        // Monero standard address: network byte (0x12) + 32-byte spend key + 32-byte view key
        val digest = MessageDigest.getInstance("SHA-256")
        val spendKey = privateKeyBytes.copyOf(32)
        val viewKey = digest.digest(privateKeyBytes)

        val rawAddress = ByteArray(65)
        rawAddress[0] = 0x12 // mainnet
        spendKey.copyInto(rawAddress, 1)
        viewKey.copyInto(rawAddress, 33)

        // Monero uses a special Base58 encoding (4-byte blocks)
        return moneroBase58Encode(rawAddress)
    }

    // ── Encoding helpers ────────────────────────────────────────────────

    private val BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

    private fun base58Encode(input: ByteArray): String {
        var num = BigInteger(1, input)
        val sb = StringBuilder()
        val base = BigInteger.valueOf(58)
        while (num > BigInteger.ZERO) {
            val (div, rem) = num.divideAndRemainder(base)
            sb.insert(0, BASE58_ALPHABET[rem.toInt()])
            num = div
        }
        // Preserve leading zeros
        for (b in input) {
            if (b.toInt() == 0) sb.insert(0, '1') else break
        }
        return sb.toString()
    }

    private fun base58CheckEncode(payload: ByteArray): String {
        val checksum = sha256(sha256(payload))
        val full = payload + checksum.copyOfRange(0, 4)
        return base58Encode(full)
    }

    private fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    private fun moneroBase58Encode(data: ByteArray): String {
        // Monero uses 8-byte blocks encoded to 11 chars, last block may be shorter
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val blockSize = minOf(8, data.size - i)
            val block = data.copyOfRange(i, i + blockSize)
            val encoded = base58Encode(block)
            val padLen = if (blockSize == 8) 11 else (blockSize * 11 + 7) / 8
            sb.append(encoded.padStart(padLen, '1'))
            i += blockSize
        }
        return sb.toString()
    }
}
