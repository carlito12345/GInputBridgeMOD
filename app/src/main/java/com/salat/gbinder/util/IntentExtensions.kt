package com.salat.gbinder.util

import android.content.Intent
import kotlin.math.roundToInt

fun Intent.getSafeInt(name: String): Int {
    // 先检查 extra 实际类型,避免触发 Bundle 类型检查异常/警告
    return when (val raw = getExtras()?.get(name)) {
        is Int -> raw
        is Long -> raw.toInt()
        is Double -> raw.roundToInt()
        is Float -> raw.roundToInt()
        is String -> raw.trim().toIntOrNull() ?: 0
        else -> 0
    }
}

fun Intent.getSafeFloat(name: String): Float {
    // 先检查 extra 实际类型,避免触发 Bundle 类型检查异常/警告
    return when (val raw = getExtras()?.get(name)) {
        is Float -> raw
        is Int -> raw.toFloat()
        is Long -> raw.toFloat()
        is Double -> raw.toFloat()
        is String -> raw.trim().replace(',', '.').toFloatOrNull() ?: 0f
        else -> 0f
    }
}
