package com.xposed.hook.location

import kotlin.math.pow
import kotlin.random.Random

/**
 * Created by lin on 2022/8/21.
 */
object LocationConfig {

    @JvmStatic
    var latitude: Double = 0.0
        get() {
            return field + (Random.Default.nextInt(1000) - 500) / 10.0.pow(8.0)
        }

    @JvmStatic
    var longitude: Double = 0.0
        get() {
            return field + (Random.Default.nextInt(1000) - 500) / 10.0.pow(8.0)
        }
}
