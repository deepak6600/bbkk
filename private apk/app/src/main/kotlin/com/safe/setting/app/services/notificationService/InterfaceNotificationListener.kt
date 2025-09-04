package com.safe.setting.app.services.notificationService

import android.graphics.Bitmap


interface InterfaceNotificationListener {

    fun setRunService(run : Boolean)
    fun getNotificationExists(id:String,exec:() -> Unit)
    fun setDataMessageNotification(type:Int,text:String?,title:String?,nameImage:String?,image:Bitmap?)

}