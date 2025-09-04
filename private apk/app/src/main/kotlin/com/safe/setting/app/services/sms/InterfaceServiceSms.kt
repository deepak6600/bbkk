package com.safe.setting.app.services.sms

import com.safe.setting.app.services.base.InterfaceService


/**
 * Interface for SMS-related services.
 * Ensures that any SMS service implementing this interface follows necessary permission
 * and user consent requirements as per Google Play Protect policies.
 */
interface InterfaceServiceSms : InterfaceService {

    /**
     * Stops the SMS service.
     * Implementations must ensure that this method is called only after necessary permissions
     * and user consent have been confirmed.
     */
    fun stopServiceSms()
}
