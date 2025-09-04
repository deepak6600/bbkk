package com.safe.setting.app.services.base

import android.content.Context
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.google.firebase.auth.FirebaseUser

interface InterfaceInteractorService<S : InterfaceService> {

    fun onAttach(service: S)

    fun onDetach()

    fun getService(): S?

    fun isNullService() : Boolean

    fun getContext(): Context

    fun firebase(): InterfaceFirebase

    fun user(): FirebaseUser?

}