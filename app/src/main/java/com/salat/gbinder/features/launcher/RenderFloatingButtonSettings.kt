package com.salat.gbinder.features.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.salat.gbinder.R
import com.salat.gbinder.datastore.LauncherPrefs
import com.salat.gbinder.datastore.LauncherStorageRepository
import com.salat.gbinder.entity.DisplayLauncherApp
import com.salat.gbinder.ui.RenderSwitcher
import com.salat.gbinder.ui.ValueSlider
import com.salat.gbinder.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

private val GESTURE_KEYS = listOf(
    "single_click" to R.string.gesture_single_click,
    "double_click" to R.string.gesture_double_click,
    "triple_click" to R.string.gesture_triple_click,
    "long_press" to R.string.gesture_long_press,
    "swipe_up" to R.string.gesture_swipe_up,
    "swipe_down" to R.string.gesture_swipe_down,
    "swipe_left" to R.string.gesture_swipe_left,
    "swipe_right" to R.string.gesture_swipe_right,
)

@Composable
fun ColumnScope.RenderFloatingButtonSettings(
    allApps: List<DisplayLauncherApp>,
    storage: LauncherStorageRepository
) {
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedGesture by remember { mutableStateOf<String?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var pendingGesture by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_GESTURES, "")
            .collect { raw ->
                config = parseGestureConfig(raw)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 42.dp),
            text = stringResource(R.string.floating_button_settings),
            style = AppTheme.typography.overlayLauncherSettingsGroup,
            color = AppTheme.colors.contentAccent
        )

        Spacer(Modifier.height(16.dp))

        GESTURE_KEYS.forEach { (key, titleRes) ->
            val currentAction = config[key] ?: ""
            val actionLabel = getActionLabel(currentAction, allApps)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.colors.launcherSurface1)
                    .clickable { selectedGesture = key }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = AppTheme.typography.overlayLauncherSettingsTitle,
                    color = AppTheme.colors.contentPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = actionLabel,
                    style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                    color = AppTheme.colors.contentAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Button size
        var savedSize by remember { mutableStateOf(100) }
        LaunchedEffect(Unit) { storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_SIZE, 100).collect { savedSize = it } }
        Text(
            modifier = Modifier.padding(horizontal = 42.dp),
            text = "按钮大小: ${savedSize}dp",
            style = AppTheme.typography.overlayLauncherSettingsTitle,
            color = AppTheme.colors.contentPrimary
        )
        ValueSlider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
            value = savedSize.toFloat(),
            valueRange = 40f..200f,
            onValueChange = { scope.launch { storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_SIZE, it.toInt()) } },
            enabled = true, defaultMark = 100f, step = 5f
        )

        Spacer(Modifier.height(26.dp))

        // Button transparency
        var savedAlpha by remember { mutableStateOf(1.0f) }
        LaunchedEffect(Unit) { storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_ALPHA, 1.0f).collect { savedAlpha = it } }
        Text(
            modifier = Modifier.padding(horizontal = 42.dp),
            text = "透明度: ${(savedAlpha * 100).toInt()}%",
            style = AppTheme.typography.overlayLauncherSettingsTitle,
            color = AppTheme.colors.contentPrimary
        )
        ValueSlider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
            value = savedAlpha,
            valueRange = .2f..1.0f,
            onValueChange = { scope.launch { storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_ALPHA, it) } },
            enabled = true, defaultMark = 1.0f, step = .05f
        )

        Spacer(Modifier.height(26.dp))

        // Pet mode toggle
        var petModeEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_PET_MODE, false).collect { petModeEnabled = it } }
        RenderSwitcher(
            modifier = Modifier.padding(horizontal = 18.dp),
            title = "桌宠模式",
            subtitle = "按钮随机缓慢移动，触碰后10秒内可正常操作",
            value = petModeEnabled,
            enable = true,
            groupDivider = false,
            titleStyle = AppTheme.typography.overlayLauncherSettingsTitle,
            subtitleStyle = AppTheme.typography.overlayLauncherSettingsSubtitle,
            onChange = { scope.launch { storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_PET_MODE, it) } }
        )

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 42.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppTheme.colors.deleteButton.copy(alpha = .15f))
                .clickable {
                    scope.launch {
                        storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_GESTURES, "")
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "重置手势配置",
                color = AppTheme.colors.deleteButton,
                style = AppTheme.typography.overlayLauncherSettingsTitle
            )
        }

        Spacer(Modifier.height(64.dp))
    }

    // App picker
    if (showAppPicker && pendingGesture != null) {
        AppPickerDialog(
            apps = allApps,
            onSelect = { app ->
                pendingGesture?.let { gesture ->
                    scope.launch {
                        val newConfig = config.toMutableMap()
                        newConfig[gesture] = "app:${app.packageName}"
                        val json = JSONObject(newConfig.toMap()).toString()
                        storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_GESTURES, json)
                    }
                }
                showAppPicker = false
                pendingGesture = null
            },
            onDismiss = {
                showAppPicker = false
                pendingGesture = null
            }
        )
    }

    // Action picker
    selectedGesture?.let { gesture ->
        ActionPickerDialog(
            gestureKey = gesture,
            currentAction = config[gesture] ?: "",
            allApps = allApps,
            onSelectApp = {
                pendingGesture = gesture
                selectedGesture = null
                showAppPicker = true
            },
            onSelect = { action ->
                scope.launch {
                    val newConfig = config.toMutableMap()
                    newConfig[gesture] = action
                    val json = JSONObject(newConfig.toMap()).toString()
                    storage.dataStore.saveValue(LauncherPrefs.FLOAT_BUTTON_GESTURES, json)
                }
                selectedGesture = null
            },
            onDismiss = { selectedGesture = null }
        )
    }
}

@Composable
private fun ActionPickerDialog(
    gestureKey: String,
    currentAction: String,
    allApps: List<DisplayLauncherApp>,
    onSelectApp: () -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surfaceSettingsLayer1)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                text = getGestureTitle(gestureKey),
                style = AppTheme.typography.overlayLauncherSettingsGroup,
                color = AppTheme.colors.contentAccent
            )

            // None
            ActionOption(
                label = stringResource(R.string.gesture_none),
                selected = currentAction.isEmpty(),
                onClick = { onSelect("") }
            )

            // System actions
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                text = "系统操作",
                style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.6f)
            )
            ActionOption(
                label = "切换启动器",
                selected = currentAction == "toggle_launcher",
                onClick = { onSelect("toggle_launcher") }
            )
            ActionOption(
                label = "返回",
                selected = currentAction == "android_back",
                onClick = { onSelect("android_back") }
            )
            ActionOption(
                label = "主页",
                selected = currentAction == "android_home",
                onClick = { onSelect("android_home") }
            )
            ActionOption(
                label = "上一个应用",
                selected = currentAction == "navigate_to_past_app",
                onClick = { onSelect("navigate_to_past_app") }
            )
            ActionOption(
                label = "任务管理器",
                selected = currentAction == "task_manager",
                onClick = { onSelect("task_manager") }
            )

            // App actions
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                text = "应用操作",
                style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.6f)
            )
            ActionOption(
                label = "启动应用",
                selected = currentAction.startsWith("app:"),
                onClick = onSelectApp
            )
            ActionOption(
                label = "应用轮播",
                selected = currentAction == "app_carousel",
                onClick = { onSelect("app_carousel") }
            )
            ActionOption(
                label = "应用启动器",
                selected = currentAction == "app_launcher",
                onClick = { onSelect("app_launcher") }
            )

            // Media & Navigation
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                text = "媒体与导航",
                style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.6f)
            )
            ActionOption(
                label = "导航/媒体切换",
                selected = currentAction == "navi_media_switch",
                onClick = { onSelect("navi_media_switch") }
            )
            ActionOption(
                label = "音频源轮播",
                selected = currentAction == "carousel_audio_source",
                onClick = { onSelect("carousel_audio_source") }
            )

            // Car features
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                text = "车辆功能",
                style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.6f)
            )
            ActionOption(
                label = "360摄像头",
                selected = currentAction == "cameras_360",
                onClick = { onSelect("cameras_360") }
            )
            ActionOption(
                label = "CarPlay",
                selected = currentAction == "carplay_launch",
                onClick = { onSelect("carplay_launch") }
            )
            ActionOption(
                label = "切换驾驶模式",
                selected = currentAction == "toggle_dm",
                onClick = { onSelect("toggle_dm") }
            )
            ActionOption(
                label = "驾驶模式轮播",
                selected = currentAction == "carousel_dm",
                onClick = { onSelect("carousel_dm") }
            )
            ActionOption(
                label = "灯光模式轮播",
                selected = currentAction == "carousel_lamp",
                onClick = { onSelect("carousel_lamp") }
            )
            ActionOption(
                label = "拨打电话",
                selected = currentAction == "phone_call",
                onClick = { onSelect("phone_call") }
            )

            // GIB actions
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                text = "GIB 功能",
                style = AppTheme.typography.overlayLauncherSettingsSubtitle,
                color = AppTheme.colors.contentPrimary.copy(.6f)
            )
            ActionOption(
                label = "打开 GIB 设置",
                selected = currentAction == "open_gib",
                onClick = { onSelect("open_gib") }
            )

            // Cancel
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                text = stringResource(R.string.close),
                color = AppTheme.colors.contentAccent,
                style = AppTheme.typography.overlayLauncherSettingsTitle
            )
        }
    }
}

@Composable
private fun ActionOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTheme.typography.overlayLauncherSettingsTitle,
            color = if (selected) AppTheme.colors.contentAccent else AppTheme.colors.contentPrimary
        )
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<DisplayLauncherApp>,
    onSelect: (DisplayLauncherApp) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surfaceSettingsLayer1)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                text = "选择应用",
                style = AppTheme.typography.overlayLauncherSettingsGroup,
                color = AppTheme.colors.contentAccent
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(apps) { _, app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(app) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = app.iconRef,
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = app.appName,
                            style = AppTheme.typography.overlayLauncherSettingsTitle,
                            color = AppTheme.colors.contentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun getGestureTitle(key: String): String = when (key) {
    "single_click" -> "单击"
    "double_click" -> "双击"
    "triple_click" -> "三击"
    "long_press" -> "长按"
    "swipe_up" -> "上滑"
    "swipe_down" -> "下滑"
    "swipe_left" -> "左滑"
    "swipe_right" -> "右滑"
    else -> key
}

private fun getActionLabel(action: String, allApps: List<DisplayLauncherApp>): String = when {
    action.isEmpty() -> "无"
    action == "toggle_launcher" -> "切换启动器"
    action == "open_gib" -> "打开 GIB 设置"
    action == "open_keybinds" -> "打开按键绑定"
    action == "android_back" -> "返回"
    action == "android_home" -> "主页"
    action == "navigate_to_past_app" -> "上一个应用"
    action == "task_manager" -> "任务管理器"
    action == "app_carousel" -> "应用轮播"
    action == "app_launcher" -> "应用启动器"
    action == "navi_media_switch" -> "导航/媒体切换"
    action == "carousel_audio_source" -> "音频源轮播"
    action == "cameras_360" -> "360摄像头"
    action == "carplay_launch" -> "CarPlay"
    action == "toggle_dm" -> "切换驾驶模式"
    action == "carousel_dm" -> "驾驶模式轮播"
    action == "carousel_lamp" -> "灯光模式轮播"
    action == "phone_call" -> "拨打电话"
    action.startsWith("app:") -> {
        val pkg = action.substring(4)
        val app = allApps.find { it.packageName == pkg }
        "应用: ${app?.appName ?: pkg}"
    }
    action.startsWith("activity:") -> {
        val parts = action.substring(9).split("/")
        if (parts.size == 2) {
            val app = allApps.find { it.packageName == parts[0] }
            "Activity: ${app?.appName ?: parts[0]}"
        } else action
    }
    else -> action
}

private fun parseGestureConfig(raw: String): Map<String, String> {
    if (raw.isBlank()) return mapOf("single_click" to "toggle_launcher")
    return try {
        val obj = JSONObject(raw)
        GESTURE_KEYS.map { it.first }.associateWith { key ->
            obj.optString(key, "")
        }
    } catch (_: Exception) {
        mapOf("single_click" to "toggle_launcher")
    }
}
