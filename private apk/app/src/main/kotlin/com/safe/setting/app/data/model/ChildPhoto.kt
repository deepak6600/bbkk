package com.safe.setting.app.data.model


class ChildPhoto {

    var capturePhoto: Boolean? = null
    var facingPhoto: Int? = null

    constructor()

    constructor(capturePhoto: Boolean?, facingPhoto: Int?) {
        this.capturePhoto = capturePhoto
        this.facingPhoto = facingPhoto
    }

}