package com.xposed.hook.extension

import com.xposed.hook.App

/**
 * Created by lin on 2021/8/7.
 */

inline val Int.dpInPx: Int
    get() = (this * App.current.resources.displayMetrics.density + 0.5).toInt()