package com.salat.gbinder.features.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.gbinder.R
import com.salat.gbinder.entity.DisplayLauncherApp
import com.salat.gbinder.entity.DisplayLauncherConfig
import com.salat.gbinder.ui.theme.AppTheme

@Composable
fun ColumnScope.RenderLauncherAllApps(
    items: List<DisplayLauncherApp>,
    config: DisplayLauncherConfig,
    gridState: LazyGridState,
    onClick: (DisplayLauncherApp) -> Unit,
    onLongClick: (item: DisplayLauncherApp, offset: Offset) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { app ->
            app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = config.iconOutSpace.dp),
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = null,
                tint = AppTheme.colors.contentPrimary
            )
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.contentAccent,
            unfocusedBorderColor = AppTheme.colors.contentPrimary.copy(alpha = .3f),
            cursorColor = AppTheme.colors.contentAccent,
        )
    )

    val frozenIconColorFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        if (filteredItems.isEmpty()) {
            Text(
                text = stringResource(R.string.no_search_results),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.stubTitle,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = config.iconSize.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(config.iconOutSpace.dp),
                verticalArrangement = Arrangement.spacedBy(config.iconInnerSpace.dp),
                horizontalArrangement = Arrangement.spacedBy(config.iconInnerSpace.dp),
            ) {
                items(
                    items = filteredItems,
                    key = { it.id }
                ) { app ->
                    RenderLauncherAllAppCell(
                        app = app,
                        cellSize = config.iconSize,
                        enableText = config.iconTextEnable,
                        iconRound = config.iconRound,
                        textSize = config.iconTextSize,
                        textPadding = config.iconTextPadding,
                        enableMultiline = config.iconTextMultiline,
                        frozenIconColorFilter = frozenIconColorFilter,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            }
        }
    }
}
