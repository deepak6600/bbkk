package com.safe.setting.app.di.module

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.safe.setting.app.di.PerActivity
import com.safe.setting.app.ui.activities.login.InteractorLogin
import com.safe.setting.app.ui.activities.login.InterfaceInteractorLogin
import com.safe.setting.app.ui.activities.login.InterfaceViewLogin
import com.safe.setting.app.ui.activities.register.InteractorRegister
import com.safe.setting.app.ui.activities.register.InterfaceInteractorRegister
import com.safe.setting.app.ui.activities.register.InterfaceViewRegister
import dagger.Module
import dagger.Provides

/**
 * Created by Lisandro Gómez on 11/20/20.
 */
@Module
class ActivityModule(var activity: AppCompatActivity) {

    @Provides
    fun provideActivity(): Activity = activity

    @Provides
    fun provideContext(): Context = activity

    @Provides
    fun provideSupportFragmentManager(): FragmentManager = activity.supportFragmentManager

    @Provides
    fun provideLinearLayoutManager(): LinearLayoutManager = LinearLayoutManager(activity)

    @Provides
    @PerActivity
    fun provideInterfaceInteractorLogin(interactor: InteractorLogin<InterfaceViewLogin>): InterfaceInteractorLogin<InterfaceViewLogin> = interactor

    @Provides
    @PerActivity
    fun provideInterfaceInteractorRegister(interactor: InteractorRegister<InterfaceViewRegister>): InterfaceInteractorRegister<InterfaceViewRegister> = interactor
}
