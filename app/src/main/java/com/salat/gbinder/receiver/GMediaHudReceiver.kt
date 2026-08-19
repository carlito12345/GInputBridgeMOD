package com.salat.gbinder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.salat.gbinder.entity.GMediaSource
import com.salat.gbinder.entity.HudTrackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * BroadcastReceiver for GMH dashboard display integration
 * Listens to com.salat.gmediahud.* broadcasts
 */
class GMediaHudReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GMediaHudReceiver"
        private const val BASE_PATH = "com.salat.gmediahud"

        private val scope = CoroutineScope(Dispatchers.Main + Job())

        // SharedFlow to emit track info updates for Compose UI
        private val _trackEventFlow = MutableSharedFlow<HudTrackInfo>(extraBufferCapacity = 1)
        val trackEvents = _trackEventFlow.asSharedFlow()
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received HUD intent: ${intent.action}")

        when (intent.action) {
            "$BASE_PATH.SHOW" -> handleShowIntent(context, intent)
            "$BASE_PATH.HIDE" -> handleHideIntent(context, intent)
            "$BASE_PATH.UPDATE_AUDIO_SOURCE" -> handleAudioSourceChange(context, intent)
        }
    }

    private fun handleShowIntent(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: ""
        val subtitle = intent.getStringExtra("subtitle") ?: ""
        val art = intent.getStringExtra("art") ?: ""
        val duration = intent.getIntExtra("duration", 3).toLong()
        val params = parseParams(intent.getStringExtra("params"))

        val sourceType = params["source"]?.toIntOrNull() ?: GMediaSource.ONLINE

        val trackInfo = HudTrackInfo(
            title = title,
            artist = subtitle,
            album = "",
            cover = if (art.isNotEmpty()) android.net.Uri.parse(art) else null,
            isPlaying = params["pause"] != "1",
            sourceType = sourceType,
            progress = params["progress"]?.toLong() ?: 0,
            maxProgress = params["max_progress"]?.toLong() ?: duration * 1000
        )

        Log.d(TAG, "Processing SHOW: $title - $subtitle")

        // 推送车机仪表盘(ecarx DimInteraction)
        DimDashboardPusher.pushTrackInfo(trackInfo, context)

        scope.launch {
            _trackEventFlow.emit(trackInfo)
        }
    }

    private fun handleHideIntent(context: Context, intent: Intent) {
        val tag = intent.getStringExtra("tag") ?: ""
        Log.d(TAG, "HUD hide requested: $tag")
    }

    private fun handleAudioSourceChange(context: Context, intent: Intent) {
        val source = intent.getStringExtra("source") ?: ""
        Log.i(TAG, "Audio source changed to: $source")
    }

    private fun parseParams(params: String?): Map<String, String> {
        if (params.isNullOrBlank()) return emptyMap()

        return params.split(",")
            .map { it.trim() }
            .filter { it.contains("=") }
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) Pair(parts[0], parts[1]) else null
            }
            .toMap()
    }
}
