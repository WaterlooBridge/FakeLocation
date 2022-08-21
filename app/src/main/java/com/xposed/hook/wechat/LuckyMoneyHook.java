package com.xposed.hook.wechat;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.xposed.hook.BuildConfig;
import com.xposed.hook.location.LocationHook;
import com.xposed.hook.utils.XmlToJson;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lin on 2018/2/2.
 */

public class LuckyMoneyHook {

    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private static final String tinkerEnableClass = "com.tencent.tinker.loader.shareutil.ShareTinkerInternals";
    private static final String tinkerEnableMethodName = "isTinkerEnabled";

    private static final String luckyMoneyReceiveUI = WECHAT_PACKAGE_NAME + ".plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
    private static final String receiveUIFunctionName = "onSceneEnd";

    private static final String chatRoomInfoUI = WECHAT_PACKAGE_NAME + ".chatroom.ui.ChatroomInfoUI";
    private static final String launcherUI = WECHAT_PACKAGE_NAME + ".ui.LauncherUI";
    private static final String openUIClass = WECHAT_PACKAGE_NAME + ".by.d";//MicroMsg.PluginHelper
    private static final String openUIMethodName = "b";

    private static final HashSet<String> autoReceiveIds = new HashSet<>();
    private static WeakReference<Activity> launcherUiActivity;

    private static ToastHandler handler;

    private static long msgId;
    private static int delay;

    public static void hook(final XC_LoadPackage.LoadPackageParam mLpp) {
        if (!WECHAT_PACKAGE_NAME.equals(mLpp.processName) || !WECHAT_PACKAGE_NAME.equals(mLpp.packageName))
            return;
        XSharedPreferences preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, "lucky_money");
        delay = preferences.getInt("lucky_money_delay", 0);
        try {
            if (preferences.getBoolean("quick_open", false))
                hookLuckyMoneyReceiveUI(mLpp);
            if (preferences.getBoolean("auto_receive", false)) {
                XposedHelpers.findAndHookMethod(WechatUnrecalledHook.SQLiteDatabaseClass, mLpp.classLoader, "insert", String.class, String.class, ContentValues.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ContentValues contentValues = (ContentValues) param.args[2];
                        String tableName = (String) param.args[0];
                        if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
                            return;
                        }
                        Integer type = contentValues.getAsInteger("type");
                        if (null == type) {
                            return;
                        }
                        Long id = contentValues.getAsLong("msgId");
                        if (id != null) {
                            if (id == msgId)
                                XposedBridge.log("wechat msg:" + contentValues.getAsString("content"));
                            msgId = id;
                        }
                        if (handler != null && (type == 436207665 || type == 469762097)) {
                            handler.obtainMessage(0, "Lucky Money is Coming").sendToTarget();
                            openLuckyMoneyReceiveUI(contentValues, mLpp);
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(chatRoomInfoUI, mLpp.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (handler != null) {
                            Activity activity = (Activity) param.thisObject;
                            String wechatId = activity.getIntent().getStringExtra("RoomInfo_Id");
                            String status = "Opened";
                            if (autoReceiveIds.contains(wechatId)) {
                                autoReceiveIds.remove(wechatId);
                                status = "Closed";
                            } else
                                autoReceiveIds.add(wechatId);
                            handler.obtainMessage(0, "Group Chat ID:" + wechatId + ",Auto Open LuckyMoneyReceiveUI " + status).sendToTarget();
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(launcherUI, mLpp.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        launcherUiActivity = new WeakReference<>((Activity) param.thisObject);
                        if (handler == null)
                            handler = new ToastHandler(launcherUiActivity.get().getApplication());
                    }
                });
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        if (preferences.getBoolean("recalled", false))
            new WechatUnrecalledHook().hook(mLpp.classLoader);
        if (preferences.getBoolean("3_days_Moments", false))
            WechatUnrecalledHook.hook3DaysMoments(mLpp.classLoader);
    }

    private static void hookLuckyMoneyReceiveUI(XC_LoadPackage.LoadPackageParam mLpp) {
        Method[] methods = XposedHelpers.findClass(luckyMoneyReceiveUI, mLpp.classLoader).getDeclaredMethods();
        Method receiveUIMethod = null;
        for (Method method : methods) {
            if (receiveUIFunctionName.equals(method.getName())) {
                receiveUIMethod = method;
                break;
            }
        }
        if (receiveUIMethod == null)
            return;
        XposedBridge.hookMethod(receiveUIMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Button button = (Button) XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), Button.class).get(param.thisObject);
                    if (button.isShown() && button.isClickable()) {
                        button.performClick();
                    }
                } catch (Throwable e) {
                    Log.e(LocationHook.TAG, e.toString());
                }
            }
        });
    }

    private static void openLuckyMoneyReceiveUI(ContentValues contentValues, XC_LoadPackage.LoadPackageParam lpparam) {
        int status = contentValues.getAsInteger("status");
        if (status == 4)
            return;

        String talker = contentValues.getAsString("talker");
        if (!autoReceiveIds.contains(talker))
            return;

        String content = contentValues.getAsString("content");
        if (!content.startsWith("<msg"))
            content = content.substring(content.indexOf("<msg"));
        try {
            JSONObject wcpayinfo = new XmlToJson.Builder(content).build()
                    .getJSONObject("msg").getJSONObject("appmsg").getJSONObject("wcpayinfo");
            String nativeUrlString = wcpayinfo.getString("nativeurl");

            if (launcherUiActivity != null && launcherUiActivity.get() != null && handler != null) {
                handler.postDelayed(() -> {
                    try {
                        Intent param = new Intent();
                        param.putExtra("key_way", 1);
                        param.putExtra("key_native_url", nativeUrlString);
                        param.putExtra("key_username", talker);
                        XposedHelpers.callStaticMethod(XposedHelpers.findClass(openUIClass, lpparam.classLoader),
                                openUIMethodName, launcherUiActivity.get(), "luckymoney", ".ui.LuckyMoneyNotHookReceiveUI", param);
                    } catch (Throwable e) {
                        XposedBridge.log(e);
                    }
                }, delay);
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    public static void disableTinker(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!WECHAT_PACKAGE_NAME.equals(lpparam.packageName))
            return;
        try {
            XposedHelpers.findAndHookMethod(tinkerEnableClass, lpparam.classLoader, tinkerEnableMethodName, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static class ToastHandler extends Handler {

        private final Application context;

        ToastHandler(Application context) {
            super(Looper.getMainLooper());
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    }
}
