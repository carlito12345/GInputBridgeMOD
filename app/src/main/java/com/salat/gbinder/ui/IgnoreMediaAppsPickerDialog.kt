package com.salat.gbinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.gbinder.APP_ICON_QUALITY
import com.salat.gbinder.APP_ICON_ROUND
import com.salat.gbinder.R
import com.salat.gbinder.datastore.DataStoreRepository
import com.salat.gbinder.datastore.GeneralPrefs
import com.salat.gbinder.entity.DeviceAppInfo
import com.salat.gbinder.mappers.toAllDisplay
import com.salat.gbinder.ui.theme.AppTheme
import com.salat.gbinder.util.SystemAppsLightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RenderIgnoreMediaAppsPickerDialog(
    uiScaleState: Float? = null,
    systemApps: SystemAppsLightRepository,
    dataStore: DataStoreRepository,
    onDismiss: () -> Unit = {}
) = BaseDialog(uiScaleState = uiScaleState, onDismiss = onDismiss) {

    var apps: List<DeviceAppInfo>? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            val ownPackage = context.packageName
            val ignoredSerialized =
                dataStore.getValueFlow(GeneralPrefs.IGNORE_MEDIA_APPS).firstOrNull() ?: ""
            val ignoredSet = ignoredSerialized
                .split("|")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
            val installed = systemApps
                .getAllApps(APP_ICON_ROUND, true, APP_ICON_QUALITY)
                .filter { it.packageName != ownPackage }
                .sortedBy { it.appName }
                .toAllDisplay()
                .map { it.copy(isSelected = it.packageName in ignoredSet, isDefault = false) }
            val (selectedFirst, rest) = installed.partition { it.isSelected }
            apps = selectedFirst + rest
        }
    }

    if (apps == null || apps?.isEmpty() == true) {
        RenderScan()
    } else {
        Column(modifier = Modifier.padding(top = 22.dp)) {
            Text(
                text = stringResource(R.string.exceptions),
                modifier = Modifier.padding(horizontal = 24.dp),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = stringResource(R.string.audio_control_exceptions_desc),
                modifier = Modifier.padding(horizontal = 23.dp),
                color = AppTheme.colors.contentPrimary.copy(.4f),
                style = AppTheme.typography.dialogSubtitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            RenderIgnoreMediaAppsDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item(key = -1) {
                    Spacer(
                        Modifier
                            .height(.8.dp)
                    )
                }
                itemsIndexed(
                    items = apps as List<DeviceAppInfo>,
                    key = { _, item -> item.packageName }
                ) { _, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    withContext(Dispatchers.Default) {
                                        apps = apps?.map {
                                            if (it == item) {
                                                it.copy(isSelected = !it.isSelected)
                                            } else it
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(16.dp))

                        item.icon.let { icon ->
                            DrawableImage(
                                icon = icon,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                        }

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
                            checked = item.isSelected,
                            enabled = true,
                            onCheckedChange = null
                        )

                        Spacer(Modifier.width(12.dp))
                    }
                }
            }

            RenderIgnoreMediaAppsDivider()

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
                        .clickable {
                            scope.launch {
                                val serialized = apps
                                    ?.filter { it.isSelected }
                                    ?.joinToString("|") { it.packageName } ?: ""
                                withContext(Dispatchers.IO) {
                                    dataStore.saveValue(GeneralPrefs.IGNORE_MEDIA_APPS, serialized)
                                }
                                onDismiss()
                            }
                        }
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
private fun RenderIgnoreMediaAppsDivider() = Spacer(
    Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.White.copy(.1f))
)
