package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class KeyData(
    var keyID: String? = null,
    var keyText: String? = null
)