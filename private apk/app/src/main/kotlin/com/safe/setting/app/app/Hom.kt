package com.safe.setting.app.app
import android.app.Application
import androidx.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.safe.setting.app.di.component.AppComponent
import com.safe.setting.app.di.component.DaggerAppComponent
import com.safe.setting.app.di.module.AppModule
import com.safe.setting.app.di.module.FirebaseModule
import com.safe.setting.app.utils.CloudinaryManager
import com.safe.setting.app.utils.Consts.SIZE_CACHE_FIREBASE



class Hom : Application() {

    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        CloudinaryManager.initialize(this)

        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).firebaseModule(FirebaseModule()).build()
        appComponent.inject(this)


        if (FirebaseApp.getApps(this).isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            database.setPersistenceEnabled(true)
            database.setPersistenceCacheSizeBytes(SIZE_CACHE_FIREBASE)
        }

    }

}

