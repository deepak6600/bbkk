package com.safe.setting.app.utils.checkForegroundApp

import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

import com.safe.setting.app.utils.checkForegroundApp.CheckPermission.hasUsageStatsPermission



class CheckForegroundApp : CheckDetector {

    override fun getForegroundPostLollipop(context: Context): String? {

        if (!context.hasUsageStatsPermission()) return null
        var foregroundApp: String? = null
        val mUsageStatsManager = context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time)
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) { // Use the replacement here
                foregroundApp = event.packageName
            }
        }

        return foregroundApp
    }

override fun getForegroundPreLollipop(context: Context): String? {
    val am = context.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
    val runningAppProcesses = am.runningAppProcesses
    if (runningAppProcesses.isNotEmpty()) {
        val foregroundProcess = runningAppProcesses[0]
        val foregroundTaskPackageName = foregroundProcess?.processName
        val pm = context.packageManager
        val foregroundAppPackageInfo: PackageInfo?
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName!!, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
        return foregroundAppPackageInfo?.applicationInfo?.packageName
    }
    return null
}

}