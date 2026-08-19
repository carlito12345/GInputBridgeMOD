package com.salat.gbinder.receiver

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ecarx.xui.adaptapi.diminteraction.DimInteraction
import com.ecarx.xui.adaptapi.diminteraction.IMediaInteraction
import com.salat.gbinder.entity.HudTrackInfo

/**
 * 通过 ecarx DimInteraction API 将媒体信息推送到车机仪表盘
 * (与 GMH 相同机制, 不依赖独立 GMH App)
 */
object DimDashboardPusher {
    private const val TAG = "DimPusher"
    private var mDimInteraction: DimInteraction? = null
    private var mMediaInteraction: IMediaInteraction? = null

    fun init(context: Context) {
        try {
            mDimInteraction = DimInteraction.create(context.applicationContext)
            mMediaInteraction = mDimInteraction?.mediaInteraction
            Log.i(TAG, "DimInteraction initialized, mediaInteraction=${mMediaInteraction != null}")
        } catch (e: Exception) {
            Log.e(TAG, "DimInteraction init failed: ${e.message}")
        }
    }

    fun pushTrackInfo(info: HudTrackInfo, context: Context? = null, coverBitmap: android.graphics.Bitmap? = null) {
        val mi = mMediaInteraction ?: return
        // 1. 优先用播放器已下载的封面 Bitmap(存本地文件)
        if (coverBitmap != null && context != null) {
            Thread {
                val localCover = CoverDownloader.saveBitmap(coverBitmap, context)
                val coverUri = localCover?.let { android.net.Uri.parse(it) }
                doPush(mi, info, coverUri)
            }.start()
            return
        }
        // 2. 封面URL是HTTP时, 下载到本地
        val coverForPush = info.cover?.toString()
        if (coverForPush != null && (coverForPush.startsWith("http://") || coverForPush.startsWith("https://")) && context != null) {
            Thread {
                val localCover = CoverDownloader.download(coverForPush, context)
                val coverUri = localCover?.let { android.net.Uri.parse(it) }
                doPush(mi, info, coverUri)
            }.start()
            return
        }
        // 3. 本地 URI 直接推送
        doPush(mi, info, info.cover)
    }

    private fun doPush(mi: IMediaInteraction, info: HudTrackInfo, coverUri: Uri?) {
        try {
            mi.updatePlaybackInfo(object : IMediaInteraction.IPlaybackInfo {
                override fun getAlbum(): String = info.album
                override fun getArtist(): String = info.artist
                override fun getArtwork(): Uri? = coverUri
                override fun getCurrentLyricSentence(): String = ""
                override fun getDuration(): Long = info.maxProgress.takeIf { it > 0 } ?: -1L
                override fun getFavoriteState(): Int = 1
                override fun getLoopMode(): Int = 0
                override fun getLyric(): Uri? = null
                override fun getLyricContent(): String = ""
                override fun getMediaPath(): Uri? = null
                override fun getNextArtwork(): Uri? = coverUri
                override fun getPlaybackStatus(): Int = if (info.isPlaying) {
                    IMediaInteraction.IPlaybackInfo.PLAYBACK_STATUS_PLAYING
                } else IMediaInteraction.IPlaybackInfo.PLAYBACK_STATUS_PAUSED
                override fun getPlayingItemPositionInQueue(): Int = 0
                override fun getPreviousArtwork(): Uri? = null
                override fun getRadioFrequency(): String = ""
                override fun getRadioMode(): Int = IMediaInteraction.IPlaybackInfo.RADIO_MODE_PLAYING
                override fun getRadioStationName(): String = ""
                override fun getSourceType(): Int = info.sourceType
                override fun getTitle(): String = info.title
                override fun getUUID(): String = ""
            })
            mi.updateCurrentProgress(info.progress.coerceAtLeast(0L))
            Log.i(TAG, "Pushed to dashboard: ${info.title} - ${info.artist} cover=${info.cover != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Push failed: ${e.message}")
        }
    }

    fun clear() {
        try {
            mMediaInteraction?.updatePlaybackInfo(object : IMediaInteraction.IPlaybackInfo {
                override fun getAlbum(): String = ""
                override fun getArtist(): String = ""
                override fun getArtwork(): Uri? = null
                override fun getCurrentLyricSentence(): String = ""
                override fun getDuration(): Long = 0L
                override fun getFavoriteState(): Int = 1
                override fun getLoopMode(): Int = 0
                override fun getLyric(): Uri? = null
                override fun getLyricContent(): String = ""
                override fun getMediaPath(): Uri? = null
                override fun getNextArtwork(): Uri? = null
                override fun getPlaybackStatus(): Int = IMediaInteraction.IPlaybackInfo.PLAYBACK_STATUS_PAUSED
                override fun getPlayingItemPositionInQueue(): Int = 0
                override fun getPreviousArtwork(): Uri? = null
                override fun getRadioFrequency(): String = ""
                override fun getRadioMode(): Int = IMediaInteraction.IPlaybackInfo.RADIO_MODE_PLAYING
                override fun getRadioStationName(): String = ""
                override fun getSourceType(): Int = 0
                override fun getTitle(): String = ""
                override fun getUUID(): String = ""
            })
        } catch (_: Exception) {
        }
    }
}
