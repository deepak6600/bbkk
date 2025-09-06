package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * हिंदी कमेंट: इस क्लास को भी 'data class' से बदलकर सामान्य 'class' कर दिया गया है
 * और फायरबेस के लिए एक खाली `constructor()` जोड़ा गया है।
 */
@IgnoreExtraProperties
class ChildAudio {

    var recordAudio: Boolean? = null

    // खाली कंस्ट्रक्टर (Firebase के लिए आवश्यक)
    constructor()

    // डेटा के साथ कंस्ट्रक्टर (वैकल्पिक)
    constructor(recordAudio: Boolean?) {
        this.recordAudio = recordAudio
    }
}