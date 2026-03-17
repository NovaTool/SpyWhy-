package com.spywhy.wallet.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.security.MessageDigest

object AntiTamper {

    fun verifySignature(context: Context): Boolean {
        return try {
            val signingInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signatures = packageInfo.signingInfo
                if (signatures.hasMultipleSigners()) {
                    signatures.apkContentsSigners
                } else {
                    signatures.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signingInfo.isNullOrEmpty()) return false

            // Compute SHA-256 of the first signing certificate
            val md = MessageDigest.getInstance("SHA-256")
            val certHash = md.digest(signingInfo[0].toByteArray())
            val certHex = certHash.joinToString("") { "%02x".format(it) }

            // In a production build, compare against the known release signing certificate hash.
            // For now, we simply verify that a valid certificate exists.
            // TODO: Replace with actual release certificate hash
            // val RELEASE_CERT_HASH = "your_release_cert_sha256_hex_here"
            // return certHex == RELEASE_CERT_HASH

            certHex.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun checkDebugger(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    fun isRunningInTestHarness(): Boolean {
        return try {
            // Check ActivityManager.isRunningInTestHarness
            val amClass = Class.forName("android.app.ActivityManager")
            val method = amClass.getMethod("isRunningInUserTestHarness")
            method.invoke(null) as? Boolean ?: false
        } catch (_: Exception) {
            try {
                // Fallback: check system property
                val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.test_harness"))
                val result = process.inputStream.bufferedReader().readLine()?.trim()
                process.waitFor()
                result == "1" || result == "true"
            } catch (_: Exception) {
                false
            }
        }
    }

    fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun performAllChecks(context: Context): TamperCheckResult {
        return TamperCheckResult(
            signatureValid = verifySignature(context),
            debuggerAttached = checkDebugger(),
            runningInTestHarness = isRunningInTestHarness(),
            isDebuggable = isDebuggable(context)
        )
    }

    data class TamperCheckResult(
        val signatureValid: Boolean,
        val debuggerAttached: Boolean,
        val runningInTestHarness: Boolean,
        val isDebuggable: Boolean
    ) {
        val isCompromised: Boolean
            get() = !signatureValid || debuggerAttached || runningInTestHarness || isDebuggable
    }
}
