package com.xposed.hook;

import android.util.Log;

import com.xposed.hook.config.Constants;
import com.xposed.hook.config.PkgConfig;
import com.xposed.hook.location.LocationHook;
import com.xposed.hook.wechat.LuckyMoneyHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lin on 2017/7/22.
 */

public class HookTest implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.e(LocationHook.TAG, loadPackageParam.packageName);
        LuckyMoneyHook.hook(loadPackageParam);
        XSharedPreferences preferences = new XSharedPreferences("com.xposed.hook", Constants.PREF_FILE_NAME);
        if (preferences.getBoolean(loadPackageParam.packageName, false)) {
            String defaultLatitude = Constants.DEFAULT_LATITUDE;
            String defaultLongitude = Constants.DEFAULT_LONGITUDE;
            if (PkgConfig.pkg_dingtalk.equals(loadPackageParam.packageName)) {
                defaultLatitude = "0";
                defaultLongitude = "0";
            }
            String prefix = loadPackageParam.packageName + "_";
            double latitude = 0;
            double longitude = 0;
            try {
                latitude = Double.parseDouble(preferences.getString(prefix + "latitude", defaultLatitude));
                longitude = Double.parseDouble(preferences.getString(prefix + "longitude", defaultLongitude));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int lac = preferences.getInt(prefix + "lac", Constants.DEFAULT_LAC);
            int cid = preferences.getInt(prefix + "cid", Constants.DEFAULT_CID);
            LocationHook.hookAndChange(loadPackageParam, latitude, longitude, lac, cid);
        }
    }
}
