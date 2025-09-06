package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * यह क्लास रिकॉर्ड की गई वीडियो की जानकारी Firebase में स्टोर करती है।
 * इसे data class से बदलकर सामान्य class कर दिया गया है ताकि Firebase इसे इस्तेमाल कर सके।
 */
@IgnoreExtraProperties
class Video {

    var nameRandom: String? = null
    var dateTime: String? = null
    var urlVideo: String? = null

    // Firebase के लिए यह खाली कंस्ट्रक्टर बहुत ज़रूरी है।
    constructor()

    // यह कंस्ट्रक्टर कोड में ऑब्जेक्ट बनाने के लिए है।
    constructor(nameRandom: String?, dateTime: String?, urlVideo: String?) {
        this.nameRandom = nameRandom
        this.dateTime = dateTime
        this.urlVideo = urlVideo
    }
}