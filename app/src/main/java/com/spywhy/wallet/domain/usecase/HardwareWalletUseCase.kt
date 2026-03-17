package com.spywhy.wallet.domain.usecase

import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.repository.TransactionRepository
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.security.MessageDigest
import javax.inject.Inject

class HardwareWalletUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val moshi: Moshi
) {
    fun parseUnsignedTransaction(qrData: String): UnsignedTxPayload? {
        return try {
            val adapter = moshi.adapter(UnsignedTxPayload::class.java)
            val payload = adapter.fromJson(qrData) ?: return null
            if (payload.version != QR_PROTOCOL_VERSION) return null
            if (!verifyChecksum(payload)) return null
            payload
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signUnsignedTransaction(
        payload: UnsignedTxPayload,
        walletId: Long
    ): SignedTxPayload {
        val blockchain = Blockchain.entries.find { it.ticker == payload.chain }
            ?: throw IllegalArgumentException("Unsupported chain: ${payload.chain}")

        val signedHex = transactionRepository.signTransaction(
            payload.data, walletId, blockchain
        )

        val txId = sha256Hex(signedHex)

        return SignedTxPayload(
            version = QR_PROTOCOL_VERSION,
            chain = payload.chain,
            type = "signed_tx",
            data = signedHex,
            txid = txId,
            checksum = sha256Hex("${QR_PROTOCOL_VERSION}${payload.chain}signed_tx$signedHex$txId")
        )
    }

    fun encodeSignedTransaction(payload: SignedTxPayload): String {
        val adapter = moshi.adapter(SignedTxPayload::class.java)
        return adapter.toJson(payload)
    }

    fun splitForAnimatedQR(data: String, maxChunkSize: Int = MAX_QR_BYTES): List<String> {
        if (data.length <= maxChunkSize) return listOf(data)
        val totalParts = (data.length + maxChunkSize - 1) / maxChunkSize
        return (0 until totalParts).map { i ->
            val start = i * maxChunkSize
            val end = minOf(start + maxChunkSize, data.length)
            val chunk = data.substring(start, end)
            """{"part":${i + 1},"total":$totalParts,"data":"$chunk"}"""
        }
    }

    fun reassembleAnimatedQR(parts: List<String>): String {
        @JsonClass(generateAdapter = true)
        data class QRPart(
            @Json(name = "part") val part: Int,
            @Json(name = "total") val total: Int,
            @Json(name = "data") val data: String
        )
        val adapter = moshi.adapter(QRPart::class.java)
        return parts.mapNotNull { adapter.fromJson(it) }
            .sortedBy { it.part }
            .joinToString("") { it.data }
    }

    private fun verifyChecksum(payload: UnsignedTxPayload): Boolean {
        val expected = sha256Hex(
            "${payload.version}${payload.chain}${payload.type}${payload.data}" +
            payload.outputs.joinToString("") { "${it.address}${it.amount}" } +
            payload.fee.toString()
        )
        return expected == payload.checksum
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val QR_PROTOCOL_VERSION = 1
        const val MAX_QR_BYTES = 2953
    }
}

@JsonClass(generateAdapter = true)
data class UnsignedTxPayload(
    @Json(name = "version") val version: Int,
    @Json(name = "chain") val chain: String,
    @Json(name = "type") val type: String,
    @Json(name = "data") val data: String,
    @Json(name = "outputs") val outputs: List<TxOutput>,
    @Json(name = "fee") val fee: Long,
    @Json(name = "checksum") val checksum: String
)

@JsonClass(generateAdapter = true)
data class TxOutput(
    @Json(name = "address") val address: String,
    @Json(name = "amount") val amount: Long
)

@JsonClass(generateAdapter = true)
data class SignedTxPayload(
    @Json(name = "version") val version: Int,
    @Json(name = "chain") val chain: String,
    @Json(name = "type") val type: String,
    @Json(name = "data") val data: String,
    @Json(name = "txid") val txid: String,
    @Json(name = "checksum") val checksum: String
)
