package com.salat.gbinder.features.launcher

import com.salat.gbinder.entity.DisplayLauncherApp
import com.salat.gbinder.entity.DisplayLauncherItem

fun filterVisibleLauncherApps(
    items: List<DisplayLauncherApp>,
    showFrozenApps: Boolean,
    showHiddenApps: Boolean = true
): List<DisplayLauncherApp> {
    if (showFrozenApps && showHiddenApps) return items
    return items.filterNot { item ->
        (!showFrozenApps && item.isFrozen) || (!showHiddenApps && item.isHidden)
    }
}

fun filterVisibleLauncherItems(
    items: List<DisplayLauncherItem>,
    showFrozenApps: Boolean,
    showHiddenApps: Boolean = true
): List<DisplayLauncherItem> {
    if (showFrozenApps && showHiddenApps) return items
    return items.filterNot { item ->
        (!showFrozenApps && item.isFrozen) || (!showHiddenApps && item.isHidden)
    }
}
