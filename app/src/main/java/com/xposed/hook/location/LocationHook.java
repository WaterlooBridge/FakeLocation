package com.xposed.hook.location;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lin on 2017/7/23.
 */

public class LocationHook {

    public static String TAG = "LocationHook";

    public static void hookAndChange(XC_LoadPackage.LoadPackageParam mLpp, final double latitude, final double longitude, final long lac, final long cid) {

        Log.d(TAG, "Avalon Hook Location Test: " + mLpp.packageName);
        LocationConfig.setLatitude(latitude);
        LocationConfig.setLongitude(longitude);

        hookMethod(WifiManager.class, "getScanResults", XC_MethodReplacement.returnConstant(Collections.emptyList()));
        hookMethod(WifiInfo.class, "getMacAddress", XC_MethodReplacement.returnConstant("02:00:00:00:00:00"));
        hookMethod(WifiInfo.class, "getSSID", XC_MethodReplacement.returnConstant("<unknown ssid>"));
        hookMethod(WifiInfo.class, "getBSSID", XC_MethodReplacement.returnConstant("02:00:00:00:00:00"));

        hookMethods("android.location.LocationManager", "requestLocationUpdates", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LocationHandler.getInstance().start();
            }
        });

        hookMethod("android.location.LocationManager", mLpp.classLoader, "getLastLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location loc = (Location) param.getResult();
                if (loc != null)
                    LocationHandler.updateLocation(loc, LocationConfig.getLatitude(), LocationConfig.getLongitude());
                else
                    param.setResult(LocationHandler.createLocation(LocationConfig.getLatitude(), LocationConfig.getLongitude()));
            }
        });

        hookMethods("android.location.LocationManager", "getLastKnownLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location loc = (Location) param.getResult();
                if (loc != null)
                    LocationHandler.updateLocation(loc, LocationConfig.getLatitude(), LocationConfig.getLongitude());
                else
                    param.setResult(LocationHandler.createLocation(LocationConfig.getLatitude(), LocationConfig.getLongitude()));
            }
        });

        hookMethod(Location.class, "getLatitude", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return LocationConfig.getLatitude();
            }
        });
        hookMethod(Location.class, "getLongitude", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return LocationConfig.getLongitude();
            }
        });
        hookMethod(LocationManager.class, "getBestProvider", Criteria.class, boolean.class, XC_MethodReplacement.returnConstant("gps"));
        hookMethod(LocationManager.class, "isProviderEnabled", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "isProviderEnabled: " + param.args[0]);
                if ("gps".equals(param.args[0]))
                    param.setResult(true);
            }
        });

        hookMethod(TelephonyManager.class, "getNeighboringCellInfo", XC_MethodReplacement.returnConstant(null));

        int ac = (int) lac;
        int ci = cid > Integer.MAX_VALUE ? -1 : (int) cid;

        hookMethod(GsmCellLocation.class, "getLac", XC_MethodReplacement.returnConstant(ac));
        hookMethod(GsmCellLocation.class, "getCid", XC_MethodReplacement.returnConstant(ci));

        // 2G
        hookMethod(CellIdentityGsm.class, "getLac", XC_MethodReplacement.returnConstant(ac));
        hookMethod(CellIdentityGsm.class, "getCid", XC_MethodReplacement.returnConstant(ci));

        // 3G
        hookMethod(CellIdentityWcdma.class, "getLac", XC_MethodReplacement.returnConstant(ac));
        hookMethod(CellIdentityWcdma.class, "getCid", XC_MethodReplacement.returnConstant(ci));

        // 4G
        hookMethod(CellIdentityLte.class, "getTac", XC_MethodReplacement.returnConstant(ac));
        hookMethod(CellIdentityLte.class, "getCi", XC_MethodReplacement.returnConstant(ci));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 3G
            hookMethod(CellIdentityTdscdma.class, "getLac", XC_MethodReplacement.returnConstant(ac));
            hookMethod(CellIdentityTdscdma.class, "getCid", XC_MethodReplacement.returnConstant(ci));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 5G
            hookMethod(CellIdentityNr.class, "getTac", XC_MethodReplacement.returnConstant(ac));
            hookMethod(CellIdentityNr.class, "getNci", XC_MethodReplacement.returnConstant(cid));
        }
    }

    //不带参数的方法拦截
    private static void hookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            Log.d(TAG, e.toString());
        }
    }

    //不带参数的方法拦截
    private static void hookMethod(String className, ClassLoader classLoader, String methodName,
                                   Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            Log.d(TAG, e.toString());
        }
    }

    //带参数的方法拦截
    private static void hookMethods(String className, String methodName, XC_MethodHook xmh) {
        try {
            Class<?> clazz = Class.forName(className);

            for (Method method : clazz.getDeclaredMethods())
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, xmh);
                }
        } catch (Throwable e) {
            Log.d(TAG, e.toString());
        }
    }

}
