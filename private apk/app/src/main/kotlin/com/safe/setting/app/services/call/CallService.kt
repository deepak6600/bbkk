package com.safe.setting.app.services.call

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CallLog
import com.safe.setting.app.data.model.Call
import com.safe.setting.app.services.base.BaseService
import javax.inject.Inject

class CallService : BaseService(), InterfaceServiceCall {

    @Inject
    lateinit var interactor: InterfaceInteractorCall<InterfaceServiceCall>

    override fun onCreate() {
        super.onCreate()
        getComponent()!!.inject(this)
        interactor.onAttach(this)
    }

    override fun onDestroy() {
        interactor.onDetach()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        interactor.getCallLogs()
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    override fun getCallLogs(context: Context) {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)

                val callTypeStr = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                    CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                    CallLog.Calls.MISSED_TYPE -> "MISSED"
                    else -> "UNKNOWN"
                }

                val call = Call(
                    phoneNumber = it.getString(numberIndex),
                    contactName = it.getString(nameIndex) ?: "Unknown",
                    type = callTypeStr,
                    date = it.getLong(dateIndex),
                    duration = it.getString(durationIndex)
                )

                interactor.setCallLogs(call)
            }
        }
        stopSelf()
    }
}
