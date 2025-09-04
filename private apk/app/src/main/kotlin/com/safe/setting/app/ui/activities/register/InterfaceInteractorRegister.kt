package com.safe.setting.app.ui.activities.register

import com.safe.setting.app.di.PerActivity
import com.safe.setting.app.ui.activities.base.InterfaceInteractor


@PerActivity
interface InterfaceInteractorRegister<V : InterfaceViewRegister> : InterfaceInteractor<V> {

    fun signUpDisposable(email: String, pass: String)

}