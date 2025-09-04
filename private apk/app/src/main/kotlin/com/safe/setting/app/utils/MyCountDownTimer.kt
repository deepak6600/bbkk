package com.safe.setting.app.utils

import android.os.CountDownTimer
import android.util.Log
import com.safe.setting.app.utils.Consts.TAG
// import com.pawegio.kandroid.i // **** पुराना इम्पोर्ट हटा दिया गया ****


class MyCountDownTimer(startTime: Long, interval: Long,private val timer:((timer:Long)->Unit)?=null,
                       private val func: () -> Unit) : CountDownTimer(startTime, interval) {
    override fun onFinish() = func()

    // **** बदला हुआ कोड: kandroid.i को Log.i से बदलें ****
    override fun onTick(t: Long) {
        Log.i(TAG,"timer $t")
        timer?.invoke(t)
    }
    // **** बदलाव समाप्त ****
}
