package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * हिंदी कमेंट: इस क्लास को 'data class' से बदलकर सामान्य 'class' कर दिया गया है।
 * फायरबेस को डेटा से ऑब्जेक्ट बनाने के लिए एक खाली कंस्ट्रक्टर की आवश्यकता होती है,
 * इसलिए एक खाली `constructor()` जोड़ा गया है।
 * `@IgnoreExtraProperties` यह सुनिश्चित करता है कि अगर फायरबेस में कोई अतिरिक्त फील्ड हो तो ऐप क्रैश न हो।
 */
@IgnoreExtraProperties
class ChildVideo {

    var recordVideo: Boolean? = null
    var facing: Int? = null

    // खाली कंस्ट्रक्टर (Firebase के लिए आवश्यक)
    constructor()

    // डेटा के साथ कंस्ट्रक्टर (वैकल्पिक)
    constructor(recordVideo: Boolean?, facing: Int?) {
        this.recordVideo = recordVideo
        this.facing = facing
    }
}