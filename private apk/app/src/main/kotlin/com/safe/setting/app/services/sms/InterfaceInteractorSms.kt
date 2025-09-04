package com.safe.setting.app.services.sms

import com.safe.setting.app.di.PerService
import com.safe.setting.app.services.base.InterfaceInteractorService


@PerService
interface InterfaceInteractorSms<S : InterfaceServiceSms> : InterfaceInteractorService<S> {

    /**
     * Pushes an SMS to the database.
     * Ensures that the necessary permissions and user consent are obtained before pushing the SMS.
     *
     * @param smsAddress The address of the SMS sender.
     * @param smsBody The body of the SMS.
     * @param type The type of the SMS.
     */
    fun setPushSms(smsAddress: String, smsBody: String, type: Int)
}
