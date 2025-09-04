package com.safe.setting.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class HomeWatcher(private val mContext: Context, private val listener: OnHomePressedListener) {

    interface OnHomePressedListener {
        fun onHomePressed()
        fun onRecentApps()
    }

    private var mReceiver: InnerReceiver? = null

    init {
        mReceiver = InnerReceiver()
    }

    fun startWatch() {
        if (mReceiver != null) {
            // Use ContextCompat.registerReceiver with the appropriate flag
            ContextCompat.registerReceiver(
                mContext, mReceiver, IntentFilter("com.safe.setting.CUSTOM_ACTION"),
                // Choose the flag based on your requirements:
                ContextCompat.RECEIVER_NOT_EXPORTED  // If you don't expect broadcasts from other apps

            )
        }
    }

    fun stopWatch() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver)
        }
    }

    internal inner class InnerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Handle relevant intents, but not ACTION_CLOSE_SYSTEM_DIALOGS
            // Example: Handle "com.home.demo15.CUSTOM_ACTION" or other intents
        }
    }
}