package com.xposed.hook

import android.app.Application

/**
 * Created by lin on 2021/8/7.
 */
class App : Application() {

    companion object {
        lateinit var current: Application
    }

    override fun onCreate() {
        super.onCreate()
        current = this
    }
}