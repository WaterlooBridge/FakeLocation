package com.xposed.hook.utils

import android.content.SharedPreferences
import com.xposed.hook.config.Constants

/**
 * Created by lin on 2023/6/19.
 */
object CellLocationHelper {

    fun getLac(preferences: SharedPreferences, prefix: String): Long {
        return try {
            preferences.getLong(prefix + "lac", Constants.DEFAULT_LAC)
        } catch (e: Throwable) {
            preferences.getInt(prefix + "lac", Constants.DEFAULT_LAC.toInt()).toLong()
        }
    }

    fun getCid(preferences: SharedPreferences, prefix: String): Long {
        return try {
            preferences.getLong(prefix + "cid", Constants.DEFAULT_CID)
        } catch (e: Throwable) {
            preferences.getInt(prefix + "cid", Constants.DEFAULT_CID.toInt()).toLong()
        }
    }
}