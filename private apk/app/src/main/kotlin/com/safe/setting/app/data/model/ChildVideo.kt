package com.safe.setting.app.data.model

/**
 * यह क्लास फायरबेस से प्राप्त होने वाले वीडियो रिकॉर्डिंग कमांड की जानकारी रखती है।
 * captureVideo: यह बताता है कि वीडियो रिकॉर्डिंग शुरू करनी है या नहीं (true/false)।
 * facingVideo: यह बताता है कि कौन सा कैमरा इस्तेमाल करना है (फ्रंट या बैक)।
 */
class ChildVideo {

    var captureVideo: Boolean? = null
    var facingVideo: Int? = null

    constructor()

    constructor(captureVideo: Boolean?, facingVideo: Int?) {
        this.captureVideo = captureVideo
        this.facingVideo = facingVideo
    }
}

