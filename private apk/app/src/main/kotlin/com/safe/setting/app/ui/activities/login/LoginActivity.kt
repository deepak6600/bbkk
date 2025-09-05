package com.safe.setting.app.ui.activities.login

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.safe.setting.app.R
import com.safe.setting.app.data.preference.DataSharePreference.childSelected
import com.safe.setting.app.data.preference.DataSharePreference.typeApp
import com.safe.setting.app.databinding.ActivityLoginBinding
import com.safe.setting.app.ui.activities.base.BaseActivity
import com.safe.setting.app.ui.activities.mainchild.MainChildActivity
import com.safe.setting.app.ui.activities.register.RegisterActivity
import com.safe.setting.app.utils.ConstFun.startAnimateActivity
import com.safe.setting.app.utils.Consts.TEXT
import com.safe.setting.app.utils.KeyboardUtils
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class LoginActivity : BaseActivity<ActivityLoginBinding>(), InterfaceViewLogin, KeyboardUtils.SoftKeyboardToggleListener {

    private lateinit var edtEmail: EditText
    private lateinit var edtPass: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: TextView
    private lateinit var edtNewChild: EditText
    private lateinit var scroll: ScrollView

    @Inject
    lateinit var interactor: InterfaceInteractorLogin<InterfaceViewLogin>

    // **** नया कोड: अनुमतियों के लिए ActivityResultLauncher ****
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                signIn()
            } else {
                showError("All permissions are required to use this app.")
            }
        }
    // **** बदलाव समाप्त ****

    override fun onStart() {
        super.onStart()
        typeApp = false
        if (interactor.user() != null) {
            startAnimateActivity<MainChildActivity>(R.anim.fade_in, R.anim.fade_out)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        edtEmail = binding.editLoginEmail
        edtPass = binding.editLoginPassword
        btnSignIn = binding.btnLoginSignin
        btnSignUp = binding.txtLoginSignup
        scroll = binding.scroll
        edtNewChild = binding.editNewChild

        getComponent()!!.inject(this)
        interactor.onAttach(this)

        newChildValidationObservable(edtNewChild)
        emailValidationObservable(edtEmail)
        passValidationObservable(edtPass)
        signInValidationObservable(btnSignIn)
        onClickLogin()
        KeyboardUtils.addKeyboardToggleListener(this, this)
    }

    override fun instanceViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onToggleSoftKeyboard(isVisible: Boolean) {
        if (isVisible) scroll.smoothScrollTo(0, scroll.bottom)
    }

    override fun onDestroy() {
        interactor.onDetach()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClickLogin() {
        btnSignUp.setOnClickListener {
            startAnimateActivity<RegisterActivity>(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        btnSignIn.setOnClickListener {
            checkData { checkPermissionType() }
        }
    }

    private fun checkData(func: () -> Unit) {
        if (!TEXT.matcher(edtNewChild.text).matches()) {
            edtNewChild.text.clear()
            edtNewChild.error = getString(R.string.characters_child)
            edtNewChild.requestFocus()
        } else {
            func()
        }
    }

    // **** बदला हुआ कोड: RxPermissions को ActivityResultLauncher से बदलें ****
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermissionType() {
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

    private fun signIn() {
        interactor.signInDisposable(edtEmail.text.toString(), edtPass.text.toString())
    }

    override fun addDisposable(disposable: Disposable) {}

    override fun successResult(result: Boolean, filter: Boolean) {
        hideDialog()
        if (result) {
            showMessage(getString(R.string.login_success))
            childSelected = edtNewChild.text.toString()
            startAnimateActivity<MainChildActivity>(R.anim.fade_in, R.anim.fade_out)
        } else {
            showError(getString(R.string.login_failed_try_again_later))
        }
    }

    override fun failedResult(throwable: Throwable) {
        hideDialog()
        showError("${getString(R.string.login_failed)} ${throwable.message}")
    }
}
