package com.salat.gbinder.gmp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * GMP 开机自启 - 系统启动后自动拉起在线音乐服务
 */
class GmpBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.i("GMP", "Boot completed, auto-starting OnlineMusicService")
        try {
            val serviceIntent = Intent(context, OnlineMusicService::class.java)
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.e("GMP", "Boot auto-start failed", e)
        }
    }
}
