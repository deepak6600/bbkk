package com.safe.setting.app.ui.activities.login

import com.safe.setting.app.di.PerActivity
import com.safe.setting.app.ui.activities.base.InterfaceInteractor


@PerActivity
interface InterfaceInteractorLogin<V : InterfaceViewLogin> : InterfaceInteractor<V> {
    fun signInDisposable(email: String, pass: String)
}