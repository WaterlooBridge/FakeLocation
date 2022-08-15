package com.xposed.hook.utils

import android.content.SharedPreferences
import java.io.File

/**
 * Created by lin on 2022/8/15.
 */
object SharedPreferencesHelper {

    fun makeWorldReadable(sp: SharedPreferences) {
        try {
            val field = sp.javaClass.getDeclaredField("mFile")
            field.isAccessible = true
            val file = field.get(sp) as File
            file.setReadable(true, false)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
