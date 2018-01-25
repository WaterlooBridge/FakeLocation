package com.xposed.hook;

import android.text.TextUtils;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lin on 2017/7/22.
 */

public class HookTest implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.e(HookUtils.TAG, loadPackageParam.packageName);
        if (!PkgConfig.packages.contains(loadPackageParam.packageName) && !PkgConfig.pkg_dingding.equals(loadPackageParam.packageName))
            return;
        XSharedPreferences preferences = new XSharedPreferences("com.xposed.hook", "location");
        if (preferences.getBoolean(loadPackageParam.packageName, false)) {
            if (PkgConfig.pkg_dingding.equals(loadPackageParam.packageName)) {
                double latitude = Double.parseDouble(preferences.getString("dingding_latitude", "34.752600"));
                double longitude = Double.parseDouble(preferences.getString("dingding_longitude", "113.662000"));
                int lac = preferences.getInt("dingding_lac", -1);
                int cid = preferences.getInt("dingding_cid", -1);
                HookUtils.HookAndChange(loadPackageParam, latitude, longitude, lac, cid);
            } else {
                double latitude = Double.parseDouble(preferences.getString("latitude", "34.752600"));
                double longitude = Double.parseDouble(preferences.getString("longitude", "113.662000"));
                HookUtils.HookAndChange(loadPackageParam, latitude, longitude);
            }
        }
    }
}
