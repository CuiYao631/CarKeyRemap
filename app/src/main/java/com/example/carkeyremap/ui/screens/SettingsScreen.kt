package com.example.carkeyremap.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carkeyremap.service.KeepAliveService
import com.example.carkeyremap.service.KeyRemapAccessibilityService
import com.example.carkeyremap.utils.PermissionUtils
import com.example.carkeyremap.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    var autoStartEnabled by remember { mutableStateOf(preferencesManager.isAutoStartEnabled()) }
    var keepAliveServiceRunning by remember { mutableStateOf(KeepAliveService.isRunning()) }
    var accessibilityServiceEnabled by remember { 
        mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) 
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "应用设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 服务状态卡片
        item {
            ServiceStatusCard(
                accessibilityEnabled = accessibilityServiceEnabled,
                keepAliveRunning = keepAliveServiceRunning,
                onOpenAccessibilitySettings = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                onRefreshStatus = {
                    accessibilityServiceEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
                    keepAliveServiceRunning = KeepAliveService.isRunning()
                }
            )
        }
        
        // 自动启动设置
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "开机自动启动",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "设备重启后自动启动按键重映射服务",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = autoStartEnabled,
                            onCheckedChange = { enabled ->
                                autoStartEnabled = enabled
                                preferencesManager.setAutoStartEnabled(enabled)
                                
                                if (enabled) {
                                    // 启动保活服务
                                    val serviceIntent = Intent(context, KeepAliveService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                    keepAliveServiceRunning = true
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // 电池优化设置
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "电池优化设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "为了确保服务能够持续运行，建议关闭电池优化",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("请求忽略电池优化")
                    }
                }
            }
        }
        
        // 权限检查
        item {
            PermissionsCard()
        }
        
        // 应用信息
        item {
            AppInfoCard()
        }
    }
}

@Composable
fun ServiceStatusCard(
    accessibilityEnabled: Boolean,
    keepAliveRunning: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (accessibilityEnabled && keepAliveRunning) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "服务状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "无障碍服务: ${if (accessibilityEnabled) "已启用" else "未启用"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "后台服务: ${if (keepAliveRunning) "运行中" else "未运行"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row {
                    IconButton(onClick = onRefreshStatus) {
                        Icon(Icons.Default.Info, contentDescription = "刷新状态")
                    }
                    
                    if (!accessibilityEnabled) {
                        Button(onClick = onOpenAccessibilitySettings) {
                            Text("设置")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsCard() {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "权限状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            PermissionItem(
                name = "无障碍服务",
                granted = PermissionUtils.isAccessibilityServiceEnabled(context)
            )
            
            PermissionItem(
                name = "悬浮窗权限",
                granted = PermissionUtils.canDrawOverlays(context)
            )
            
            PermissionItem(
                name = "安全设置权限",
                granted = PermissionUtils.canWriteSecureSettings(context)
            )
        }
    }
}

@Composable
fun PermissionItem(
    name: String,
    granted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = if (granted) "已授权" else "未授权",
            style = MaterialTheme.typography.bodySmall,
            color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "应用信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoItem("版本", "1.0.0")
            InfoItem("目标Android版本", "API 34 (Android 14)")
            InfoItem("最低支持版本", "API 21 (Android 5.0)")
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
