package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

// @IgnoreExtraProperties अज्ञात फील्ड को अनदेखा करने में मदद करता है
@IgnoreExtraProperties
data class Location(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var address: String? = null,
    var dateTime: String? = null
)
