package com.safe.setting.app.ui.activities.base

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.textChanges
import com.safe.setting.app.R
import com.safe.setting.app.app.Hom
import com.safe.setting.app.di.component.ActivityComponent
import com.safe.setting.app.di.component.DaggerActivityComponent
import com.safe.setting.app.di.module.ActivityModule
import com.safe.setting.app.ui.fragments.base.BaseFragment
import com.safe.setting.app.ui.widget.toolbar.CustomToolbar
import com.safe.setting.app.utils.ConstFun.adjustFontScale
import com.safe.setting.app.utils.Consts.TEXT
// **** पुराने इम्पोर्ट हटा दिए गए ****
// import com.tbruyelle.rxpermissions3.Permission
// import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(),
    InterfaceView, BaseFragment.Callback {

    private var currentDialog: AlertDialog? = null
    private var compositeDisposable: CompositeDisposable? = null
    // private var rxPermissions: RxPermissions? = null // **** हटा दिया गया ****
    private lateinit var snackbar: Snackbar

    companion object {
        @JvmStatic
        var activityComponent: ActivityComponent? = null
    }

    private lateinit var emailObservable: Observable<Boolean>
    private lateinit var passObservable: Observable<Boolean>
    private lateinit var signInEnabled: Observable<Boolean>
    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = instanceViewBinding()
        setContentView(binding.root)
        initializeActivityComponent()
        adjustFontScale()
    }

    abstract fun instanceViewBinding(): VB

    private fun initializeActivityComponent() {
        activityComponent = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .appComponent(Hom.appComponent).build()
        compositeDisposable = CompositeDisposable()
        // rxPermissions = RxPermissions(this) // **** हटा दिया गया ****
    }

    override fun getComponent(): ActivityComponent? = activityComponent

    // **** RxPermissions से संबंधित फ़ंक्शन हटा दिए गए ****
    // override fun getPermissions(): RxPermissions? = rxPermissions
    // override fun subscribePermission(...) { ... }

    override fun showProgressDialog(title: String?, message: String) {
        hideDialog()
        currentDialog = MaterialAlertDialogBuilder(this).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
        }.show()
    }

    override fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String?,
        positiveAction: (() -> Unit)?,
        negativeButtonText: String?,
        isCancelable: Boolean
    ) {
        hideDialog()
        currentDialog = MaterialAlertDialogBuilder(this).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(isCancelable)
            positiveButtonText?.let {
                setPositiveButton(it) { dialog, _ ->
                    positiveAction?.invoke()
                    dialog.dismiss()
                }
            }
            negativeButtonText?.let {
                setNegativeButton(it) { dialog, _ -> dialog.dismiss() }
            }
        }.show()
    }

    override fun hideDialog() {
        currentDialog?.dismiss()
        currentDialog = null
    }

    override fun showError(message: String) {
        showDialog(getString(R.string.ops), message, getString(android.R.string.ok))
    }

    override fun showMessage(message: Int) {
        showMessage(getString(message))
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showSnackbar(message: Int, v: View) {
        snackbar = Snackbar.make(v, message, Snackbar.LENGTH_LONG)
            .setAction(android.R.string.ok) { snackbar.dismiss() }
        snackbar.show()
    }

    override fun showSnackbar(message: String, v: View) {
        snackbar = Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(android.R.string.ok) { snackbar.dismiss() }
        snackbar.show()
    }

    override fun addDisposable(disposable: Disposable) {
        compositeDisposable?.add(disposable)
    }

    override fun clearDisposable() {
        compositeDisposable?.dispose()
        compositeDisposable?.clear()
    }

    // ... बाकी के फंक्शन्स वैसे ही रहेंगे ...
    fun newChildValidationObservable(newChild: EditText): Disposable {
        return newChild.textChanges().map { textNewChild -> TEXT.matcher(textNewChild).matches() }
            .distinctUntilChanged()
            .map { b -> if (b) R.drawable.ic_child_care else R.drawable.ic_child_care_black_24dp }
            .subscribe { id -> newChild.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0) }
    }

    fun emailValidationObservable(edtEmail: EditText) {
        emailObservable = edtEmail.textChanges().map { textEmail -> Patterns.EMAIL_ADDRESS.matcher(textEmail).matches() }
        emailObservable(edtEmail)
    }

    private fun emailObservable(edtEmail: EditText): Disposable {
        return emailObservable.distinctUntilChanged()
            .map { b -> if (b) R.drawable.ic_user else R.drawable.ic_user_alert }
            .subscribe { id -> edtEmail.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0) }
    }

    fun passValidationObservable(edtPass: EditText) {
        passObservable = edtPass.textChanges().map { textPass -> textPass.length > 5 }
        passObservable(edtPass)
    }

    private fun passObservable(edtPass: EditText): Disposable {
        return passObservable.distinctUntilChanged()
            .map { b -> if (b) R.drawable.ic_lock_open else R.drawable.ic_lock }
            .subscribe { id -> edtPass.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0) }
    }

    fun signInValidationObservable(btnSignIn: Button) {
        signInEnabled = Observable.combineLatest(
            emailObservable, passObservable
        ) { email, pass -> email && pass }
        signInEnableObservable(btnSignIn)
    }

    private fun signInEnableObservable(btnSignIn: Button): Disposable {
        return signInEnabled.distinctUntilChanged()
            .doOnNext { enabled ->
                btnSignIn.isEnabled = enabled
                val colorResId = if (enabled) R.color.dark_accent else R.color.colorTextDisabled
                val color = ContextCompat.getColorStateList(this, colorResId)
                btnSignIn.backgroundTintList = color
            }
            .subscribe()
    }

    override fun setActionToolbar(action: Boolean) {}
    override fun successResult(result: Boolean, filter: Boolean) {}
    override fun failedResult(throwable: Throwable) {}
    override fun onItemClick(key: String?, child: String, file: String, position: Int) {}
    override fun onItemLongClick(key: String?, child: String, file: String, position: Int) {}
    override fun setDrawerLock() {}
    override fun setDrawerUnLock() {}
    override fun openDrawer() {}
    override fun setMenu(menu: PopupMenu?) {}
    override fun changeChild(fragmentTag: String) {}
    override fun setToolbar(toolbar: CustomToolbar, showSearch: Boolean, title: Int, showItemMenu: Int, hint: Int) {}
}
