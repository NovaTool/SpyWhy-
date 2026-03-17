package com.spywhy.wallet.core.crypto

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AESEncryptor @Inject constructor(
    private val keystoreManager: KeystoreManager
) {

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128 // bits
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = keystoreManager.getMasterKey()
            ?: throw IllegalStateException("Master key not available. Generate it first.")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv // GCM generates a random IV automatically
        val ciphertext = cipher.doFinal(plaintext)

        // Prepend IV to ciphertext: [IV (12 bytes)] [ciphertext + GCM tag]
        return iv + ciphertext
    }

    fun decrypt(ciphertext: ByteArray): ByteArray {
        require(ciphertext.size > GCM_IV_LENGTH) {
            "Ciphertext too short to contain IV"
        }

        val key = keystoreManager.getMasterKey()
            ?: throw IllegalStateException("Master key not available.")

        val iv = ciphertext.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedData = ciphertext.copyOfRange(GCM_IV_LENGTH, ciphertext.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(encryptedData)
    }

    fun encryptString(text: String): String {
        val encrypted = encrypt(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptString(encrypted: String): String {
        val ciphertext = Base64.decode(encrypted, Base64.NO_WRAP)
        val decrypted = decrypt(ciphertext)
        return String(decrypted, Charsets.UTF_8)
    }
}
