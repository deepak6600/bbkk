package com.safe.setting.app.di.component

import com.safe.setting.app.di.PerService
import com.safe.setting.app.di.module.ServiceModule
import com.safe.setting.app.services.call.CallService
import com.safe.setting.app.services.devicestatus.DeviceStatusService
import com.safe.setting.app.services.sms.SmsService
import dagger.Component


@PerService
@Component(dependencies = [AppComponent::class], modules = [ServiceModule::class])
interface ServiceComponent {


    fun inject(smsService: SmsService)
    fun inject(service: CallService)

    fun inject(service: DeviceStatusService)


}