package com.safe.setting.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.safe.setting.app.services.accessibilityData.AccessibilityDataService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // फोन रीस्टार्ट होने पर हमारी मुख्य सर्विस को फिर से शुरू करें
            val serviceIntent = Intent(context, AccessibilityDataService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
