package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Call(
    var phoneNumber: String? = null,
    var contactName: String? = null,
    var type: String? = null,
    var date: Long? = null,
    var duration: String? = null
)