package com.xposed.hook.location;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.xposed.hook.config.Constants;

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

        hookMethod("android.net.wifi.WifiManager", mLpp.classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(Collections.emptyList());
            }
        });

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
                Location l = (Location) param.getResult();
                if (l != null) {
                    LocationHandler.updateLocation(l, LocationConfig.getLatitude(), LocationConfig.getLongitude());
                    param.setResult(l);
                } else
                    param.setResult(LocationHandler.createLocation(LocationConfig.getLatitude(), LocationConfig.getLongitude()));
            }
        });

        hookMethod("android.net.wifi.WifiInfo", mLpp.classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-E0");
            }
        });

        hookMethod("android.net.wifi.WifiInfo", mLpp.classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });

        hookMethod("android.net.wifi.WifiInfo", mLpp.classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00:00:00:00:00:00");
            }
        });

        hookMethods("android.telephony.TelephonyManager", "getSimState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (lac == Constants.DEFAULT_LAC && cid == Constants.DEFAULT_CID)
                    param.setResult(0);
            }
        });

        hookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (lac == Constants.DEFAULT_LAC && cid == Constants.DEFAULT_CID)
                    param.setResult(null);
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

        hookMethod(GsmCellLocation.class, "getLac", XC_MethodReplacement.returnConstant((int) lac));
        hookMethod(GsmCellLocation.class, "getCid", XC_MethodReplacement.returnConstant((int) cid));

        hookMethod(CellIdentityGsm.class, "getLac", XC_MethodReplacement.returnConstant((int) lac));
        hookMethod(CellIdentityGsm.class, "getCid", XC_MethodReplacement.returnConstant((int) cid));

        hookMethod(CellIdentityLte.class, "getTac", XC_MethodReplacement.returnConstant((int) lac));
        hookMethod(CellIdentityLte.class, "getCi", XC_MethodReplacement.returnConstant((int) cid));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hookMethod(CellIdentityNr.class, "getTac", XC_MethodReplacement.returnConstant((int) lac));
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
