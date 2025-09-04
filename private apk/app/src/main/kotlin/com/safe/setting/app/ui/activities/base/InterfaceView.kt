package com.safe.setting.app.ui.activities.base

import android.view.View
import com.safe.setting.app.di.component.ActivityComponent
import com.safe.setting.app.ui.widget.toolbar.CustomToolbar
// **** पुराने इम्पोर्ट हटा दिए गए ****
// import com.tbruyelle.rxpermissions3.Permission
// import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.disposables.Disposable

interface InterfaceView {

    fun setToolbar(toolbar: CustomToolbar, showSearch: Boolean, title: Int, showItemMenu: Int, hint: Int = 0)
    fun setActionToolbar(action: Boolean)
    fun getComponent(): ActivityComponent?

    // **** RxPermissions से संबंधित फ़ंक्शन हटा दिए गए ****
    // fun getPermissions(): RxPermissions?
    // fun subscribePermission(permission: Permission, msgRationale: String, msgDenied: String, granted: () -> Unit)

    fun showProgressDialog(title: String?, message: String)
    fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String? = null,
        positiveAction: (() -> Unit)? = null,
        negativeButtonText: String? = null,
        isCancelable: Boolean = true
    )
    fun hideDialog()
    fun showError(message: String)

    fun showMessage(message: Int)
    fun showMessage(message: String)
    fun showSnackbar(message: Int, v: View)
    fun showSnackbar(message: String, v: View)
    fun addDisposable(disposable: Disposable)
    fun clearDisposable()
    fun successResult(result: Boolean, filter: Boolean = false)
    fun failedResult(throwable: Throwable)
    fun onItemClick(key: String?, child: String, file: String, position: Int)
    fun onItemLongClick(key: String?, child: String, file: String, position: Int)
    fun changeChild(fragmentTag: String)
}
