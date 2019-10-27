package com.xposed.hook.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.xposed.hook.config.PkgConfig;
import com.xposed.hook.entity.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {

    public static ArrayList<AppInfo> getAppList(Context context) {
        ArrayList<AppInfo> apps = new ArrayList<>();
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo installedPackage : installedPackages) {
            if ((installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppInfo app = new AppInfo();
                app.packageName = installedPackage.packageName;
                app.title = installedPackage.applicationInfo.loadLabel(context.getPackageManager()).toString();
                app.icon = installedPackage.applicationInfo.loadIcon(context.getPackageManager());
                if (PkgConfig.pkg_dingtalk.equals(app.packageName))
                    apps.add(0, app);
                else
                    apps.add(app);
            }
        }

        return apps;
    }
}
