package com.safe.setting.app.utils.async

import android.content.Context
import com.safe.setting.app.ui.activities.base.BaseActivity
import com.safe.setting.app.utils.ConstFun.isRoot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AsyncTaskRootPermission(private val context: Context, private val result: (result: Boolean) -> Unit) {

    @OptIn(DelicateCoroutinesApi::class)
    fun execute() {
        // **** बदला हुआ कोड: डायलॉग दिखाने से पहले जांचें कि context एक Activity है ****
        if (context is BaseActivity<*>) {
            context.showProgressDialog(null, "Checking root access...")
        }

        GlobalScope.launch(Dispatchers.IO) {
            // बैकग्राउंड में रूट की जांच करें
            val isRooted = isRoot()

            // मुख्य थ्रेड पर वापस आकर UI अपडेट करें
            launch(Dispatchers.Main) {
                // डायलॉग को छिपाएं और परिणाम वापस भेजें
                if (context is BaseActivity<*>) {
                    context.hideDialog()
                }
                result(isRooted)
            }
        }
    }
}
