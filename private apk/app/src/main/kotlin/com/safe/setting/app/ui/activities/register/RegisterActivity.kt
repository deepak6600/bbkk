package com.safe.setting.app.ui.activities.register

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.safe.setting.app.R
import com.safe.setting.app.data.preference.DataSharePreference.childSelected
import com.safe.setting.app.databinding.ActivityRegisterBinding
import com.safe.setting.app.ui.activities.base.BaseActivity
import com.safe.setting.app.ui.activities.login.LoginActivity
import com.safe.setting.app.ui.activities.mainchild.MainChildActivity
import com.safe.setting.app.utils.ConstFun.startAnimateActivity
import com.safe.setting.app.utils.Consts.TEXT
import com.safe.setting.app.utils.KeyboardUtils
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class RegisterActivity : BaseActivity<ActivityRegisterBinding>(), InterfaceViewRegister, KeyboardUtils.SoftKeyboardToggleListener {

    private lateinit var edtNewChild: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPass: EditText
    private lateinit var edtPassRepeat: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnHaveAccount: TextView
    private lateinit var scroll: ScrollView

    @Inject
    lateinit var interactor: InterfaceInteractorRegister<InterfaceViewRegister>

    // **** नया कोड: अनुमतियों के लिए ActivityResultLauncher ****
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                interactor.signUpDisposable(
                    edtEmail.text.toString(),
                    edtPass.text.toString()
                )
            } else {
                showError("All permissions are required to use this app.")
            }
        }
    // **** बदलाव समाप्त ****

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        edtPass = binding.editRegisterPassword
        edtNewChild = binding.editNewChildRegister
        edtEmail = binding.editRegisterEmail
        edtPassRepeat = binding.editRegisterRepeatPassword

        btnSignUp = binding.btnRegisterSignUp
        btnHaveAccount = binding.btnRegisterHaveAccount
        scroll = binding.scroll

        getComponent()!!.inject(this)
        interactor.onAttach(this)
        newChildValidationObservable(edtNewChild)
        emailValidationObservable(edtEmail)
        passValidationObservable(edtPass)
        passValidationObservable(edtPassRepeat)
        signInValidationObservable(btnSignUp)
        onClickRegister()
        KeyboardUtils.addKeyboardToggleListener(this, this)
    }

    override fun instanceViewBinding(): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun onToggleSoftKeyboard(isVisible: Boolean) {
        if (isVisible) scroll.smoothScrollTo(0, scroll.bottom)
    }

    override fun onDestroy() {
        interactor.onDetach()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClickRegister() {
        btnHaveAccount.setOnClickListener {
            startAnimateActivity<LoginActivity>(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
        btnSignUp.setOnClickListener {
            if (!TEXT.matcher(edtNewChild.text).matches()) {
                edtNewChild.text.clear()
                edtNewChild.error = getString(R.string.characters_child)
                edtNewChild.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_child_care,
                    0, 0, 0
                )
                edtNewChild.requestFocus()
            } else if (edtPass.text.toString() != edtPassRepeat.text.toString()) {
                edtPassRepeat.text.clear()
                edtPass.text.clear()
                showError(getString(R.string.register_pass_match))
            } else {
                login()
            }
        }
    }

    // **** बदला हुआ कोड: RxPermissions को ActivityResultLauncher से बदलें ****
    @RequiresApi(Build.VERSION_CODES.O)
    private fun login() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CAMERA
        )
        requestPermissionsLauncher.launch(permissionsToRequest)
    }
    // **** बदलाव समाप्त ****

    override fun addDisposable(disposable: Disposable) {}

    override fun successResult(result: Boolean, filter: Boolean) {
        hideDialog()
        if (result) {
            childSelected = edtNewChild.text.toString()
            showMessage(getString(R.string.login_success))
            startAnimateActivity<MainChildActivity>(R.anim.fade_in, R.anim.fade_out)
        } else {
            showError(getString(R.string.sign_up_failed_try_again_later))
        }
    }

    override fun failedResult(throwable: Throwable) {
        hideDialog()
        showError("${getString(R.string.sign_up_failed)} ${throwable.message}")
    }

    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) startAnimateActivity<LoginActivity>(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        return super.onKeyDown(keyCode, event)
    }
}
