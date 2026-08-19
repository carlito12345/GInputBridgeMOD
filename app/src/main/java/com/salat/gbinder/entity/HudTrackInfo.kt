package com.salat.gbinder.entity

import android.net.Uri

/**
 * Track information for GMH dashboard display
 */
data class HudTrackInfo(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val cover: Uri? = null,
    val isPlaying: Boolean = false,
    val sourceType: Int = -1,
    val progress: Long = -1L,
    val maxProgress: Long = -1L,
)
