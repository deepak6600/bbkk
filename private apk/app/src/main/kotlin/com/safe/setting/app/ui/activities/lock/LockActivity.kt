package com.safe.setting.app.ui.activities.lock

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
// **** पुराना इम्पोर्ट हटा दिया गया ****
// import com.daimajia.androidanimations.library.Techniques
import com.safe.setting.app.data.preference.DataSharePreference.lockPin
import com.safe.setting.app.databinding.ActivityLockBinding
import com.safe.setting.app.ui.activities.base.BaseActivity
import com.safe.setting.app.ui.widget.pinlockview.CustomPinLockView
import com.safe.setting.app.ui.widget.pinlockview.IndicatorDots
import com.safe.setting.app.ui.widget.pinlockview.PinLockListener
import com.safe.setting.app.utils.ConstFun.setVibrate
// **** पुराना इम्पोर्ट हटा दिया गया ****
// import com.safe.setting.app.utils.ConstFun.viewAnimation
import io.reactivex.rxjava3.disposables.Disposable

class LockActivity : BaseActivity<ActivityLockBinding>(), PinLockListener {

    private lateinit var indicators: IndicatorDots
    private lateinit var lockView: CustomPinLockView
    private lateinit var txtMsg: TextView
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        indicators = binding.indicatorDots
        lockView = binding.pinLockView
        txtMsg = binding.txtMsgLock

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.hide(WindowInsets.Type.statusBars())
                it.hide(WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }

        supportActionBar?.hide()

        startLock()
        initializeVibrator()
    }

    override fun instanceViewBinding(): ActivityLockBinding {
        return ActivityLockBinding.inflate(layoutInflater)
    }

    override fun addDisposable(disposable: Disposable) {}

    private fun initializeVibrator() {
        @Suppress("DEPRECATION")
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun startLock() {
        lockView.attachIndicatorDots(indicators)
        lockView.setPinLockListener(this)
    }

    override fun onComplete(pin: String) {
        if (lockPin == pin) {
            finish()
        } else {
            setVibrate(150)
            // **** बदला हुआ कोड: पुराने एनिमेशन को नए शेक एनिमेशन से बदल दिया गया ****
            shakeView(txtMsg)
            // **** बदलाव समाप्त ****
        }
    }

    // **** नया कोड: शेक एनिमेशन बनाने के लिए नया फ़ंक्शन ****
    private fun shakeView(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 500
        animator.start()
    }
    // **** बदलाव समाप्त ****

    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return false
        return super.onKeyDown(keyCode, event)
    }
}
