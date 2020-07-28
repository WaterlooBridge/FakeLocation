package com.xposed.hook.location;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by lin on 2018/1/25.
 */

public class PhoneStateListenerDelegate {

    private static final String TAG = "PhoneStateListener";

    private static List<String> hookedClass = new ArrayList<>();

    public static void hookPhoneStateListener(int lac, int cid) {
        try {
            Constructor<PhoneStateListener> constructor = PhoneStateListener.class.getConstructor();
            XposedBridge.hookMethod(constructor, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class<?> clazz = param.thisObject.getClass();
                    while (clazz != null && clazz != PhoneStateListener.class) {
                        if (hookedClass.contains(clazz.getName()))
                            break;
                        try {
                            Method method = XposedHelpers.findMethodExact(clazz, "onCellLocationChanged", CellLocation.class);
                            hookPhoneStateListener(method, lac, cid);
                            hookedClass.add(clazz.getName());
                            break;
                        } catch (Throwable e) {
                            Log.i(TAG, e.toString());
                        }
                        clazz = clazz.getSuperclass();
                    }
                }
            });
        } catch (Throwable e) {
            Log.w(TAG, e.toString());
        }
    }

    private static void hookPhoneStateListener(Method method, int lac, int cid) {
        try {
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] instanceof GsmCellLocation) {
                        Log.i(TAG, "hooking onCellLocationChanged");
                        ((GsmCellLocation) param.args[0]).setLacAndCid(lac, cid);
                    }
                }
            });
        } catch (Throwable e) {
            Log.w(TAG, e.toString());
        }
    }
}
