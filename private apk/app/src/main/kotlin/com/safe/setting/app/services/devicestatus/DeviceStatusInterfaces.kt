package com.safe.setting.app.services.devicestatus

import com.safe.setting.app.data.model.DeviceStatus
import com.safe.setting.app.services.base.InterfaceInteractorService
import com.safe.setting.app.services.base.InterfaceService

interface InterfaceServiceDeviceStatus : InterfaceService

interface InterfaceInteractorDeviceStatus<S : InterfaceServiceDeviceStatus> : InterfaceInteractorService<S> {
    fun sendDeviceStatus(status: DeviceStatus)
}
