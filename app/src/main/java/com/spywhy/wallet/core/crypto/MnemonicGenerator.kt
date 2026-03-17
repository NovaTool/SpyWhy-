package com.spywhy.wallet.core.crypto

import java.security.SecureRandom
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object MnemonicGenerator {

    private var wordList: List<String> = emptyList()

    fun initialize(words: List<String>) {
        require(words.size == 2048) { "BIP39 wordlist must contain 2048 words" }
        wordList = words
    }

    fun generate(wordCount: Int = 12): String {
        require(wordList.isNotEmpty()) { "Word list not initialized" }
        require(wordCount == 12 || wordCount == 24) { "Only 12 or 24 word mnemonics supported" }

        val entropyBits = if (wordCount == 12) 128 else 256
        val entropy = ByteArray(entropyBits / 8)
        SecureRandom().nextBytes(entropy)

        return entropyToMnemonic(entropy)
    }

    fun entropyToMnemonic(entropy: ByteArray): String {
        require(wordList.isNotEmpty()) { "Word list not initialized" }
        require(entropy.size == 16 || entropy.size == 32) { "Entropy must be 16 or 32 bytes" }

        val hash = MessageDigest.getInstance("SHA-256").digest(entropy)
        val checksumBits = entropy.size / 4

        val bits = ByteArray(entropy.size * 8 + checksumBits)
        for (i in entropy.indices) {
            for (j in 0 until 8) {
                bits[i * 8 + j] = ((entropy[i].toInt() ushr (7 - j)) and 1).toByte()
            }
        }
        for (j in 0 until checksumBits) {
            bits[entropy.size * 8 + j] = ((hash[0].toInt() ushr (7 - j)) and 1).toByte()
        }

        val words = mutableListOf<String>()
        val totalBits = entropy.size * 8 + checksumBits
        for (i in 0 until totalBits / 11) {
            var index = 0
            for (j in 0 until 11) {
                index = (index shl 1) or bits[i * 11 + j].toInt()
            }
            words.add(wordList[index])
        }

        return words.joinToString(" ")
    }

    fun validate(mnemonic: String): Boolean {
        if (wordList.isEmpty()) return false

        val words = mnemonic.trim().lowercase().split("\\s+".toRegex())
        if (words.size != 12 && words.size != 24) return false

        val indices = words.map { word ->
            val idx = wordList.indexOf(word)
            if (idx < 0) return false
            idx
        }

        val totalBits = words.size * 11
        val checksumBits = words.size / 3
        val entropyBits = totalBits - checksumBits

        val bits = ByteArray(totalBits)
        for (i in indices.indices) {
            for (j in 0 until 11) {
                bits[i * 11 + j] = ((indices[i] ushr (10 - j)) and 1).toByte()
            }
        }

        val entropy = ByteArray(entropyBits / 8)
        for (i in entropy.indices) {
            var byte = 0
            for (j in 0 until 8) {
                byte = (byte shl 1) or bits[i * 8 + j].toInt()
            }
            entropy[i] = byte.toByte()
        }

        val hash = MessageDigest.getInstance("SHA-256").digest(entropy)
        for (j in 0 until checksumBits) {
            val expected = ((hash[0].toInt() ushr (7 - j)) and 1).toByte()
            if (bits[entropyBits + j] != expected) return false
        }

        return true
    }

    fun mnemonicToSeed(mnemonic: String, passphrase: String = ""): ByteArray {
        val mnemonicChars = mnemonic.toCharArray()
        val salt = "mnemonic$passphrase".toByteArray(Charsets.UTF_8)

        return try {
            val spec = PBEKeySpec(mnemonicChars, salt, 2048, 512)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            factory.generateSecret(spec).encoded
        } finally {
            SecureMemory.wipe(mnemonicChars)
        }
    }

    fun getWordList(): List<String> = wordList

    fun suggestWords(prefix: String): List<String> {
        if (prefix.isBlank() || wordList.isEmpty()) return emptyList()
        val lower = prefix.lowercase()
        return wordList.filter { it.startsWith(lower) }.take(5)
    }
}
