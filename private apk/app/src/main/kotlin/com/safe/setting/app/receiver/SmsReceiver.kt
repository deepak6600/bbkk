package com.safe.setting.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.safe.setting.app.data.preference.DataSharePreference.typeApp
import com.safe.setting.app.services.sms.SmsService
import com.safe.setting.app.utils.Consts.TYPE_SMS_INCOMING
import com.safe.setting.app.utils.ConstFun.startServiceSms


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        var smsAddress = ""
        var smsBody = ""

        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            smsAddress = smsMessage.displayOriginatingAddress
            smsBody += smsMessage.messageBody
        }

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION){
            if (!context.typeApp) context.startServiceSms<SmsService>(smsAddress,smsBody, TYPE_SMS_INCOMING)
        }
    }

}