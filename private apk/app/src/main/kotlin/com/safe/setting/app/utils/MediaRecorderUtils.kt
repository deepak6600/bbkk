package com.safe.setting.app.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
// import com.pawegio.kandroid.e // **** पुराना इम्पोर्ट हटा दिया गया ****

@RequiresApi(Build.VERSION_CODES.S)
class MediaRecorderUtils(private val errorAction: () -> Unit, context: Context) : MediaRecorder(context) {

    fun startRecording(audioSource: Int, fileName: String?) {
        try {
            setAudioSource(audioSource)
            setOutputFormat(OutputFormat.THREE_GPP)
            setAudioEncoder(AudioEncoder.AMR_NB)
            setOutputFile(fileName)

            val errorListener = OnErrorListener { _, _, _ -> errorAction() }
            setOnErrorListener(errorListener)

            prepare()
            start()
        } catch (er: Throwable) {
            // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
            Log.e(Consts.TAG, er.message.toString())
            // **** बदलाव समाप्त ****
            errorAction()
        }
    }

    fun stopRecording(sendFile: () -> Unit) {
        try {
            stop()
            sendFile()
        } catch (e: Throwable) {
            // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
            Log.e(Consts.TAG, e.message.toString())
            // **** बदलाव समाप्त ****
            errorAction()
        }
    }
}
