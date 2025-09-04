package com.safe.setting.app.services.devicestatus

import android.content.Context
import com.safe.setting.app.data.model.DeviceStatus
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.services.base.BaseInteractorService
import com.safe.setting.app.utils.Consts
import javax.inject.Inject

class InteractorDeviceStatus<S : InterfaceServiceDeviceStatus> @Inject constructor(
    private val firebase: InterfaceFirebase,
    context: Context
) : BaseInteractorService<S>(context, firebase), InterfaceInteractorDeviceStatus<S> {

    override fun sendDeviceStatus(status: DeviceStatus) {
        firebase.getDatabaseReference(Consts.DEVICE_STATUS).setValue(status)
    }
}
