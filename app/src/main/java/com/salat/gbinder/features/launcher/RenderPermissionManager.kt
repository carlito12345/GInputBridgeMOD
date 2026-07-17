package com.salat.gbinder.features.launcher
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.gbinder.adb.domain.repository.AdbRepository
import com.salat.gbinder.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RenderPermissionManagerDialog(
    packageName: String,
    appName: String,
    adb: AdbRepository,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit,
    autoGrant: Boolean = false
) {
    var permissions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedPerms by remember { mutableStateOf<Set<String>>(emptySet()) }
    var granting by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        val rawPerms = adb.listAppPermissions(packageName).toMutableList()
        // Add special BIND permissions
        rawPerms.add("BIND_ACCESSIBILITY_SERVICE" to "special")
        rawPerms.add("BIND_NOTIFICATION_LISTENER_SERVICE" to "special")
        permissions = rawPerms.map { (name, status) -> name to status }
        selectedPerms = rawPerms.map { it.first }.toSet()
        loading = false
        if (autoGrant && rawPerms.isNotEmpty()) {
            granting = true
            val regularPerms = selectedPerms.filter { !it.startsWith("BIND_") }
            val specialPerms = selectedPerms.filter { it.startsWith("BIND_") }
            var result = ""
            if (regularPerms.isNotEmpty()) {
                result = adb.grantPermissions(packageName, regularPerms)
            }
            // Grant special permissions via ADB/root
            for (perm in specialPerms) {
                val r = grantSpecialPermission(adb, packageName, perm)
                result += "\n$r"
            }
            granting = false
            onResult("授权完成: $result")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surfaceSettingsLayer1)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                text = "权限管理 - $appName",
                style = AppTheme.typography.overlayLauncherSettingsGroup,
                color = AppTheme.colors.contentAccent
            )

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val tabs = listOf("运行时权限 (${permissions.size})")
                tabs.forEachIndexed { i, title ->
                    val selected = true
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) AppTheme.colors.contentAccent.copy(.2f) else Color.Transparent)
                            
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) AppTheme.colors.contentAccent else AppTheme.colors.contentPrimary,
                            style = AppTheme.typography.overlayLauncherSettingsTitle
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (loading) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "正在获取...",
                    style = AppTheme.typography.overlayLauncherSettingsTitle,
                    color = AppTheme.colors.contentPrimary
                )
            } else {
                if (permissions.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(20.dp),
                        text = "未找到运行时权限",
                        style = AppTheme.typography.overlayLauncherSettingsTitle,
                        color = AppTheme.colors.contentPrimary
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPerms = if (selectedPerms.size == permissions.size) emptySet()
                                else permissions.map { it.first }.toSet()
                            }
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (selectedPerms.size == permissions.size) "取消全选" else "全选",
                            color = AppTheme.colors.contentAccent,
                            style = AppTheme.typography.overlayLauncherSettingsTitle)
                        Spacer(Modifier.weight(1f))
                        Text("${selectedPerms.size}/${permissions.size}",
                            color = AppTheme.colors.contentPrimary.copy(.7f),
                            style = AppTheme.typography.overlayLauncherSettingsSubtitle)
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        itemsIndexed(permissions) { _, (perm, status) ->
                            val checked = perm in selectedPerms
                            val info = PERMISSION_LIST[perm]; val name = when (perm) {
                                "BIND_ACCESSIBILITY_SERVICE" -> "无障碍服务 (Accessibility)"
                                "BIND_NOTIFICATION_LISTENER_SERVICE" -> "通知监听 (Notification Listener)"
                                else -> info?.title ?: perm.split(".").lastOrNull() ?: perm
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { selectedPerms = if (checked) selectedPerms - perm else selectedPerms + perm }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = { c ->
                                    selectedPerms = if (c) selectedPerms + perm else selectedPerms - perm
                                })
                                Spacer(Modifier.width(8.dp))
                                Text(name, style = AppTheme.typography.overlayLauncherSettingsTitle,
                                    color = AppTheme.colors.contentPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = when (status) {
                                            "granted" -> "已授权"
                                            "denied" -> "未授权"
                                            "special" -> "BIND权限"
                                            else -> "已声明"
                                        },
                                        color = when (status) {
                                            "granted" -> AppTheme.colors.greenAccent
                                            "denied" -> AppTheme.colors.deleteButton
                                            else -> AppTheme.colors.contentPrimary.copy(.7f)
                                        },
                                    style = AppTheme.typography.overlayLauncherSettingsSubtitle
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End) {
                        Text(modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onDismiss)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                            text = "关闭", color = AppTheme.colors.contentAccent,
                            style = AppTheme.typography.overlayLauncherSettingsTitle)
                        Spacer(Modifier.width(12.dp))
                        Text(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(if (!granting && selectedPerms.isNotEmpty()) AppTheme.colors.greenAccent else AppTheme.colors.contentPrimary.copy(.3f))
                            .clickable(enabled = !granting && selectedPerms.isNotEmpty()) {
                                granting = true
                                scope.launch {
                                    val regularPerms = selectedPerms.filter { !it.startsWith("BIND_") }
                                    val specialPerms = selectedPerms.filter { it.startsWith("BIND_") }
                                    var result = ""
                                    if (regularPerms.isNotEmpty()) {
                                        result = adb.grantPermissions(packageName, regularPerms)
                                    }
                                    for (perm in specialPerms) {
                                        val r = grantSpecialPermission(adb, packageName, perm)
                                        result += "\n$r"
                                    }
                                    onResult(result)
                                }
                            }.padding(horizontal = 20.dp, vertical = 10.dp),
                            text = if (granting) "授权中..." else "授权选中 (${selectedPerms.size})",
                            color = Color.White, style = AppTheme.typography.overlayLauncherSettingsTitle)
                    }
                }

        }
    }
}
}

private suspend fun grantSpecialPermission(adb: com.salat.gbinder.adb.domain.repository.AdbRepository, packageName: String, permission: String): String {
    return when (permission) {
        "BIND_ACCESSIBILITY_SERVICE" -> {
            // Find the accessibility service component
            val services = adb.execute("pm dump $packageName | grep -A1 'accessibility' | grep 'Service'").trim()
            val componentName = if (services.isNotEmpty()) {
                val svcName = services.lines().firstOrNull()?.trim()?.split(" ")?.lastOrNull() ?: "BootAccessibilityService"
                "$packageName/$packageName.$svcName"
            } else {
                "$packageName/$packageName.BootAccessibilityService"
            }
            val result = adb.execute("settings put secure enabled_accessibility_services $componentName")
            adb.execute("settings put secure accessibility_enabled 1")
            "无障碍服务: $componentName 已启用"
        }
        "BIND_NOTIFICATION_LISTENER_SERVICE" -> {
            // Find the notification listener service component
            val services = adb.execute("pm dump $packageName | grep -A1 'notification' | grep -i 'listener\\|Service'").trim()
            val componentName = if (services.isNotEmpty()) {
                val svcName = services.lines().firstOrNull()?.trim()?.split(" ")?.lastOrNull() ?: "MediaNotificationListenerService"
                "$packageName/$packageName.$svcName"
            } else {
                "$packageName/$packageName.MediaNotificationListenerService"
            }
            val result = adb.execute("cmd notification allow_listener $componentName")
            "通知监听服务: $componentName 已授权"
        }
        else -> "未知权限: $permission"
    }
}


