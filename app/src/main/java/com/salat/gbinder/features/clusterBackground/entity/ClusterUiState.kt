package com.salat.gbinder.features.clusterBackground.entity

import android.graphics.Bitmap
import com.salat.gbinder.R
import com.salat.gbinder.features.clusterBackground.domain.QnxEnvProbe

enum class ClusterMode {
    IDLE,
    BUSY,
    SLOT_PICKER
}

enum class ClusterConnStatus {
    CHECKING,
    READY,
    NO_SHELL,
    UNREACHABLE
}

// One replaceable background theme slot with a preview rendered from the actual archive
data class SlotPreview(
    val index: Int,
    val width: Int,
    val height: Int,
    val hasAlpha: Boolean,
    val length: Int,
    val thumbnail: Bitmap?
)

data class ClusterUiState(
    val mode: ClusterMode = ClusterMode.IDLE,
    val probe: QnxEnvProbe? = null,
    val status: ClusterConnStatus = ClusterConnStatus.CHECKING,
    val busyTitleRes: Int = R.string.cluster_bg_busy_generic,
    val progress: Float = 0f,
    val progressLabel: String = "",
    val slots: List<SlotPreview> = emptyList()
) {
    val canBackup: Boolean get() = probe?.canBackup == true
    val canWrite: Boolean get() = probe?.canWrite == true
}

sealed interface ClusterEffect {
    data class Toast(val res: Int) : ClusterEffect
    data class ShareBackup(val bytes: ByteArray, val fileName: String) : ClusterEffect
}
