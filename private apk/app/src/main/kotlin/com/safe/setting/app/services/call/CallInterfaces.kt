package com.safe.setting.app.services.call

import android.content.Context
import com.safe.setting.app.data.model.Call
import com.safe.setting.app.services.base.InterfaceInteractorService
import com.safe.setting.app.services.base.InterfaceService

interface InterfaceServiceCall : InterfaceService {
    fun getCallLogs(context: Context)
}

interface InterfaceInteractorCall<S : InterfaceServiceCall> : InterfaceInteractorService<S> {
    fun getCallLogs()
    fun setCallLogs(call: Call)
}
