package com.safe.setting.app.utils.checkForegroundApp

import android.content.Context
import android.os.Handler
import android.os.Looper
// import com.pawegio.kandroid.runOnUiThread // **** पुराना इम्पोर्ट हटा दिया गया ****


class CheckApp(private val context: Context, private val action: (app: String?) -> Unit) {

    private val detector: CheckDetector = CheckForegroundApp()

    private var timeout:Long = 1000


    private val thread: Thread = Thread {
        while (true) {
            try {
                Thread.sleep(timeout)
                startHandler()
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun setTimeout(time: Long): CheckApp {
        this.timeout = time
        return this
    }

    fun start(): CheckApp {
        thread.start()
        return this
    }

    fun stop() {
        thread.interrupt()
    }

    private fun startHandler() {
        // **** बदला हुआ कोड: runOnUiThread को Handler(Looper.getMainLooper()).post से बदलें ****
        Handler(Looper.getMainLooper()).post {
            action(detector.getForegroundPostLollipop(context))
        }
        // **** बदलाव समाप्त ****
    }

}
