package com.salat.gbinder.features.clusterBackground

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.gbinder.R
import com.salat.gbinder.features.clusterBackground.data.ClusterImagePreparer
import com.salat.gbinder.features.clusterBackground.domain.QnxClusterException
import com.salat.gbinder.features.clusterBackground.domain.QnxClusterRepository
import com.salat.gbinder.features.clusterBackground.domain.QnxErrorCode
import com.salat.gbinder.features.clusterBackground.entity.ClusterConnStatus
import com.salat.gbinder.features.clusterBackground.entity.ClusterEffect
import com.salat.gbinder.features.clusterBackground.entity.ClusterMode
import com.salat.gbinder.features.clusterBackground.entity.ClusterUiState
import com.salat.gbinder.features.clusterBackground.entity.SlotPreview
import com.salat.gbinder.features.clusterBackground.kzb.KzbSlotParser
import com.salat.gbinder.features.clusterBackground.kzb.PngSlotPatcher
import com.salat.gbinder.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ClusterBackgroundViewModel @Inject constructor(
    private val repository: QnxClusterRepository,
    private val stateKeeper: StateKeeperRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClusterUiState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ClusterEffect>()
    val effect = _effect.asSharedFlow()

    // Latest pulled archive, reused so installing does not re-download the whole file
    @Volatile
    private var currentFile: ByteArray? = null

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        if (_state.value.mode == ClusterMode.BUSY) return@launch
        _state.update { it.copy(status = ClusterConnStatus.CHECKING) }
        val probe = runCatching { repository.probe() }.getOrNull()
        _state.update {
            it.copy(
                probe = probe,
                status = statusFor(probe?.canBackup ?: false, probe?.androidShellReady ?: false)
            )
        }
    }

    fun backup() = launchBusy(R.string.cluster_bg_busy_backup) {
        val bytes = repository.backupToBytes { f, label -> setProgress(f, label) }
        currentFile = bytes
        val name = "background_backup_${timestamp()}.kzb"
        _effect.emit(ClusterEffect.ShareBackup(bytes, name))
        _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_done_backup))
    }

    fun restore(bytes: ByteArray) = launchBusy(R.string.cluster_bg_busy_install) {
        if (!KzbSlotParser.hasKzbMagic(bytes)) {
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_invalid_file))
            return@launchBusy
        }
        repository.pushAndReboot(bytes) { f, label -> setProgress(f, label) }
        _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_done_reboot))
    }

    // Pulls the current archive and opens the theme picker built from its real slots
    fun startInstall() = launchBusy(R.string.cluster_bg_busy_download) {
        val bytes = currentFile ?: repository.backupToBytes { f, label -> setProgress(f, label) }.also { currentFile = it }
        val slots = KzbSlotParser.parse(bytes)
        if (slots.isEmpty()) {
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_no_slots))
            return@launchBusy
        }
        // Show only the real theme backgrounds - masks and warning overlays are RGBA with heavy
        // transparency, real backgrounds are opaque, so keep opaque RGB and near-opaque RGBA slots
        val previews = slots.mapNotNull { slot ->
            val thumb = decodeThumbnail(bytes, slot.dataOffset, slot.length)
            val isBackground = slot.colorType == 2 || (thumb != null && opaqueFraction(thumb) > BG_OPACITY_THRESHOLD)
            if (!isBackground) {
                thumb?.recycle()
                null
            } else {
                SlotPreview(
                    index = slot.index,
                    width = slot.width,
                    height = slot.height,
                    hasAlpha = slot.hasAlpha,
                    length = slot.length,
                    thumbnail = thumb
                )
            }
        }
        if (previews.isEmpty()) {
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_no_slots))
            return@launchBusy
        }
        _state.update { it.copy(mode = ClusterMode.SLOT_PICKER, slots = previews) }
    }

    fun cancelSlotPicker() {
        _state.update { it.copy(mode = ClusterMode.IDLE, slots = emptyList()) }
    }

    fun installToSlot(sourceImage: ByteArray, slotIndex: Int) = launchBusy(R.string.cluster_bg_busy_install) {
        val file = currentFile
        if (file == null) {
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_no_file))
            return@launchBusy
        }
        val slot = KzbSlotParser.parse(file).firstOrNull { it.index == slotIndex }
        if (slot == null) {
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_no_slots))
            return@launchBusy
        }

        setProgress(0.02f, "encode")
        val encoded = runCatching { ClusterImagePreparer.encodeForSlot(sourceImage, slot) }.getOrElse {
            Timber.e(it, "[QNX] image encode failed")
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_invalid_image))
            return@launchBusy
        }

        when (val result = PngSlotPatcher.patch(file, slot, encoded)) {
            is PngSlotPatcher.Result.TooLarge -> {
                Timber.w("[QNX] image too large: ${result.encodedSize} > ${result.budget}")
                _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_too_large))
            }

            is PngSlotPatcher.Result.Invalid -> {
                Timber.w("[QNX] patch invalid: ${result.reason}")
                _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_invalid_image))
            }

            is PngSlotPatcher.Result.Success -> {
                repository.pushAndReboot(result.patchedFile) { f, label -> setProgress(f, label) }
                // The archive on the cluster changed - drop the stale cache
                currentFile = result.patchedFile
                _state.update { it.copy(mode = ClusterMode.IDLE, slots = emptyList()) }
                _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_done_reboot))
            }
        }
    }

    private fun launchBusy(busyTitleRes: Int, block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        if (_state.value.mode == ClusterMode.BUSY) return@launch
        _state.update { it.copy(mode = ClusterMode.BUSY, busyTitleRes = busyTitleRes, progress = 0f, progressLabel = "") }
        try {
            block()
        } catch (e: QnxClusterException) {
            Timber.w(e, "[QNX] operation failed: ${e.code}")
            stateKeeper.sendLog("[QNX] failed code=${e.code} msg=${e.message}", true)
            _effect.emit(ClusterEffect.Toast(errorRes(e.code)))
        } catch (e: Throwable) {
            Timber.e(e, "[QNX] unexpected failure")
            stateKeeper.sendLog("[QNX] unexpected: ${e.message}", true)
            _effect.emit(ClusterEffect.Toast(R.string.cluster_bg_err_unknown))
        } finally {
            _state.update {
                if (it.mode == ClusterMode.SLOT_PICKER) it.copy(progress = 0f)
                else it.copy(mode = ClusterMode.IDLE, progress = 0f)
            }
        }
    }

    private fun setProgress(fraction: Float, label: String) {
        _state.update { it.copy(progress = fraction.coerceIn(0f, 1f), progressLabel = label) }
    }

    private suspend fun decodeThumbnail(file: ByteArray, offset: Int, length: Int): Bitmap? =
        withContext(Dispatchers.Default) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = THUMB_SAMPLE }
                BitmapFactory.decodeByteArray(file, offset, length, opts)
            }.getOrNull()
        }

    // Fraction of sampled pixels that are fully opaque - backgrounds are ~1.0, overlays are low
    private fun opaqueFraction(bitmap: Bitmap): Float {
        val w = bitmap.width
        val h = bitmap.height
        if (w == 0 || h == 0) return 1f
        val stepX = (w / OPACITY_SAMPLE_GRID).coerceAtLeast(1)
        val stepY = (h / OPACITY_SAMPLE_GRID).coerceAtLeast(1)
        var total = 0
        var opaque = 0
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                total++
                if ((bitmap.getPixel(x, y) ushr 24 and 0xFF) == 255) opaque++
                x += stepX
            }
            y += stepY
        }
        return if (total == 0) 1f else opaque.toFloat() / total
    }

    private fun statusFor(canBackup: Boolean, shellReady: Boolean): ClusterConnStatus = when {
        canBackup -> ClusterConnStatus.READY
        !shellReady -> ClusterConnStatus.NO_SHELL
        else -> ClusterConnStatus.UNREACHABLE
    }

    private fun errorRes(code: QnxErrorCode): Int = when (code) {
        QnxErrorCode.SHELL_UNAVAILABLE -> R.string.cluster_bg_err_no_shell
        QnxErrorCode.SHARED_NOT_WRITABLE -> R.string.cluster_bg_err_no_shared
        QnxErrorCode.QNX_UNREACHABLE -> R.string.cluster_bg_err_unreachable
        QnxErrorCode.TARGET_MISSING -> R.string.cluster_bg_err_target_missing
        QnxErrorCode.TRANSFER_FAILED -> R.string.cluster_bg_err_transfer
        QnxErrorCode.VERIFY_MISMATCH -> R.string.cluster_bg_err_verify
        QnxErrorCode.REMOUNT_FAILED -> R.string.cluster_bg_err_remount
        QnxErrorCode.NO_CHECKSUM_TOOL -> R.string.cluster_bg_err_checksum
        QnxErrorCode.IMAGE_TOO_LARGE -> R.string.cluster_bg_err_too_large
        QnxErrorCode.INVALID_FILE -> R.string.cluster_bg_err_invalid_file
        QnxErrorCode.UNKNOWN -> R.string.cluster_bg_err_unknown
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())

    private companion object {
        const val THUMB_SAMPLE = 4
        const val OPACITY_SAMPLE_GRID = 32
        const val BG_OPACITY_THRESHOLD = 0.9f
    }
}
