package com.spywhy.wallet.core.security

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

object ScreenshotProtection {

    private const val CLIPBOARD_CLEAR_DELAY_MS = 60_000L // 1 minute

    fun apply(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun remove(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun clearClipboard(context: Context, delayMs: Long = CLIPBOARD_CLEAR_DELAY_MS) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }, delayMs)
    }

    fun clearClipboardImmediately(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    }
}
