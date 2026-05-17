package com.salat.gbinder.features.geelyLauncher

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salat.gbinder.R
import com.salat.gbinder.entity.DisplayLauncherItem
import com.salat.gbinder.entity.DisplayLauncherItemType
import com.salat.gbinder.features.geelyLauncher.entity.GLScreenState
import com.salat.gbinder.features.launcher.RenderLauncherMyAppCell
import com.salat.gbinder.ui.BaseDialog
import com.salat.gbinder.ui.ConfirmDialog
import com.salat.gbinder.ui.DrawableImage
import com.salat.gbinder.ui.ProfileSwitch
import com.salat.gbinder.ui.RenderScan
import com.salat.gbinder.ui.TopShadow
import com.salat.gbinder.ui.theme.AppTheme
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed

@Composable
fun RenderGeelyLauncherSettings(
    uiScaleState: Float? = null,
    onClose: () -> Unit
) {
    val viewModel: GeelyLauncherViewModel = hiltViewModel()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showRefreshDialog by remember { mutableStateOf(false) }
    val screenType by viewModel.screenType.collectAsStateWithLifecycle()
    val isApplying by viewModel.isApplying.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.toastMessage.collect { messageRes ->
            Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_LONG).show()
        }
    }

    // Check has app storage
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.initialCheck()

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize()) {

        // Toolbar
        RenderLauncherSettingsToolbar(
            isBuilder = screenType == GLScreenState.BUILDER,
            isApplying = isApplying,
            onAddClick = { showAddDialog = true },
            onRefreshClick = { showRefreshDialog = true },
            onClose = onClose
        )

        RenderConfiguratorPresetsContent(
            uiScaleState = uiScaleState,
            viewModel = viewModel
        )

        val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle()
        val applyProgress by viewModel.applyProgress.collectAsStateWithLifecycle()
        val applyProgressAnimationDurationMs by viewModel.applyProgressAnimationDurationMs.collectAsStateWithLifecycle()
        RenderGeelyLauncherApplyButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            hasChanges = hasChanges,
            isApplying = isApplying,
            progress = applyProgress,
            progressAnimationDurationMs = applyProgressAnimationDurationMs,
            onClick = viewModel::applyChanges
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.launcher_order_unavailable),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 23.dp),
            color = AppTheme.colors.contentPrimary.copy(.4f),
            style = AppTheme.typography.dialogSubtitle
        )

        Spacer(Modifier.height(46.dp))
    }

    if (showAddDialog) {
        val availableApps by viewModel.availableApps.collectAsStateWithLifecycle()
        val launcherApps by viewModel.launcherApps.collectAsStateWithLifecycle()
        RenderGeelyLauncherAppsPickerDialog(
            uiScaleState = uiScaleState,
            apps = availableApps,
            selectedPackages = launcherApps.map { it.packageName }.toSet(),
            onApply = { selectedPackages ->
                viewModel.setSelectedApps(selectedPackages)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showRefreshDialog) {
        ConfirmDialog(
            title = stringResource(R.string.restart_launcher),
            message = stringResource(R.string.stock_launcher_restart_warning),
            uiScale = uiScaleState,
            disableNegative = false,
            negativeAction = true,
            onCancel = { showRefreshDialog = false },
            onDismiss = { showRefreshDialog = false },
            onClick = {
                viewModel.restartLauncher()
                showRefreshDialog = false
            }
        )
    }
}

@Composable
private fun RenderLauncherSettingsToolbar(
    isBuilder: Boolean,
    isApplying: Boolean,
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onClose: () -> Unit
) {
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
            onClick = remember { { onClose() } }
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
            text = stringResource(R.string.stock_launcher_title),
            style = AppTheme.typography.stubTitle,
            color = AppTheme.colors.contentPrimary
        )

        if (isBuilder) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isApplying) AppTheme.colors.contentPrimary.copy(.18f) else AppTheme.colors.surfaceLayer1)
                    .clickable(enabled = !isApplying, onClick = onRefreshClick)
                    .padding(start = 12.dp, top = 7.dp, end = 12.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.Refresh,
                    tint = Color.White,
                    contentDescription = "restart"
                )

            }

            Spacer(Modifier.width(20.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isApplying) AppTheme.colors.contentPrimary.copy(.18f) else AppTheme.colors.contentAccent)
                    .clickable(enabled = !isApplying, onClick = onAddClick)
                    .padding(start = 8.dp, top = 7.dp, end = 12.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.Add,
                    tint = Color.White,
                    contentDescription = "add"
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.add),
                    color = Color.White,
                    style = AppTheme.typography.toolbarButton
                )
            }
            Spacer(Modifier.width(20.dp))
        } else {
            Spacer(Modifier.width(36.dp))
        }
    }
}

@Composable
private fun ColumnScope.RenderConfiguratorPresetsContent(
    uiScaleState: Float?,
    viewModel: GeelyLauncherViewModel
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(AppTheme.colors.lampBackground.copy(.3f))
    ) {
        TopShadow()

        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            val screenType by viewModel.screenType.collectAsStateWithLifecycle()
            val apps by viewModel.launcherApps.collectAsStateWithLifecycle()
            val isApplying by viewModel.isApplying.collectAsStateWithLifecycle()

            when (screenType) {
                GLScreenState.LOADING -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RenderScan()
                }

                GLScreenState.NEED_APP_STORAGE -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.install_gappstorage_first),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.screenTitle.copy(
                            lineHeight = 23.sp
                        ),
                        color = AppTheme.colors.contentPrimary
                    )
                }

                GLScreenState.BUILDER -> Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(Modifier.height(16.dp))
                    if (apps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                text = stringResource(R.string.geely_launcher_empty_grid),
                                style = AppTheme.typography.overlayLauncherMenuTitle,
                                textAlign = TextAlign.Center,
                                color = AppTheme.colors.contentPrimary.copy(.65f)
                            )
                        }
                    } else {
                        AppTheme(darkTheme = true) {
                            RenderGeelyLauncherAppsGrid(
                                items = apps,
                                isApplying = isApplying,
                                gridState = rememberLazyGridState(),
                                config = GeelyLauncherGridConfig.default,
                                onHideApp = { viewModel.removeApp(it.packageName) }
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun RenderGeelyLauncherApplyButton(
    modifier: Modifier = Modifier,
    hasChanges: Boolean,
    isApplying: Boolean,
    progress: Float,
    progressAnimationDurationMs: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val baseColor = AppTheme.colors.contentPrimary.copy(.22f)
    val accentColor = AppTheme.colors.contentAccent
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = progressAnimationDurationMs),
        label = "GeelyLauncherApplyProgress"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(if (hasChanges || isApplying) accentColor.copy(.35f) else baseColor)
            .clickable(enabled = hasChanges && !isApplying, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isApplying) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .align(Alignment.CenterStart)
                    .background(accentColor)
            )
        } else if (hasChanges) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(accentColor)
            )
        }

        Text(
            text = if (isApplying) {
                stringResource(R.string.geely_launcher_applying)
            } else {
                stringResource(R.string.apply)
            },
            style = AppTheme.typography.overlayLauncherSettingsGroup.copy(fontSize = 18.sp),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColumnScope.RenderGeelyLauncherAppsGrid(
    items: List<GeelyLauncherApp>,
    isApplying: Boolean,
    gridState: LazyGridState,
    config: GeelyLauncherIconConfig,
    onHideApp: (item: GeelyLauncherApp) -> Unit
) {
    val frozenIconColorFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(config.columns),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentPadding = PaddingValues(config.iconOutSpace.dp),
        verticalArrangement = Arrangement.spacedBy(config.iconInnerSpace.dp),
        horizontalArrangement = Arrangement.spacedBy(config.iconInnerSpace.dp),
    ) {
        gridItemsIndexed(
            items = items,
            key = { _, item -> item.packageName }
        ) { index, app ->
            Box {
                RenderLauncherMyAppCell(
                    app = app.toDisplayLauncherItem(index),
                    cellSize = config.iconSize,
                    enableText = config.iconTextEnable,
                    iconRound = config.iconRound,
                    textSize = config.iconTextSize,
                    textPadding = config.iconTextPadding,
                    enableShortcuts = config.enableShortcuts,
                    shortcutSize = config.shortcutSize,
                    enableMultiline = config.iconTextMultiline,
                    sizeSensitive = false,
                    frozenIconColorFilter = frozenIconColorFilter,
                    lockMode = isApplying,
                    enableClick = false,
                    onHideApp = { onHideApp(app) },
                    onLongClick = {}
                )
            }
            /*
            ReorderableItem(
                state = reorderState,
                key = app.packageName,
                enabled = !isApplying
            ) {
                val dragModifier = if (!isApplying) {
                    Modifier.draggableHandle(onDragStopped = { onReorderDrop() })
                } else Modifier

                Box(dragModifier) {
                    RenderLauncherMyAppCell(
                        app = app.toDisplayLauncherItem(index),
                        cellSize = config.iconSize,
                        enableText = config.iconTextEnable,
                        iconRound = config.iconRound,
                        textSize = config.iconTextSize,
                        textPadding = config.iconTextPadding,
                        enableShortcuts = config.enableShortcuts,
                        shortcutSize = config.shortcutSize,
                        enableMultiline = config.iconTextMultiline,
                        sizeSensitive = false,
                        frozenIconColorFilter = frozenIconColorFilter,
                        lockMode = isApplying,
                        enableClick = false,
                        onHideApp = { onHideApp(app) },
                        onLongClick = {}
                    )
                }
            }
             */
        }
    }
}

@Composable
private fun RenderGeelyLauncherAppsPickerDialog(
    uiScaleState: Float?,
    apps: List<GeelyLauncherApp>,
    selectedPackages: Set<String>,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) = BaseDialog(uiScaleState = uiScaleState, onDismiss = onDismiss) {
    var selected by remember(selectedPackages) { mutableStateOf(selectedPackages) }
    val sortedApps = remember(apps, selectedPackages) {
        val (active, inactive) = apps.partition { it.packageName in selectedPackages }
        active + inactive
    }

    if (sortedApps.isEmpty()) {
        RenderScan()
    } else {
        Column(modifier = Modifier.padding(top = 22.dp)) {
            Text(
                text = stringResource(R.string.geely_launcher_add_apps),
                modifier = Modifier.padding(horizontal = 24.dp),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = stringResource(R.string.geely_launcher_add_apps_desc),
                modifier = Modifier.padding(horizontal = 23.dp),
                color = AppTheme.colors.contentPrimary.copy(.4f),
                style = AppTheme.typography.dialogSubtitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            RenderGeelyLauncherAppsDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item(key = -1) {
                    Spacer(Modifier.height(.8.dp))
                }
                itemsIndexed(
                    items = sortedApps,
                    key = { _, item -> item.packageName }
                ) { _, item ->
                    val checked = item.packageName in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked) {
                                    selected - item.packageName
                                } else {
                                    selected + item.packageName
                                }
                            }
                            .padding(vertical = 8.dp)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(16.dp))

                        DrawableImage(
                            icon = item.iconRef,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.appName,
                                style = AppTheme.typography.dialogListTitle,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = AppTheme.colors.contentPrimary
                            )
                            Text(
                                text = item.packageName,
                                style = AppTheme.typography.dialogSubtitle,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = AppTheme.colors.contentPrimary.copy(.5f)
                            )
                        }

                        ProfileSwitch(
                            scale = .8f,
                            checked = checked,
                            enabled = true,
                            onCheckedChange = null
                        )

                        Spacer(Modifier.width(12.dp))
                    }
                }
            }

            RenderGeelyLauncherAppsDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    text = stringResource(android.R.string.cancel).uppercase(),
                    style = AppTheme.typography.dialogButton,
                    color = AppTheme.colors.contentAccent
                )
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onApply(selected) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    text = stringResource(android.R.string.ok).uppercase(),
                    style = AppTheme.typography.dialogButton,
                    color = AppTheme.colors.contentAccent
                )
            }
        }
    }
}

@Composable
private fun RenderGeelyLauncherAppsDivider() = Spacer(
    Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.White.copy(.1f))
)

private fun GeelyLauncherApp.toDisplayLauncherItem(index: Int) = DisplayLauncherItem(
    type = DisplayLauncherItemType.APP,
    id = packageName.hashCode().toLong(),
    order = index,
    title = appName,
    iconRef = iconRef,
    customIcon = null,
    packageName = packageName,
    launchActivity = "",
    data = "",
    isCall = false,
    isSplit = false,
    isFrozen = false,
    isSystem = false
)
