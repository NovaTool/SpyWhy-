package com.spywhy.wallet.core.crypto

import java.util.Arrays

object SecureMemory {

    fun wipe(array: ByteArray) {
        Arrays.fill(array, 0.toByte())
    }

    fun wipe(array: CharArray) {
        Arrays.fill(array, '\u0000')
    }

    fun wipeString(str: String) {
        try {
            val field = String::class.java.getDeclaredField("value")
            field.isAccessible = true
            val chars = field.get(str)
            when (chars) {
                is ByteArray -> Arrays.fill(chars, 0.toByte())
                is CharArray -> Arrays.fill(chars, '\u0000')
            }
        } catch (_: Exception) {
            // Best effort - not all JVM implementations expose internal char array
        }
    }

    inline fun <T> withSecureBytes(size: Int, block: (ByteArray) -> T): T {
        val bytes = ByteArray(size)
        return try {
            block(bytes)
        } finally {
            wipe(bytes)
        }
    }

    inline fun <T> withSecureChars(size: Int, block: (CharArray) -> T): T {
        val chars = CharArray(size)
        return try {
            block(chars)
        } finally {
            wipe(chars)
        }
    }
}
