package com.xposed.hook.entity;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AppInfo implements Serializable {

    public String title;
    public String packageName;
    public long time;
    public transient Drawable icon;
}
