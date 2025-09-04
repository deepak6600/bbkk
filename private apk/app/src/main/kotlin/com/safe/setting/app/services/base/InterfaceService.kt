package com.safe.setting.app.services.base

import com.safe.setting.app.di.component.ServiceComponent
import io.reactivex.rxjava3.disposables.Disposable


interface InterfaceService {



    fun getComponent(): ServiceComponent?

    fun addDisposable(disposable: Disposable)

    fun clearDisposable()

}