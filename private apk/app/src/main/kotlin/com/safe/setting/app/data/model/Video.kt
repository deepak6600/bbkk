package com.safe.setting.app.data.model

/**
 * यह क्लास फायरबेस में सेव होने वाली वीडियो की जानकारी रखती है।
 * nameRandom: वीडियो फ़ाइल का एक यूनिक नाम।
 * dateTime: वीडियो कब रिकॉर्ड किया गया था, उसका समय।
 * urlVideo: Cloudinary पर अपलोड होने के बाद वीडियो का URL।
 */
class Video {

    var nameRandom: String? = null
    var dateTime: String? = null
    var urlVideo: String? = null

    constructor()

    constructor(nameRandom: String?, dateTime: String?, urlVideo: String?) {
        this.nameRandom = nameRandom
        this.dateTime = dateTime
        this.urlVideo = urlVideo
    }
}

