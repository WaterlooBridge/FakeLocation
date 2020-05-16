package com.xposed.hook.wechat;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by lin on 2018/2/6.
 */

public class WechatUnrecalledHook {

    private static final int EXEC_SUC = 1;

    static final String SQLiteDatabaseClass = "com.tencent.wcdb.database.SQLiteDatabase";

    private static final String recallClass = LuckyMoneyHook.WECHAT_PACKAGE_NAME + ".sdk.platformtools.bw";
    private static final String recallMethod = "S";
    private static final String storageClass = LuckyMoneyHook.WECHAT_PACKAGE_NAME + ".storage.w";
    private static final String storageMethodParam = LuckyMoneyHook.WECHAT_PACKAGE_NAME + ".sdk.e.e";
    private static final String incMsgLocalIdClass = LuckyMoneyHook.WECHAT_PACKAGE_NAME + ".storage.bl";
    private static final String incMsgLocalIdMethod = "aHd";
    private static final String updateMsgLocalIdMethod = "ao";
    private static final String updateMsgLocalIdMethodParam = LuckyMoneyHook.WECHAT_PACKAGE_NAME + ".storage.bk";

    private static final boolean mDebug = true;
    private WechatMainDBHelper mDb;
    private Object mObject;
    private Object updateMsgLocalIdMethodParamObj;

    private Map<String, Boolean> mSettings = new HashMap<>();

    WechatUnrecalledHook(String packageName) {
        mSettings.put("prevent_moments_recall", true);
        mSettings.put("prevent_comments_recall", true);
    }

    private static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameters) {
        Class<?> cls = findClass(className, classLoader);
        Class<?>[] parameterTypes = new Class[parameters.length - 1];
        for (int i = 0; i < parameters.length - 1; i++) {
            if (parameters[i] instanceof String) {
                parameterTypes[i] = findClass((String) parameters[i], classLoader);
            } else if (parameters[i] instanceof Class) {
                parameterTypes[i] = (Class<?>) parameters[i];
            }
        }
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            XC_MethodHook callback = (XC_MethodHook) parameters[parameters.length - 1];
            XposedBridge.hookMethod(constructor, callback);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public void hook(final ClassLoader loader) {
        try {
            hookRecall(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookDatabase(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookDbObject(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            hookMsgLocalId(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private void hookRecall(final ClassLoader loader) {
        findAndHookMethod(recallClass, loader,
                recallMethod, String.class, String.class,
                new XC_MethodHook() {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        preventMsgRecall(param);
                    }
                });
    }

    private void hookDatabase(ClassLoader loader) {
        findAndHookMethod(SQLiteDatabaseClass, loader,
                "updateWithOnConflict", String.class, ContentValues.class, String.class,
                String[].class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        preventCommentRecall(param);
                        preventMomentRecall(param);
                    }
                });

        findAndHookMethod(SQLiteDatabaseClass, loader,
                "executeSql", String.class, Object[].class, "com.tencent.wcdb.support.CancellationSignal", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String query = (String) param.args[0];
                        if (mSettings.get("prevent_moments_recall") &&
                                query.toLowerCase().contains("snsinfo set sourcetype")) {
                            XposedBridge.log("preventMomentRecall executeSql");
                            param.setResult(EXEC_SUC);
                        }
                    }
                });

    }

    static void hook3DaysMoments(ClassLoader loader) {
        findAndHookMethod(SQLiteDatabaseClass, loader, "rawQueryWithFactory",
                SQLiteDatabaseClass + ".CursorFactory", String.class, Object[].class, String.class, "com.tencent.wcdb.support.CancellationSignal",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e("rawQueryWithFactory", param.args[1] + ":" + param.args[3]);
                        String sourceType = "sourceType in (8,72,10,74,12,76,14,78,24,88,26,90,28,92,30,94)";
                        String type = "type in ( 1,2 , 3 , 4 , 18 , 5 , 12 , 9 , 14 , 15 , 13 , 21 , 25 , 26,28,29,30)";
                        if (param.args[1] != null && param.args[1].toString().contains("from SnsInfo") &&
                                param.args[1].toString().contains(sourceType) &&
                                param.args[1].toString().contains(type)) {
                            param.args[1] = param.args[1].toString().replace(sourceType, "1=1")
                                    .replace(type, "1=1")
                                    .replace("snsId >=", "0 !=");
                        }
                    }

                });
    }

    private void hookDbObject(final ClassLoader loader) {
        // get database object
        findAndHookConstructor(storageClass, loader,
                storageMethodParam, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // look for: LinkedBlockingQueue
                        if (mDb == null) {
                            try {
                                mDb = new WechatMainDBHelper(param.args[0]);
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    }
                });
    }

    private void hookMsgLocalId(ClassLoader loader) {
        findAndHookMethod(incMsgLocalIdClass, loader, incMsgLocalIdMethod, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ("message".equals(param.args[0]))
                    mObject = param.getResult();
            }
        });
        try {
            Class cls = XposedHelpers.findClass(updateMsgLocalIdMethodParam, loader);
            updateMsgLocalIdMethodParamObj = cls.newInstance();
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private void preventMsgRecall(XC_MethodHook.MethodHookParam param) {
        String xml = (String) param.args[0];
        String tag = (String) param.args[1];
        if (TextUtils.isEmpty(xml) || TextUtils.isEmpty(tag) ||
                !tag.equals("sysmsg") || !xml.contains("revokemsg")) {
            return;
        }

        @SuppressWarnings("unchecked") Map<String, String> map =
                (Map<String, String>) param.getResult();
        if (map == null)
            return;

        String key = ".sysmsg.$type";
        if (!map.containsKey(key))
            return;

        String type = map.get(key);
        if (type == null || !type.equals("revokemsg"))
            return;

        final String talker = map.get(".sysmsg.revokemsg.session");
        String replacemsg = map.get(".sysmsg.revokemsg.replacemsg");
        String msgsvrid = map.get(".sysmsg.revokemsg.newmsgid");

        if (replacemsg.startsWith("你") || replacemsg.toLowerCase().startsWith("you")) {
            return;
        }

        String[] strings = replacemsg.split("\"");
        replacemsg = "\"" + strings[1] + "\" " + "尝试撤回上一条消息 （已阻止)";

        map.put(key, null);
        param.setResult(map);

        try {
            Cursor cursor = mDb.getMessageBySvrId(msgsvrid);
            if (cursor == null || !cursor.moveToFirst())
                return;

            long createTime = cursor.getLong(cursor.getColumnIndex("createTime"));
            int idx = cursor.getColumnIndex("talkerId");
            int talkerId = -1;
            if (idx != -1) {
                talkerId = cursor.getInt(cursor.getColumnIndex("talkerId"));
            }
            cursor.close();
            mDb.insertSystemMessage(talker, talkerId, replacemsg, createTime + 1);
            updateMessageCount();
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

    }

    private void updateMessageCount() {
        if (mObject != null) {
            XposedHelpers.callMethod(mObject, updateMsgLocalIdMethod, updateMsgLocalIdMethodParamObj);
            XposedBridge.log("updateMessageCount");
        }
    }

    private void preventCommentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snscomment"))
            return;

        ContentValues v = (ContentValues) param.args[1];
        if (v.containsKey("commentflag") && v.getAsInteger("commentflag") == 1 &&
                mSettings.get("prevent_comments_recall")) {
            XposedBridge.log("preventCommentRecall");
            param.setResult(EXEC_SUC); // prevent call
        }
    }

    private void preventMomentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snsinfo"))
            return;

        ContentValues v = (ContentValues) param.args[1];
        if (mSettings.get("prevent_moments_recall") &&
                v.containsKey("sourceType") && v.containsKey("type")) {
            int sourceType = v.getAsInteger("sourceType");
            int type = v.getAsInteger("type");
            //type: 2: text, 21 luckymoneyphoto,
            if (sourceType == 0 || (type != 2 && sourceType == 8/*set to private*/)) {
                XposedBridge.log("preventMomentRecall");
                param.setResult(EXEC_SUC); // prevent call
            }
        }
    }

    private void log(Throwable t) {
        if (mDebug) {
            XposedBridge.log(t);
        }
    }

}
