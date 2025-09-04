package com.safe.setting.app.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.cloudinary.Transformation
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import io.reactivex.rxjava3.core.Single
import java.io.File

object CloudinaryManager {

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val config = mapOf(
            "cloud_name" to Keys.getCloudinaryCloudName(),
            "api_key" to Keys.getCloudinaryApiKey(),
            "api_secret" to Keys.getCloudinaryApiSecret(),
            "secure" to true
        )
        MediaManager.init(context, config)
        isInitialized = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadPhoto(file: File): Single<String> {
        return Single.create { emitter ->
            MediaManager.get().upload(file.absolutePath)
                .option("transformation", Transformation<Transformation<*>>()
                    .width(1080)
                    .quality("auto:good")
                    .fetchFormat("auto"))
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            emitter.onSuccess(url)
                        } else {
                            emitter.onError(Throwable("Cloudinary URL not found in response"))
                        }
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        emitter.onError(Throwable(error.description))
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        }
    }

    // --- नया कोड यहाँ से शुरू होता है ---
    // यह फ़ंक्शन वीडियो फ़ाइलों को Cloudinary पर अपलोड करता है।
    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadVideo(file: File): Single<String> {
        return Single.create { emitter ->
            MediaManager.get().upload(file.absolutePath)
                // 'resource_type' को 'video' पर सेट करना महत्वपूर्ण है।
                .option("resource_type", "video")
                .option("transformation", Transformation<Transformation<*>>()
                    // वीडियो की गुणवत्ता को 'auto:eco' पर सेट करके उसे कंप्रेस (श्रिंक) करें।
                    .quality("auto:eco")
                    // वीडियो को mp4 फॉर्मेट में बदलें।
                    .fetchFormat("mp4"))
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // अपलोड शुरू होने पर
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // अपलोड की प्रगति
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        // अपलोड सफल होने पर
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            emitter.onSuccess(url)
                        } else {
                            emitter.onError(Throwable("Cloudinary video URL not found in response"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        // त्रुटि होने पर
                        emitter.onError(Throwable(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        // पुनः शेड्यूल होने पर
                    }
                }).dispatch()
        }
    }
    // --- नया कोड यहाँ समाप्त होता है ---
}


