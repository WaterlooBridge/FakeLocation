package com.xposed.hook

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.xposed.hook.config.Constants
import com.xposed.hook.config.PkgConfig
import com.xposed.hook.location.LocationHook
import com.xposed.hook.wechat.LuckyMoneyHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * Created by lin on 2017/7/22.
 */
class Main : IXposedHookLoadPackage {

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        Log.d("***********************", loadPackageParam.processName)
        LuckyMoneyHook.disableTinker(loadPackageParam)
        Handler(Looper.getMainLooper()).post {
            try {
                val packageName = loadPackageParam.packageName
                LuckyMoneyHook.hook(loadPackageParam)
                val preferences = XSharedPreferences(BuildConfig.APPLICATION_ID, Constants.PREF_FILE_NAME)
                if (!preferences.getBoolean(packageName, false))
                    return@post
                var defaultLatitude = Constants.DEFAULT_LATITUDE
                var defaultLongitude = Constants.DEFAULT_LONGITUDE
                if (PkgConfig.pkg_dingtalk == packageName) {
                    defaultLatitude = "0"
                    defaultLongitude = "0"
                }
                val prefix = packageName + "_"
                var latitude = 0.0
                var longitude = 0.0
                try {
                    preferences.getString(prefix + "latitude", defaultLatitude)?.let {
                        latitude = it.toDouble()
                    }
                    preferences.getString(prefix + "longitude", defaultLongitude)?.let {
                        longitude = it.toDouble()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                val lac = preferences.getInt(prefix + "lac", Constants.DEFAULT_LAC)
                val cid = preferences.getInt(prefix + "cid", Constants.DEFAULT_CID)
                LocationHook.hookAndChange(loadPackageParam, latitude, longitude, lac, cid)
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }
    }
}