package com.example.carkeyremap

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carkeyremap.ui.theme.CarKeyRemapTheme
import com.example.carkeyremap.service.KeyRemapAccessibilityService
import com.example.carkeyremap.utils.PreferencesManager
import com.example.carkeyremap.model.ActionType
import com.example.carkeyremap.model.KeyCodes
import com.example.carkeyremap.model.KeyMapping
import com.example.carkeyremap.ui.components.AddMappingDialog
import com.example.carkeyremap.ui.components.MappingItem
import com.example.carkeyremap.ui.screens.SettingsScreen
import com.example.carkeyremap.ui.theme.CarKeyRemapTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarKeyRemapTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp
    
    // 判断是否使用侧边导航栏（横屏或大屏幕）
    val useNavigationRail = isLandscape || screenWidth > 600
    
    if (useNavigationRail) {
        // 横屏或大屏幕使用侧边导航栏
        LandscapeLayout(navController = navController)
    } else {
        // 竖屏使用底部导航栏
        PortraitLayout(navController = navController)
    }
}

@Composable
fun PortraitLayout(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("映射管理") },
                    selected = currentDestination?.route == "mappings",
                    onClick = {
                        navController.navigate("mappings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("按键检测") },
                    selected = currentDestination?.route == "detection",
                    onClick = {
                        navController.navigate("detection") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("设置") },
                    selected = currentDestination?.route == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun LandscapeLayout(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 侧边导航栏
        NavigationRail(
            modifier = Modifier.fillMaxHeight()
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NavigationRailItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                label = { Text("映射") },
                selected = currentDestination?.route == "mappings",
                onClick = {
                    navController.navigate("mappings") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            
            NavigationRailItem(
                icon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("检测") },
                selected = currentDestination?.route == "detection",
                onClick = {
                    navController.navigate("detection") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            
            NavigationRailItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("设置") },
                selected = currentDestination?.route == "settings",
                onClick = {
                    navController.navigate("settings") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // 主要内容区域
        AppNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "mappings",
        modifier = modifier
    ) {
        composable("mappings") {
            MappingsScreen()
        }
        composable("detection") {
            KeyDetectionScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingsScreen() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    var mappings by remember { mutableStateOf(preferencesManager.getKeyMappings()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceEnabled by remember { 
        mutableStateOf(KeyRemapAccessibilityService.instance?.isServiceRunning() == true) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("车机按键重映射") },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加映射")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 服务状态卡片
            ServiceStatusCard(
                serviceEnabled = serviceEnabled,
                onEnableService = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 映射列表
            if (mappings.isEmpty()) {
                EmptyMappingsView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mappings) { mapping ->
                        MappingItem(
                            mapping = mapping,
                            onDelete = {
                                preferencesManager.removeKeyMapping(mapping.id)
                                mappings = preferencesManager.getKeyMappings()
                            },
                            onToggle = { enabled ->
                                val updatedMapping = mapping.copy(isEnabled = enabled)
                                preferencesManager.updateKeyMapping(updatedMapping)
                                mappings = preferencesManager.getKeyMappings()
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 添加映射对话框
    if (showAddDialog) {
        AddMappingDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { mapping ->
                preferencesManager.addKeyMapping(mapping)
                mappings = preferencesManager.getKeyMappings()
                showAddDialog = false
            }
        )
    }
    
    // 定期检查服务状态
    LaunchedEffect(Unit) {
        while (true) {
            serviceEnabled = KeyRemapAccessibilityService.instance?.isServiceRunning() == true
            kotlinx.coroutines.delay(2000) // 每2秒检查一次
        }
    }
}

@Composable
fun ServiceStatusCard(
    serviceEnabled: Boolean,
    onEnableService: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (serviceEnabled) 
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
                Column {
                    Text(
                        text = "无障碍服务状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (serviceEnabled) "已启用" else "未启用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (serviceEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (!serviceEnabled) {
                    Button(
                        onClick = onEnableService
                    ) {
                        Text("启用服务")
                    }
                }
            }
            
            if (!serviceEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "需要启用无障碍服务才能使用按键重映射功能",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun EmptyMappingsView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "暂无按键映射",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角的 + 按钮添加第一个映射",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    CarKeyRemapTheme {
        MainApp()
    }
}

// 按键检测相关数据类
data class DetectedKey(
    val keyCode: Int,
    val keyName: String,
    val timestamp: Long,
    val mapped: Boolean,
    val actionType: com.example.carkeyremap.model.ActionType? = null,
    val actionDetails: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyDetectionScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val preferencesManager = remember { PreferencesManager(context) }
    var isDetecting by remember { mutableStateOf(false) }
    var detectedKeys by remember { mutableStateOf(listOf<DetectedKey>()) }
    var lastDetectedKey by remember { mutableStateOf<DetectedKey?>(null) }
    var serviceStatus by remember { mutableStateOf("检查中...") }
    
    // 刷新服务状态的函数
    val refreshServiceStatus = {
        serviceStatus = if (isAccessibilityServiceEnabled(context)) {
            if (KeyRemapAccessibilityService.instance != null) {
                "服务运行中"
            } else {
                "服务已启用但未连接"
            }
        } else {
            "服务未启用"
        }
    }
    
    // 检查无障碍服务状态
    LaunchedEffect(Unit) {
        refreshServiceStatus()
    }
    
    // 智能自动刷新服务状态
    LaunchedEffect(serviceStatus) {
        while (true) {
            kotlinx.coroutines.delay(
                if (serviceStatus == "服务运行中") {
                    30000 // 服务运行时每30秒检查一次
                } else {
                    5000  // 服务未运行时每5秒检查一次
                }
            )
            refreshServiceStatus()
        }
    }
    
    // 广播接收器
    val keyDetectionReceiver = remember {
        object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == KeyRemapAccessibilityService.ACTION_KEY_DETECTED) {
                    val keyCode = intent.getIntExtra(KeyRemapAccessibilityService.EXTRA_KEY_CODE, -1)
                    val keyName = intent.getStringExtra(KeyRemapAccessibilityService.EXTRA_KEY_NAME) ?: "未知"
                    val timestamp = intent.getLongExtra(KeyRemapAccessibilityService.EXTRA_TIMESTAMP, System.currentTimeMillis())
                    val mapped = intent.getBooleanExtra(KeyRemapAccessibilityService.EXTRA_MAPPED, false)
                    
                    // 获取映射信息
                    val mapping = if (mapped) preferencesManager.findMappingBySourceKey(keyCode) else null
                    val actionType = mapping?.actionType
                    val actionDetails = mapping?.let { 
                        when (it.actionType) {
                            com.example.carkeyremap.model.ActionType.KEY_EVENT -> "→ ${it.targetKeyCode?.let { code -> 
                                com.example.carkeyremap.model.KeyCodes.getKeyName(code) 
                            } ?: "未知按键"}"
                            com.example.carkeyremap.model.ActionType.SCREEN_CLICK -> "→ 点击 (${it.clickX}, ${it.clickY})"
                            com.example.carkeyremap.model.ActionType.GESTURE -> "→ 手势操作"
                        }
                    }
                    
                    val detectedKey = DetectedKey(keyCode, keyName, timestamp, mapped, actionType, actionDetails)
                    lastDetectedKey = detectedKey
                    detectedKeys = listOf(detectedKey) + detectedKeys.take(19) // 保留最近20个
                }
            }
        }
    }
    
    // 注册和注销广播接收器
    DisposableEffect(isDetecting) {
        if (isDetecting) {
            val filter = android.content.IntentFilter(KeyRemapAccessibilityService.ACTION_KEY_DETECTED)
            context.registerReceiver(keyDetectionReceiver, filter)
        }
        
        onDispose {
            try {
                context.unregisterReceiver(keyDetectionReceiver)
            } catch (e: IllegalArgumentException) {
                // 接收器可能已经注销
            }
        }
    }
    
    // 响应式布局
    if (isLandscape) {
        LandscapeKeyDetectionLayout(
            isDetecting = isDetecting,
            onToggleDetection = { isDetecting = !isDetecting },
            onClearHistory = { 
                detectedKeys = emptyList()
                lastDetectedKey = null
            },
            lastDetectedKey = lastDetectedKey,
            detectedKeys = detectedKeys,
            serviceStatus = serviceStatus,
            context = context,
            onRefreshServiceStatus = refreshServiceStatus
        )
    } else {
        PortraitKeyDetectionLayout(
            isDetecting = isDetecting,
            onToggleDetection = { isDetecting = !isDetecting },
            onClearHistory = { 
                detectedKeys = emptyList()
                lastDetectedKey = null
            },
            lastDetectedKey = lastDetectedKey,
            detectedKeys = detectedKeys,
            serviceStatus = serviceStatus,
            context = context,
            onRefreshServiceStatus = refreshServiceStatus
        )
    }
}

@Composable
fun LandscapeKeyDetectionLayout(
    isDetecting: Boolean,
    onToggleDetection: () -> Unit,
    onClearHistory: () -> Unit,
    lastDetectedKey: DetectedKey?,
    detectedKeys: List<DetectedKey>,
    serviceStatus: String,
    context: android.content.Context,
    onRefreshServiceStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左侧：控制面板和当前检测
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "按键检测器",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 服务状态卡片
            ServiceStatusCard(serviceStatus, context, onRefreshServiceStatus)
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleDetection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDetecting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isDetecting) Icons.Default.Settings else Icons.Default.Search,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isDetecting) "停止" else "开始")
                }
                
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清空")
                }
            }
            
            // 当前检测到的按键
            if (lastDetectedKey != null) {
                CurrentKeyCard(lastDetectedKey)
            }
        }
        
        // 右侧：历史记录
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "检测历史 (${detectedKeys.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            HistoryList(detectedKeys, isDetecting)
        }
    }
}

@Composable
fun PortraitKeyDetectionLayout(
    isDetecting: Boolean,
    onToggleDetection: () -> Unit,
    onClearHistory: () -> Unit,
    lastDetectedKey: DetectedKey?,
    detectedKeys: List<DetectedKey>,
    serviceStatus: String,
    context: android.content.Context,
    onRefreshServiceStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "按键检测器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // 服务状态卡片
        ServiceStatusCard(serviceStatus, context, onRefreshServiceStatus)
        
        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleDetection,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDetecting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (isDetecting) Icons.Default.Settings else Icons.Default.Search,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isDetecting) "停止检测" else "开始检测")
            }
            
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清空记录")
                }
            }
            
            // 测试按钮（仅在服务运行时显示）
            if (serviceStatus == "服务运行中") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 模拟音量加键
                            KeyRemapAccessibilityService.instance?.simulateTestKeyEvent(android.view.KeyEvent.KEYCODE_VOLUME_UP)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("测试音量+", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Button(
                        onClick = {
                            // 模拟返回键
                            KeyRemapAccessibilityService.instance?.simulateTestKeyEvent(android.view.KeyEvent.KEYCODE_BACK)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("测试返回键", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // 当前检测到的按键
            if (lastDetectedKey != null) {
                CurrentKeyCard(lastDetectedKey)
            }
        
        // 历史记录
        Text(
            text = "检测历史 (${detectedKeys.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        HistoryList(detectedKeys, isDetecting)
    }
}

@Composable
fun CurrentKeyCard(key: DetectedKey) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (key.mapped) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "最新检测",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = key.keyName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "按键代码: ${key.keyCode}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            if (key.mapped) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "已映射",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                if (key.actionType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (key.actionType) {
                            com.example.carkeyremap.model.ActionType.KEY_EVENT -> "按键事件"
                            com.example.carkeyremap.model.ActionType.SCREEN_CLICK -> "屏幕点击"
                            com.example.carkeyremap.model.ActionType.GESTURE -> "手势操作"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (key.actionDetails != null) {
                        Text(
                            text = key.actionDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryList(detectedKeys: List<DetectedKey>, isDetecting: Boolean) {
    if (detectedKeys.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDetecting) "等待按键输入..." else "暂无检测记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(detectedKeys) { key ->
                KeyHistoryItem(key = key)
            }
        }
    }
}

@Composable
fun KeyHistoryItem(key: DetectedKey) {
    val timeFormat = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (key.mapped) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                        text = key.keyName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "代码: ${key.keyCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = timeFormat.format(java.util.Date(key.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    if (key.mapped) {
                        Text(
                            text = "已映射",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 显示映射动作信息
            if (key.mapped && key.actionType != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (key.actionType) {
                            com.example.carkeyremap.model.ActionType.KEY_EVENT -> MaterialTheme.colorScheme.secondary
                            com.example.carkeyremap.model.ActionType.SCREEN_CLICK -> MaterialTheme.colorScheme.tertiary
                            com.example.carkeyremap.model.ActionType.GESTURE -> MaterialTheme.colorScheme.primary
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (key.actionType) {
                                com.example.carkeyremap.model.ActionType.KEY_EVENT -> "按键"
                                com.example.carkeyremap.model.ActionType.SCREEN_CLICK -> "点击"
                                com.example.carkeyremap.model.ActionType.GESTURE -> "手势"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (key.actionType) {
                                com.example.carkeyremap.model.ActionType.KEY_EVENT -> MaterialTheme.colorScheme.onSecondary
                                com.example.carkeyremap.model.ActionType.SCREEN_CLICK -> MaterialTheme.colorScheme.onTertiary
                                com.example.carkeyremap.model.ActionType.GESTURE -> MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }
                    
                    if (key.actionDetails != null) {
                        Text(
                            text = key.actionDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 检查无障碍服务是否已启用
 */
fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val serviceName = "${context.packageName}/${KeyRemapAccessibilityService::class.java.name}"
    return enabledServices?.contains(serviceName) == true
}

@Composable
fun ServiceStatusCard(serviceStatus: String, context: android.content.Context, onRefreshServiceStatus: () -> Unit) {
    val isEnabled = serviceStatus == "服务运行中"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
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
                        text = "无障碍服务状态",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = serviceStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                // 刷新按钮
                IconButton(
                    onClick = onRefreshServiceStatus
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "刷新状态",
                        tint = if (isEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // 操作按钮行
            if (!isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // 打开无障碍服务设置页面
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("启用服务", color = MaterialTheme.colorScheme.onError)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请在无障碍设置中启用 'CarKey Remap' 服务来检测按键事件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}