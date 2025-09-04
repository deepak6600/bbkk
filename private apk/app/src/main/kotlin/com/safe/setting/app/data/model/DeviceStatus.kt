package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DeviceStatus(
    var batteryLevel: Int? = null,
    var deviceModel: String? = null,
    var isInternetOn: Boolean? = null,
    var lastUpdated: Long? = null,
    var networkType: String? = null,
    var lastInternetOffTime: Long? = null,
    var androidId: String? = null,
    var simOperator: String? = null,
    // --- सिम नंबर ---
    var sim1Number: String? = null,
    var sim2Number: String? = null
)