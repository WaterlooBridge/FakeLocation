package com.xposed.hook.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin on 2018/1/24.
 */

public class PkgConfig {

    public static final List<String> packages = new ArrayList<>();
    public static final String pkg_dingtalk = "com.alibaba.android.rimet";
    public static final String pkg_wechat = "com.tencent.mm";

    static {
        packages.add("com.autonavi.minimap");
        packages.add("com.team.club");
        packages.add("com.baidu.BaiduMap");
        packages.add("com.tencent.mobileqq");
        packages.add("com.sina.weibo");
    }
}
