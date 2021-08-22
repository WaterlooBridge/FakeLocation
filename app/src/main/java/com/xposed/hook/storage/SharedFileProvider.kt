package com.xposed.hook.storage

import android.content.Context
import android.content.pm.ProviderInfo
import androidx.core.content.FileProvider

/**
 * Created by lin on 2021/8/21.
 */
class SharedFileProvider : FileProvider() {

    override fun attachInfo(context: Context, info: ProviderInfo) {
        try {
            super.attachInfo(context, info)
        } catch (ignored: SecurityException) {
            val method = FileProvider::class.java.getDeclaredMethod(
                "getPathStrategy",
                Context::class.java,
                String::class.java
            )
            method.isAccessible = true
            val field = FileProvider::class.java.getDeclaredField("mStrategy")
            field.isAccessible = true
            field.set(this, method.invoke(null, context, info.authority.split(";")[0]))
        }
    }
}