package com.safe.setting.app.utils.checkForegroundApp

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.safe.setting.app.utils.Consts.PERMISSION_USAGE_STATS
// import com.pawegio.kandroid.isPermissionGranted // **** पुराना इम्पोर्ट हटा दिया गया ****


object CheckPermission{

    fun Context.hasUsageStatsPermission(): Boolean {
        val mode = getModeManager(AppOpsManager.OPSTR_GET_USAGE_STATS)
        // **** बदला हुआ कोड: isPermissionGranted को ContextCompat.checkSelfPermission से बदलें ****
        return if (mode == AppOpsManager.MODE_DEFAULT) ContextCompat.checkSelfPermission(this, PERMISSION_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        else mode == AppOpsManager.MODE_ALLOWED
        // **** बदलाव समाप्त ****
    }

    @Suppress("DEPRECATION")
    fun Context.getModeManager(ops: String): Int {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(ops, android.os.Process.myUid(), packageName)
        } else {

            appOps.checkOpNoThrow(ops, android.os.Process.myUid(), packageName)
        }
    }
}
