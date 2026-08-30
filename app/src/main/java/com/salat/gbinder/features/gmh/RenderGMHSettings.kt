package com.salat.gbinder.features.gmh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.salat.gbinder.receiver.GMediaHudReceiver
import com.salat.gbinder.entity.HudTrackInfo
import com.salat.gbinder.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * GMH 仪表盘设置页 - 显示当前歌曲信息 + 仪表盘开关
 */
@Composable
fun RenderGMHSettings(
    gmhEnabled: Boolean,
    onGMHEnabledChanged: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    var currentTrack by remember { mutableStateOf<HudTrackInfo?>(null) }

    // 订阅 GMH 广播的歌曲信息
    LaunchedEffect(Unit) {
        GMediaHudReceiver.trackEvents.collectLatest { info ->
            currentTrack = info
        }
    }

    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = AppTheme.colors.contentPrimary,
                    contentDescription = "Back"
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "GMH 仪表盘",
                style = AppTheme.typography.stubTitle,
                color = AppTheme.colors.contentPrimary
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 仪表盘开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("仪表盘显示", color = AppTheme.colors.contentPrimary, fontSize = 16.sp)
                Switch(
                    checked = gmhEnabled,
                    onCheckedChange = onGMHEnabledChanged
                )
            }

            // 当前歌曲信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22000000))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "当前播放",
                        color = AppTheme.colors.contentPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = currentTrack?.title ?: "未在播放",
                        color = AppTheme.colors.contentPrimary,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentTrack?.artist ?: "",
                        color = AppTheme.colors.contentPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentTrack != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (currentTrack!!.isPlaying) "▶ 播放中" else "⏸ 已暂停",
                            color = if (currentTrack!!.isPlaying) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Text(
                text = "GMH 仪表盘通过广播接收歌曲信息。安装 GIB 并授权通知监听权限后,播放音乐时此处会自动显示当前歌曲。",
                color = AppTheme.colors.contentPrimary,
                fontSize = 12.sp
            )
        }
    }
}
