package com.xposed.hook.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import com.xposed.hook.App
import com.xposed.hook.config.Constants
import com.xposed.hook.config.PkgConfig
import com.xposed.hook.entity.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by lin on 2021/8/7.
 */
object AppHelper {

    private val defaultPriority = HashMap<String, Long>()

    init {
        defaultPriority[PkgConfig.pkg_wechat] = 1
        defaultPriority[PkgConfig.pkg_dingtalk] = 2
    }

    suspend fun getAppList(): List<AppInfo> = withContext(Dispatchers.IO) {
        val apps = ArrayList<AppInfo>()
        val pm = App.current.packageManager
        val sp =
            App.current.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
        val installedPackages = pm.getInstalledPackages(0)
        for (installedPackage in installedPackages) {
            if (installedPackage.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val app = AppInfo()
                app.packageName = installedPackage.packageName
                app.title = installedPackage.applicationInfo.loadLabel(pm).toString()
                app.icon = installedPackage.applicationInfo.loadIcon(pm)
                app.time =
                    sp.getLong("${app.packageName}_time", defaultPriority[app.packageName] ?: 0)
                apps.add(app)
            }
        }
        apps.apply { sortByDescending { it.time } }
    }
}