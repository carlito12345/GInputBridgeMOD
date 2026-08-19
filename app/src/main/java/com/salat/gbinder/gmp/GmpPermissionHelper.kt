package com.salat.gbinder.gmp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * GMP 权限引导 - 自动检测并引导通知监听权限
 * 通知监听是 MediaSessionPipeline 捕获第三方 App 播放的前提
 */
object GmpPermissionHelper {
    private const val TAG = "GmpPermission"

    /**
     * 检测通知监听权限是否已授予
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        return try {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val me = ComponentName(context, MediaSessionNotificationListenerService::class.java)
            enabled.split(':').any { it.equals(me.flattenToString(), ignoreCase = true) }
        } catch (e: Exception) {
            Log.e(TAG, "check notification access failed", e)
            false
        }
    }

    /**
     * 跳转到通知监听设置页
     */
    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "open notification settings failed", e)
        }
    }

    /**
     * 自动引导: 若未授权通知监听, 自动跳转设置页
     * @return true=已授权或无需引导, false=已跳转设置页等待授权
     */
    fun autoGuideNotificationAccess(context: Context): Boolean {
        if (isNotificationAccessGranted(context)) return true
        openNotificationAccessSettings(context)
        return false
    }
}
