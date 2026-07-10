package com.salat.gbinder.features.clusterBackground

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salat.gbinder.BuildConfig
import com.salat.gbinder.R
import com.salat.gbinder.entity.DisplayAdbState
import com.salat.gbinder.features.clusterBackground.entity.ClusterConnStatus
import com.salat.gbinder.features.clusterBackground.entity.ClusterEffect
import com.salat.gbinder.features.clusterBackground.entity.ClusterMode
import com.salat.gbinder.features.clusterBackground.entity.SlotPreview
import com.salat.gbinder.ui.BaseButton
import com.salat.gbinder.ui.ConfirmDialog
import com.salat.gbinder.ui.RenderListButton
import com.salat.gbinder.ui.StatusLampSquare
import com.salat.gbinder.ui.TopShadow
import com.salat.gbinder.ui.clickableNoRipple
import com.salat.gbinder.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

private var SHOW_CONTROLS = false
private var ENABLE_TIMER = if (BuildConfig.DEBUG) 3 else 10

@Composable
fun RenderClusterBackgroundScreen(
    uiScaleState: Float? = null,
    onClose: () -> Unit
) {
    val viewModel: ClusterBackgroundViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    var pendingRestore by remember { mutableStateOf<ByteArray?>(null) }
    var pendingSlot by remember { mutableStateOf<Int?>(null) }
    var pendingInstall by remember { mutableStateOf<Pair<Int, ByteArray>?>(null) }

    val restorePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) scope.launch {
                val bytes = readBytes(context, uri)
                if (bytes == null) {
                    Toast.makeText(context, R.string.cluster_bg_err_read_file, Toast.LENGTH_LONG)
                        .show()
                } else {
                    pendingRestore = bytes
                }
            }
        }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val slot = pendingSlot
            if (uri != null && slot != null) scope.launch {
                val bytes = readBytes(context, uri)
                if (bytes == null) {
                    Toast.makeText(context, R.string.cluster_bg_err_read_file, Toast.LENGTH_LONG)
                        .show()
                } else {
                    pendingInstall = slot to bytes
                }
            }
            pendingSlot = null
        }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ClusterEffect.Toast ->
                    Toast.makeText(context, context.getString(effect.res), Toast.LENGTH_LONG).show()

                is ClusterEffect.ShareBackup ->
                    withContext(Dispatchers.IO) {
                        shareBackup(
                            context,
                            effect.bytes,
                            effect.fileName
                        )
                    }
            }
        }
    }

    // Block leaving while a write is in flight - losing the screen drops progress, verify and share
    val handleBack = {
        when (state.mode) {
            ClusterMode.BUSY -> Unit
            ClusterMode.SLOT_PICKER -> viewModel.cancelSlotPicker()
            else -> onClose()
        }
    }

    BackHandler(onBack = handleBack)

    Column(Modifier.fillMaxSize()) {
        ClusterToolbar(
            titleRes = if (state.mode == ClusterMode.SLOT_PICKER) R.string.cluster_bg_pick_slot else R.string.cluster_bg_title,
            onClose = handleBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.lampBackground.copy(.3f))
        ) {
            TopShadow()

            when (state.mode) {
                ClusterMode.SLOT_PICKER -> SlotPickerContent(
                    slots = state.slots,
                    onSlotClick = { index ->
                        pendingSlot = index
                        imagePicker.launch("image/*")
                    }
                )

                else -> ClusterMainContent(
                    status = state.status,
                    canBackup = state.canBackup,
                    canWrite = state.canWrite,
                    onBackup = viewModel::backup,
                    onRestore = { restorePicker.launch(arrayOf("*/*")) },
                    onInstall = viewModel::startInstall,
                    onRefresh = viewModel::refresh
                )
            }

            if (state.mode == ClusterMode.BUSY) {
                BusyOverlay(
                    title = stringResource(state.busyTitleRes),
                    progress = state.progress,
                    label = state.progressLabel
                )
            }
        }
    }

    pendingRestore?.let { bytes ->
        ConfirmDialog(
            title = stringResource(R.string.cluster_bg_confirm_restore_title),
            message = stringResource(R.string.cluster_bg_confirm_danger_message),
            uiScale = uiScaleState,
            negativeAction = true,
            onCancel = { pendingRestore = null },
            onDismiss = { pendingRestore = null },
            onClick = {
                viewModel.restore(bytes)
                pendingRestore = null
            }
        )
    }

    pendingInstall?.let { (slotIndex, bytes) ->
        ConfirmDialog(
            title = stringResource(R.string.cluster_bg_confirm_install_title),
            message = stringResource(R.string.cluster_bg_confirm_danger_message),
            uiScale = uiScaleState,
            negativeAction = true,
            onCancel = { pendingInstall = null },
            onDismiss = { pendingInstall = null },
            onClick = {
                viewModel.installToSlot(bytes, slotIndex)
                pendingInstall = null
            }
        )
    }
}

@Composable
private fun ClusterToolbar(titleRes: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .padding(start = 2.dp),
            onClick = onClose
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint = AppTheme.colors.contentPrimary,
                contentDescription = stringResource(R.string.back)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(titleRes),
            style = AppTheme.typography.stubTitle,
            color = AppTheme.colors.contentPrimary
        )
        Spacer(Modifier.width(36.dp))
    }
}

@Composable
private fun ClusterMainContent(
    status: ClusterConnStatus,
    canBackup: Boolean,
    canWrite: Boolean,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onInstall: () -> Unit,
    onRefresh: () -> Unit
) {
    var showControls by remember { mutableStateOf(SHOW_CONTROLS) }
    var enableTimer by remember { mutableIntStateOf(ENABLE_TIMER) }
    LaunchedEffect(Unit) {
        val times = enableTimer
        repeat(times) {
            delay(1000L)
            enableTimer--
            ENABLE_TIMER--
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(26.dp))

        // Standing danger warning - cluster changes can brick with no recovery dump
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 43.dp)
                .background(AppTheme.colors.launcherAccent.copy(.9f), RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            text = stringResource(R.string.cluster_bg_warning),
            style = AppTheme.typography.surfaceSubtitle,
            color = AppTheme.colors.contentPrimary
        )

        Spacer(Modifier.height(16.dp))

        // normal warning
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 43.dp)
                .background(AppTheme.colors.warning.copy(.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            text = stringResource(R.string.cluster_theme_replacement_desc),
            style = AppTheme.typography.surfaceSubtitle,
            color = AppTheme.colors.contentPrimary
        )

        val (lampState, titleRes, subtitleRes) = when (status) {
            ClusterConnStatus.CHECKING -> Triple(
                DisplayAdbState.Connecting,
                R.string.cluster_bg_status_checking,
                R.string.cluster_bg_status_checking_desc
            )

            ClusterConnStatus.READY -> Triple(
                DisplayAdbState.Connected,
                R.string.cluster_bg_status_ready,
                R.string.cluster_bg_status_ready_desc
            )

            ClusterConnStatus.NO_SHELL -> Triple(
                DisplayAdbState.Error(""),
                R.string.cluster_bg_status_no_shell,
                R.string.cluster_bg_status_no_shell_desc
            )

            ClusterConnStatus.UNREACHABLE -> Triple(
                DisplayAdbState.Error(""),
                R.string.cluster_bg_status_unreachable,
                R.string.cluster_bg_status_unreachable_desc
            )
        }

        if (showControls) {
            Spacer(Modifier.height(16.dp))

            StatusLampSquare(
                state = lampState,
                title = stringResource(titleRes),
                subtitle = stringResource(subtitleRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 43.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickableNoRipple(onClick = onRefresh)
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(8.dp))

            RenderListButton(
                modifier = Modifier.padding(horizontal = 20.dp),
                enable = canBackup,
                title = stringResource(R.string.cluster_bg_backup),
                subtitle = stringResource(R.string.cluster_bg_backup_desc),
                onClick = onBackup
            )

            RenderListButton(
                modifier = Modifier.padding(horizontal = 20.dp),
                enable = canWrite,
                title = stringResource(R.string.cluster_bg_restore),
                subtitle = stringResource(R.string.cluster_bg_restore_desc),
                onClick = onRestore
            )

            RenderListButton(
                modifier = Modifier.padding(horizontal = 20.dp),
                enable = canWrite,
                title = stringResource(R.string.cluster_bg_install),
                subtitle = stringResource(R.string.cluster_bg_install_desc),
                onClick = onInstall
            )
        } else {
            Spacer(Modifier.height(22.dp))

            BaseButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 43.dp),
                title = if (enableTimer != 0) {
                    enableTimer.toString()
                } else {
                    stringResource(R.string.im_scared_but_ready)
                },
                backgroundColor = if (enableTimer == 0) {
                    AppTheme.colors.addSplitBottom
                } else AppTheme.colors.statusDisabled.copy(.3f),
                enable = enableTimer == 0
            ) {
                showControls = true
                SHOW_CONTROLS = true
            }
        }

        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun SlotPickerContent(
    slots: List<SlotPreview>,
    onSlotClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(slots, key = { _, it -> it.index }) { position, slot ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSlotClick(slot.index) }
                    .background(AppTheme.colors.lampSelectorBg)
                    .padding(8.dp)
            ) {
                val thumb = slot.thumbnail
                if (thumb != null) {
                    Image(
                        bitmap = thumb.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(thumb.width.toFloat() / thumb.height.coerceAtLeast(1))
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1920f / 532f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppTheme.colors.contentPrimary.copy(.1f))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.cluster_bg_slot_label,
                        position + 1,
                        if (slot.hasAlpha) "RGBA" else "RGB"
                    ),
                    style = AppTheme.typography.dialogSubtitle,
                    color = AppTheme.colors.contentPrimary
                )
            }
        }
    }
}

@Composable
private fun BusyOverlay(title: String, progress: Float, label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.lampBackground.copy(.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = AppTheme.typography.screenTitle,
                color = AppTheme.colors.contentPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                color = AppTheme.colors.contentAccent,
                trackColor = AppTheme.colors.contentPrimary.copy(.15f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${(progress * 100).toInt()}%  $label",
                style = AppTheme.typography.dialogSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.7f)
            )
        }
    }
}

private suspend fun readBytes(context: android.content.Context, uri: Uri): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri).use { it?.readBytes() }
        }.getOrNull()
    }

// Writes the pulled archive to cache and hands it to the system share sheet
private fun shareBackup(context: android.content.Context, bytes: ByteArray, fileName: String) {
    runCatching {
        val file = File(context.cacheDir, fileName).apply { writeBytes(bytes) }
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, "file", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, fileName).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }.onFailure { Timber.e(it, "[QNX] share backup failed") }
}
