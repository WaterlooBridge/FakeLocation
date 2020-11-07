package com.xposed.hook.location;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lin on 2017/7/23.
 */

public class LocationHook {

    public static String TAG = "LocationHook";

    public static void hookAndChange(XC_LoadPackage.LoadPackageParam mLpp, final double latitude, final double longitude, final int lac, final int cid) {

        Log.e(TAG, "Avalon Hook Location Test: " + mLpp.packageName);
        LocationHandler.latitude = latitude;
        LocationHandler.longitude = longitude;

        hookMethod("android.content.ContextWrapper", mLpp.classLoader, "getApplicationContext", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LocationHandler.getInstance().attach((Context) param.getResult());
            }
        });

        hookMethod("android.net.wifi.WifiManager", mLpp.classLoader, "getScanResults",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        hookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getCellLocation",
                new XC_MethodHook() {
                    /**
                     * android.telephony.TelephonyManager的getCellLocation方法
                     * Returns the current location of the device.
                     * Return null if current location is not available.
                     */
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        hookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getNeighboringCellInfo",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        param.setResult(null);
                    }
                });

        hookMethods("android.location.LocationManager", "requestLocationUpdates",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        LocationHandler.getInstance().start();
                    }
                });

        hookMethod("android.location.LocationManager", mLpp.classLoader, "getLastLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location l = (Location) param.getResult();
                if (l != null) {
                    LocationHandler.updateLocation(l, latitude, longitude);
                    param.setResult(l);
                } else
                    param.setResult(LocationHandler.createLocation(latitude, longitude));
            }
        });

        hookMethods("android.location.LocationManager", "getLastKnownLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location l = (Location) param.getResult();
                if (l != null) {
                    LocationHandler.updateLocation(l, latitude, longitude);
                    param.setResult(l);
                } else
                    param.setResult(LocationHandler.createLocation(latitude, longitude));
            }
        });

        hookMethod("android.location.Location", mLpp.classLoader, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(latitude);
            }
        });

        hookMethod("android.location.Location", mLpp.classLoader, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(longitude);
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
                Log.e(TAG, "getSimState");
                param.setResult(0);
            }
        });

        hookMethods("android.location.LocationManager", "getBestProvider", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e(TAG, "getBestProvider");
                param.setResult("gps");
            }
        });

        hookMethods("android.location.LocationManager", "getProviders", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e(TAG, "getProviders");
            }
        });

        hookMethods("android.location.LocationManager", "isProviderEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e(TAG, "isProviderEnabled: " + param.args[0]);
                if ("gps".equals(param.args[0]))
                    param.setResult(true);
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            hookMethod("android.telephony.TelephonyManager", mLpp.classLoader,
                    "getAllCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.e(TAG, "getAllCellInfo");
                            param.setResult(null);
                        }
                    });
        }

        PhoneStateListenerDelegate.hookPhoneStateListener(lac, cid);
    }

    //不带参数的方法拦截
    private static void hookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    //不带参数的方法拦截
    private static void hookMethod(String className, ClassLoader classLoader, String methodName,
                                   Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
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
            Log.e(TAG, e.toString());
        }
    }

}
