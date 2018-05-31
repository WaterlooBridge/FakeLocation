package com.xposed.hook;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.xposed.hook.wechat.WechatUnrecalledHook;

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

    public static final String luckyMoneyReceiveUI = WECHAT_PACKAGE_NAME + ".plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    public static final String receiveUIFunctionName = "d";
    public static final String receiveUIParamName = "com.tencent.mm.ab.l";

    public static ToastHandler handler;

    private static long msgId;

    public static void hook(final XC_LoadPackage.LoadPackageParam mLpp) {
        if (WECHAT_PACKAGE_NAME.equals(mLpp.packageName)) {
            XSharedPreferences preferences = new XSharedPreferences("com.xposed.hook", "lucky_money");
            try {
                XposedHelpers.findAndHookMethod("android.app.Application", mLpp.classLoader, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];
                        handler = new ToastHandler(context);
                    }
                });
                if (preferences.getBoolean("quick_open", false))
                    XposedHelpers.findAndHookMethod(luckyMoneyReceiveUI, mLpp.classLoader, receiveUIFunctionName, int.class, int.class, String.class, receiveUIParamName, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Button button = (Button) XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), Button.class).get(param.thisObject);
                                if (button.isShown() && button.isClickable()) {
                                    button.performClick();
                                }
                            } catch (Exception e) {
                                Log.e(HookUtils.TAG, e.toString());
                            }
                        }
                    });
                if (preferences.getBoolean("auto_receive", false))
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
                            if (handler != null && (type == 436207665 || type == 469762097))
                                handler.sendEmptyMessage(0);
                        }
                    });
            } catch (Exception e) {
                XposedBridge.log(e);
            }
            if (preferences.getBoolean("recalled", false))
                new WechatUnrecalledHook(WECHAT_PACKAGE_NAME).hook(mLpp.classLoader);
        }
    }

    private static class ToastHandler extends Handler {

        private Context context;

        public ToastHandler(Context context) {
            super(Looper.getMainLooper());
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(context, "Lucky Money is Coming", Toast.LENGTH_SHORT).show();
        }
    }
}
