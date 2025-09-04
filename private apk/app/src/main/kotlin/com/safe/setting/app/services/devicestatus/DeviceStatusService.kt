package com.safe.setting.app.services.devicestatus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.safe.setting.app.data.model.DeviceStatus
import com.safe.setting.app.data.preference.DataSharePreference.lastInternetOffTime
import com.safe.setting.app.services.base.BaseService
import java.util.Date
import javax.inject.Inject

class DeviceStatusService : BaseService(), InterfaceServiceDeviceStatus {

    @Inject
    lateinit var interactor: InterfaceInteractorDeviceStatus<InterfaceServiceDeviceStatus>

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var statusRunnable: Runnable
    private val updateInterval = 5 * 60 * 1000L // हर 5 मिनट

    override fun onCreate() {
        super.onCreate()
        getComponent()!!.inject(this)
        interactor.onAttach(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        statusRunnable = Runnable {
            collectAndSendStatus()
            handler.postDelayed(statusRunnable, updateInterval)
        }
        handler.post(statusRunnable)
        return START_STICKY
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun collectAndSendStatus() {
        val batteryLevel = getBatteryLevel()
        val deviceModel = getDeviceModel()
        val internetStatus = getInternetStatus()
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val simInfo = getSimInfo()

        // --- यह हिस्सा ठीक कर दिया गया है ---
        if (!internetStatus.first) {
            // सीधे कॉन्टेक्स्ट (this) का उपयोग करके वैरिएबल को सेट करें
            this.lastInternetOffTime = Date().time
        }

        val status = DeviceStatus(
            batteryLevel = batteryLevel,
            deviceModel = deviceModel,
            isInternetOn = internetStatus.first,
            networkType = internetStatus.second,
            androidId = androidId,
            // सीधे कॉन्टेक्स्ट (this) का उपयोग करके वैरिएबल को प्राप्त करें
            lastInternetOffTime = this.lastInternetOffTime,
            lastUpdated = Date().time,
            simOperator = simInfo.operator,
            sim1Number = simInfo.sim1,
            sim2Number = simInfo.sim2
        )
        // --- यहाँ तक बदलाव है ---
        interactor.sendDeviceStatus(status)
    }

    private data class SimInfo(val operator: String, val sim1: String, val sim2: String)

    @SuppressLint("MissingPermission", "ObsoleteSdkInt")
    private fun getSimInfo(): SimInfo {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operatorName = telephonyManager.simOperatorName ?: "No SIM"
        var sim1 = "N/A"
        var sim2 = "N/A"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            try {
                val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                if (subscriptionInfoList != null) {
                    for (subscriptionInfo in subscriptionInfoList) {
                        val number = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            subscriptionManager.getPhoneNumber(subscriptionInfo.subscriptionId)
                        } else {
                            @Suppress("DEPRECATION")
                            subscriptionInfo.number
                        }

                        if (subscriptionInfo.simSlotIndex == 0) {
                            sim1 = number ?: "SIM 1 (Unknown)"
                        } else if (subscriptionInfo.simSlotIndex == 1) {
                            sim2 = number ?: "SIM 2 (Unknown)"
                        }
                    }
                }
            } catch (e: SecurityException) {
                sim1 = "Permission Denied"
                sim2 = "Permission Denied"
            }
        }
        return SimInfo(operatorName, sim1, sim2)
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
    }

    @SuppressLint("MissingPermission")
    private fun getInternetStatus(): Pair<Boolean, String> {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return Pair(false, "No Connection")
        val capabilities = cm.getNetworkCapabilities(network) ?: return Pair(false, "No Connection")

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Pair(true, "WIFI")
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) tm.dataNetworkType else @Suppress("DEPRECATION") tm.networkType
                Pair(true, getNetworkClass(networkType))
            }
            else -> Pair(false, "No Connection")
        }
    }

    @Suppress("DEPRECATION")
    private fun getNetworkClass(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "Unknown"
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(statusRunnable)
        interactor.onDetach()
        super.onDestroy()
    }
}
