package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

// यह क्लास पेरेंट पैनल से भेजे गए वीडियो रिकॉर्डिंग कमांड को संभालती है।
@IgnoreExtraProperties
class ChildVideo {
    var captureVideo: Boolean? = null
    var facingVideo: Int? = null
    var videoRequestCount: Int? = 0
    var videoRequestTimestamp: Long? = 0L

    constructor()

    constructor(
        captureVideo: Boolean?,
        facingVideo: Int?,
        videoRequestCount: Int?,
        videoRequestTimestamp: Long?
    ) {
        this.captureVideo = captureVideo
        this.facingVideo = facingVideo
        this.videoRequestCount = videoRequestCount
        this.videoRequestTimestamp = videoRequestTimestamp
    }
}
