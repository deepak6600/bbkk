package com.safe.setting.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import com.safe.setting.app.services.call.CallService

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            // आउटगोइंग कॉल को हम STATE_IDLE होने पर संभालेंगे
        } else {
            val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            val state = when (stateStr) {
                TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                else -> TelephonyManager.CALL_STATE_IDLE
            }
            onCallStateChanged(context, state)
        }
    }

    private fun onCallStateChanged(context: Context, state: Int) {
        if (lastState == state) {
            return
        }
        // शर्त को सरल बनाया गया है
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            // कॉल समाप्त हो गई है
            Handler(Looper.getMainLooper()).postDelayed({
                val callServiceIntent = Intent(context, CallService::class.java)
                context.startService(callServiceIntent)
            }, 1500) // 1.5 सेकंड की देरी ताकि कॉल लॉग सिस्टम में सेव हो जाए
        }
        lastState = state
    }
}