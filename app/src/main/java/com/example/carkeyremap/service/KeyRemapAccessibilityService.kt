package com.example.carkeyremap.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.carkeyremap.model.ActionType
import com.example.carkeyremap.model.KeyCodes
import com.example.carkeyremap.model.KeyMapping
import com.example.carkeyremap.utils.PreferencesManager
import java.util.*

/**
 * 按键重映射无障碍服务
 */
class KeyRemapAccessibilityService : AccessibilityService() {
    
    private lateinit var preferencesManager: PreferencesManager
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "KeyRemapService"
        const val ACTION_KEY_DETECTED = "com.example.carkeyremap.KEY_DETECTED"
        const val ACTION_KEY_MAPPED = "com.example.carkeyremap.KEY_MAPPED"
        const val EXTRA_KEY_CODE = "keyCode"
        const val EXTRA_KEY_NAME = "keyName"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_MAPPED = "mapped"
        
        var instance: KeyRemapAccessibilityService? = null
        
        // 最近检测到的按键列表
        private val recentKeys = mutableListOf<KeyEventInfo>()
        private const val MAX_RECENT_KEYS = 20
    }
    
    data class KeyEventInfo(
        val keyCode: Int,
        val keyName: String,
        val timestamp: Long,
        val mapped: Boolean
    )
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)
        Log.d(TAG, "KeyRemapAccessibilityService created")
        
        // 发送服务启动广播
        sendBroadcast(Intent("com.example.carkeyremap.SERVICE_STARTED"))
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "KeyRemapAccessibilityService connected")
        
        // 获取服务信息以确认配置
        val serviceInfo = serviceInfo
        Log.d(TAG, "Service info: flags=${serviceInfo?.flags}, eventTypes=${serviceInfo?.eventTypes}")
        Log.d(TAG, "Can filter key events: ${serviceInfo?.flags?.and(AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS) != 0}")
        
        // 发送服务连接广播
        sendBroadcast(Intent("com.example.carkeyremap.SERVICE_CONNECTED"))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "KeyRemapAccessibilityService destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 记录所有可访问性事件
        event?.let {
            Log.v(TAG, "Accessibility event: ${it.eventType}, package: ${it.packageName}")
            
            // 尝试从事件中获取按键信息
            if (it.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                it.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                Log.d(TAG, "Relevant accessibility event detected")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    /**
     * 拦截按键事件
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // 记录所有按键事件，包括 ACTION_DOWN 和 ACTION_UP
        Log.d(TAG, "Key event received: keyCode=${event.keyCode}, action=${event.action}, " +
                "deviceId=${event.deviceId}, source=${event.source}")
        
        // 只处理按下事件，避免重复处理
        if (event.action != KeyEvent.ACTION_DOWN) {
            Log.v(TAG, "Ignoring key action: ${event.action} (not ACTION_DOWN)")
            return false
        }
        
        val keyCode = event.keyCode
        val keyName = KeyCodes.getKeyName(keyCode)
        val timestamp = System.currentTimeMillis()
        
        Log.i(TAG, "Key event intercepted: $keyCode ($keyName) at $timestamp")
        
        // 记录特殊按键
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_MENU -> {
                Log.i(TAG, "System key detected: $keyName")
            }
        }
        
        // 查找对应的映射配置
        val mapping = preferencesManager.findMappingBySourceKey(keyCode)
        val isMapped = mapping != null
        
        // 记录按键事件
        recordKeyEvent(keyCode, keyName, timestamp, isMapped)
        
        // 发送广播通知应用按键被检测到
        sendKeyDetectedBroadcast(keyCode, keyName, timestamp, isMapped)
        
        if (mapping != null) {
            Log.d(TAG, "Found mapping for key $keyCode: $mapping")
            handleKeyMapping(mapping)
            return true // 拦截原始按键事件
        }
        
        return false // 不拦截，让系统处理
    }
    
    /**
     * 测试方法：模拟按键事件检测
     */
    fun simulateTestKeyEvent(keyCode: Int) {
        val keyName = KeyCodes.getKeyName(keyCode)
        val timestamp = System.currentTimeMillis()
        
        Log.i(TAG, "Test key event simulated: $keyCode ($keyName)")
        
        // 查找对应的映射配置
        val mapping = preferencesManager.findMappingBySourceKey(keyCode)
        val isMapped = mapping != null
        
        // 记录按键事件
        recordKeyEvent(keyCode, keyName, timestamp, isMapped)
        
        // 发送广播通知应用按键被检测到
        sendKeyDetectedBroadcast(keyCode, keyName, timestamp, isMapped)
    }
    
    /**
     * 记录按键事件
     */
    private fun recordKeyEvent(keyCode: Int, keyName: String, timestamp: Long, mapped: Boolean) {
        synchronized(recentKeys) {
            recentKeys.add(0, KeyEventInfo(keyCode, keyName, timestamp, mapped))
            while (recentKeys.size > MAX_RECENT_KEYS) {
                recentKeys.removeLastOrNull()
            }
        }
    }
    
    /**
     * 获取最近的按键事件
     */
    fun getRecentKeys(): List<KeyEventInfo> {
        synchronized(recentKeys) {
            return recentKeys.toList()
        }
    }
    
    /**
     * 发送按键检测广播
     */
    private fun sendKeyDetectedBroadcast(keyCode: Int, keyName: String, timestamp: Long, mapped: Boolean) {
        val intent = Intent(ACTION_KEY_DETECTED).apply {
            putExtra(EXTRA_KEY_CODE, keyCode)
            putExtra(EXTRA_KEY_NAME, keyName)
            putExtra(EXTRA_TIMESTAMP, timestamp)
            putExtra(EXTRA_MAPPED, mapped)
        }
        sendBroadcast(intent)
    }
    
    /**
     * 处理按键映射
     */
    private fun handleKeyMapping(mapping: KeyMapping) {
        when (mapping.actionType) {
            ActionType.KEY_EVENT -> {
                if (mapping.targetKeyCode != null) {
                    simulateKeyPress(mapping.targetKeyCode)
                }
            }
            ActionType.SCREEN_CLICK -> {
                if (mapping.clickX != null && mapping.clickY != null) {
                    performScreenClick(mapping.clickX, mapping.clickY)
                }
            }
            ActionType.GESTURE -> {
                // 实现手势支持
                performGestureAction(mapping)
            }
        }
        
        // 发送映射执行广播
        sendBroadcast(Intent(ACTION_KEY_MAPPED).apply {
            putExtra("sourceKeyCode", mapping.sourceKeyCode)
            putExtra("actionType", mapping.actionType.name)
            putExtra("successful", true)
        })
    }
    
    /**
     * 执行手势动作
     */
    private fun performGestureAction(mapping: KeyMapping) {
        try {
            when {
                // 如果有点击坐标，执行点击手势
                mapping.clickX != null && mapping.clickY != null -> {
                    performScreenClick(mapping.clickX, mapping.clickY)
                }
                // 可以根据需要添加更多手势类型
                else -> {
                    // 默认执行一个长按手势
                    val displayMetrics = resources.displayMetrics
                    val centerX = displayMetrics.widthPixels / 2
                    val centerY = displayMetrics.heightPixels / 2
                    performLongPress(centerX, centerY)
                }
            }
            Log.d(TAG, "Gesture action performed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform gesture action", e)
        }
    }
    
    /**
     * 执行长按手势
     */
    private fun performLongPress(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 1000) // 1秒长按
        gestureBuilder.addStroke(strokeDescription)
        
        val gesture = gestureBuilder.build()
        
        val callback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Long press completed at ($x, $y)")
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Long press cancelled")
            }
        }
        
        dispatchGesture(gesture, callback, null)
    }
    
    /**
     * 模拟按键按下
     */
    private fun simulateKeyPress(keyCode: Int) {
        try {
            // 使用无障碍服务的全局手势功能来模拟特定按键操作
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d(TAG, "Performed back action")
                }
                KeyEvent.KEYCODE_HOME -> {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    Log.d(TAG, "Performed home action")
                }
                KeyEvent.KEYCODE_MENU -> {
                    // 尝试打开通知栏作为菜单的替代
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                    Log.d(TAG, "Performed menu/notifications action")
                }
                KeyEvent.KEYCODE_POWER -> {
                    performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
                    Log.d(TAG, "Performed power dialog action")
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    // 音量按键需要通过 AudioManager 处理
                    simulateVolumeKey(true)
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    simulateVolumeKey(false)
                }
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    // 方向键通过模拟滑动手势实现
                    simulateDirectionKey(keyCode)
                }
                else -> {
                    // 对于其他按键，尝试通过输入法或者发送广播
                    simulateOtherKey(keyCode)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to simulate key press: $keyCode", e)
        }
    }
    
    /**
     * 模拟音量按键
     */
    private fun simulateVolumeKey(volumeUp: Boolean) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (volumeUp) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
            } else {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
            }
            Log.d(TAG, "Volume ${if (volumeUp) "up" else "down"} simulated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to simulate volume key", e)
        }
    }
    
    /**
     * 模拟方向键
     */
    private fun simulateDirectionKey(keyCode: Int) {
        try {
            val displayMetrics = resources.displayMetrics
            val centerX = displayMetrics.widthPixels / 2f
            val centerY = displayMetrics.heightPixels / 2f
            val distance = 200f // 滑动距离
            
            val coordinates = when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    arrayOf(centerX, centerY + distance, centerX, centerY - distance)
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    arrayOf(centerX, centerY - distance, centerX, centerY + distance)
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    arrayOf(centerX + distance, centerY, centerX - distance, centerY)
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    arrayOf(centerX - distance, centerY, centerX + distance, centerY)
                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    // 中心键作为点击处理
                    performScreenClick(centerX.toInt(), centerY.toInt())
                    return
                }
                else -> return
            }
            
            val startX = coordinates[0].toInt()
            val startY = coordinates[1].toInt()
            val endX = coordinates[2].toInt()
            val endY = coordinates[3].toInt()
            
            performSwipeGesture(startX, startY, endX, endY)
            Log.d(TAG, "Direction key simulated: ${KeyCodes.getKeyName(keyCode)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to simulate direction key", e)
        }
    }
    
    /**
     * 模拟其他按键
     */
    private fun simulateOtherKey(keyCode: Int) {
        try {
            // 发送广播让其他组件处理
            sendBroadcast(Intent("com.example.carkeyremap.SIMULATE_KEY")
                .putExtra("keyCode", keyCode)
                .putExtra("keyName", KeyCodes.getKeyName(keyCode)))
            
            Log.d(TAG, "Broadcasted key simulation: ${KeyCodes.getKeyName(keyCode)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast key simulation", e)
        }
    }
    
    /**
     * 执行滑动手势
     */
    private fun performSwipeGesture(startX: Int, startY: Int, endX: Int, endY: Int) {
        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 300)
        gestureBuilder.addStroke(strokeDescription)
        
        val gesture = gestureBuilder.build()
        
        val callback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Swipe gesture completed")
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Swipe gesture cancelled")
            }
        }
        
        dispatchGesture(gesture, callback, null)
    }
    
    /**
     * 执行屏幕点击
     */
    private fun performScreenClick(x: Int, y: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 100)
        gestureBuilder.addStroke(strokeDescription)
        
        val gesture = gestureBuilder.build()
        
        val callback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Screen click completed at ($x, $y)")
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Screen click cancelled")
            }
        }
        
        dispatchGesture(gesture, callback, handler)
    }
    
    /**
     * 检查服务是否正在运行
     */
    fun isServiceRunning(): Boolean {
        return instance != null
    }
}
