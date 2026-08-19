package com.salat.gbinder.features.gmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.salat.gbinder.gmp.GmpPermissionHelper
import com.salat.gbinder.ui.theme.AppTheme
import android.content.Context
import androidx.compose.ui.platform.LocalContext

/**
 * GMP 在线音乐服务设置页 - 开关 + 权限引导 + 状态
 */
@Composable
fun RenderGMPSettings(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(true) }
    var notificationGranted by remember { mutableStateOf(false) }

    // 检测通知监听权限
    LaunchedEffect(Unit) {
        notificationGranted = GmpPermissionHelper.isNotificationAccessGranted(context)
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
                text = "GMP 在线音乐",
                style = AppTheme.typography.stubTitle,
                color = AppTheme.colors.contentPrimary
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 服务开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("在线音乐服务", color = AppTheme.colors.contentPrimary, fontSize = 16.sp)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            // 通知监听权限状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22000000))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("通知监听权限", color = AppTheme.colors.contentPrimary, fontSize = 16.sp)
                            Text(
                                text = if (notificationGranted) "已授权" else "未授权 - 需要此权限捕获第三方App播放",
                                color = if (notificationGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                                fontSize = 12.sp
                            )
                        }
                        if (!notificationGranted) {
                            Button(
                                onClick = {
                                    GmpPermissionHelper.openNotificationAccessSettings(context)
                                }
                            ) {
                                Text("去授权")
                            }
                        }
                    }
                }
            }

            Text(
                text = "GMP 在线音乐服务让车机通过 IMusicManager 控制 GIB 播放。需授权通知监听权限以捕获第三方音乐 App 的播放状态。",
                color = AppTheme.colors.contentPrimary,
                fontSize = 12.sp
            )
        }
    }
}
