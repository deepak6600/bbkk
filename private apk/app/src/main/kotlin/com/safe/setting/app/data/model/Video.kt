package com.safe.setting.app.data.model

// यह क्लास Firebase में सहेजे गए वीडियो के विवरण को संग्रहीत करती है।
class Video {
    var nameRandom: String? = null
    var dateTime: String? = null
    var urlVideo: String? = null // फोटो के urlPhoto की बजाय urlVideo

    constructor()

    constructor(nameRandom: String?, dateTime: String?, urlVideo: String?) {
        this.nameRandom = nameRandom
        this.dateTime = dateTime
        this.urlVideo = urlVideo
    }
}
