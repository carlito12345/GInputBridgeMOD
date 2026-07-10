package com.salat.gbinder.features.launcher

import android.content.Context
import android.net.Uri
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.salat.gbinder.coroutines.IoCoroutineScope
import com.salat.gbinder.entity.DisplayIconRef
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val PREWARM_LIMIT = 50

@Singleton
class LauncherIconPrewarmer @Inject constructor(
    @ApplicationContext private val context: Context,
    data: LauncherDataRepository,
    @IoCoroutineScope scope: CoroutineScope
) {
    private data class Targets(
        val icons: List<Pair<DisplayIconRef?, Uri?>>,
        val pxSize: Int
    )

    init {
        scope.launch {
            combine(
                data.myAppsItems.filterNotNull(),
                data.allApps,
                data.settingsConfig.filterNotNull()
            ) { myApps, allApps, config ->
                val pxSize = with(
                    Density(context.resources.displayMetrics.density * config.uiScale)
                ) { config.iconSize.dp.roundToPx() }
                Targets(
                    icons = (
                        myApps.take(PREWARM_LIMIT).map { it.iconRef to it.customIcon } +
                            allApps.take(PREWARM_LIMIT).map { it.iconRef to it.customIcon }
                        ).distinct(),
                    pxSize = pxSize
                )
            }
                .distinctUntilChanged()
                .collect { targets ->
                    val loader = context.imageLoader
                    targets.icons.forEach { (iconRef, customIcon) ->
                        if (iconRef == null && customIcon == null) return@forEach
                        loader.enqueue(
                            launcherIconRequest(context, iconRef, customIcon, targets.pxSize)
                        )
                    }
                }
        }
    }
}
