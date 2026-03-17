package com.spywhy.wallet.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootDetector {

    private val KNOWN_ROOT_APPS = arrayOf(
        "com.topjohnwu.magisk",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.koushikdutta.superuser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
        "com.formyhm.hideroot",
        "me.phh.superuser",
        "com.kingouser.com",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.amphoras.hidemyroot",
        "com.amphoras.hidemyrootadfree"
    )

    private val KNOWN_ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/su/bin/su",
        "/su/bin",
        "/system/xbin/daemonsu",
        "/system/etc/init.d/99teleroot",
        "/system/app/SuperSU.apk",
        "/system/app/SuperSU",
        "/system/app/Magisk.apk",
        "/data/adb/magisk",
        "/sbin/.magisk",
        "/cache/.disable_magisk",
        "/dev/.magisk.unblock"
    )

    private val KNOWN_DANGEROUS_PROPS = mapOf(
        "ro.debuggable" to "1",
        "ro.secure" to "0",
        "service.adb.root" to "1"
    )

    fun isRooted(): Boolean {
        return checkRootBinaries() ||
                checkRootPaths() ||
                checkDangerousProperties() ||
                checkBusybox() ||
                checkSuCommand() ||
                checkMagisk()
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("emulator") ||
                Build.PRODUCT.contains("simulator") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.BOARD.contains("unknown") ||
                Build.HOST.contains("ci-") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT ||
                Build.PRODUCT.contains("vbox86p") ||
                Build.DEVICE.contains("vbox86p") ||
                Build.HARDWARE.lowercase().contains("nox") ||
                Build.PRODUCT.lowercase().contains("nox") ||
                Build.BOARD.lowercase().contains("nox") ||
                Build.SERIAL.contains("unknown") ||
                Build.SERIAL.contains("android"))
    }

    fun detectTampering(context: Context): Boolean {
        return !verifyInstaller(context)
    }

    private fun checkRootBinaries(): Boolean {
        val paths = System.getenv("PATH")?.split(":") ?: return false
        for (path in paths) {
            val suFile = File(path, "su")
            if (suFile.exists() && suFile.canExecute()) {
                return true
            }
        }
        return false
    }

    private fun checkRootPaths(): Boolean {
        return KNOWN_ROOT_PATHS.any { path ->
            File(path).exists()
        }
    }

    private fun checkDangerousProperties(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()

            KNOWN_DANGEROUS_PROPS.any { (key, value) ->
                output.contains("[$key]: [$value]")
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun checkBusybox(): Boolean {
        val busyboxPaths = arrayOf(
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/sbin/busybox",
            "/data/local/xbin/busybox",
            "/data/local/bin/busybox"
        )
        return busyboxPaths.any { File(it).exists() }
    }

    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            !result.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    private fun checkMagisk(): Boolean {
        val magiskPaths = arrayOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/cache/.disable_magisk"
        )
        return magiskPaths.any { File(it).exists() }
    }

    private fun verifyInstaller(context: Context): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            val validInstallers = listOf(
                "com.android.vending",       // Google Play Store
                "com.google.android.feedback" // Google Play Store (alternative)
            )
            installer != null && installer in validInstallers
        } catch (_: Exception) {
            false
        }
    }

    fun checkRootApps(context: Context): Boolean {
        val pm = context.packageManager
        return KNOWN_ROOT_APPS.any { packageName ->
            try {
                pm.getPackageInfo(packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}
