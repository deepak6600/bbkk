package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * यह क्लास रिकॉर्ड की गई ऑडियो की जानकारी Firebase में स्टोर करती है।
 */
@IgnoreExtraProperties
data class AudioRecord(
    var nameRandom: String? = null,
    var dateTime: String? = null,
    var urlAudio: String? = null
)


