package com.salat.gbinder.features.geelyLauncher

import androidx.compose.runtime.Immutable
import com.salat.gbinder.entity.DisplayIconRef

@Immutable
data class GeelyLauncherApp(
    val packageName: String,
    val appName: String,
    val iconRef: DisplayIconRef
)

@Immutable
data class GeelyLauncherIconConfig(
    val columns: Int = 6,
    val iconSize: Int = 64,
    val iconRound: Int = 14,
    val iconTextEnable: Boolean = true,
    val iconTextSize: Int = 11,
    val iconTextPadding: Int = 10,
    val iconTextMultiline: Boolean = false,
    val iconOutSpace: Int = 18,
    val iconInnerSpace: Int = 14,
    val shortcutSize: Int = 18,
    val enableShortcuts: Boolean = false
)

object GeelyLauncherGridConfig {
    val default = GeelyLauncherIconConfig()
}
