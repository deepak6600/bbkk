package com.safe.setting.app.di.module

import android.app.Service
import android.content.Context
import com.safe.setting.app.di.PerService
import com.safe.setting.app.services.call.InteractorCall
import com.safe.setting.app.services.call.InterfaceInteractorCall
import com.safe.setting.app.services.call.InterfaceServiceCall
import com.safe.setting.app.services.devicestatus.InteractorDeviceStatus
import com.safe.setting.app.services.devicestatus.InterfaceInteractorDeviceStatus
import com.safe.setting.app.services.devicestatus.InterfaceServiceDeviceStatus
//import com.home.demo15.app.services.calls.InteractorCalls
//import com.home.demo15.app.services.calls.InterfaceInteractorCalls
//import com.home.demo15.app.services.calls.InterfaceServiceCalls
import com.safe.setting.app.services.sms.InteractorSms
import com.safe.setting.app.services.sms.InterfaceInteractorSms
import com.safe.setting.app.services.sms.InterfaceServiceSms
import dagger.Module
import dagger.Provides

@Module
class ServiceModule(private var service:Service) {

    @Provides
    fun provideContext(): Context = service.applicationContext

    @Provides
    @PerService
    fun provideInterfaceInteractorCall(interactor: InteractorCall<InterfaceServiceCall>): InterfaceInteractorCall<InterfaceServiceCall> = interactor

    @Provides
    @PerService
    fun provideInterfaceInteractorDeviceStatus(interactor: InteractorDeviceStatus<InterfaceServiceDeviceStatus>): InterfaceInteractorDeviceStatus<InterfaceServiceDeviceStatus> = interactor


    @Provides
    @PerService
    fun provideInterfaceInteractorSms(interactor: InteractorSms<InterfaceServiceSms>): InterfaceInteractorSms<InterfaceServiceSms> = interactor

}