package com.example.carkeyremap.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.carkeyremap.MainActivity
import com.example.carkeyremap.R
import com.example.carkeyremap.utils.PermissionUtils
import kotlinx.coroutines.*

/**
 * 后台保活服务
 */
class KeepAliveService : Service() {
    
    companion object {
        private const val TAG = "KeepAliveService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "keep_alive_channel"
        private const val CHECK_INTERVAL = 30000L // 30秒检查一次
        
        private var instance: KeepAliveService? = null
        
        fun isRunning(): Boolean = instance != null
    }
    
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "KeepAliveService created")
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        startMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "KeepAliveService started")
        return START_STICKY // 服务被杀死后自动重启
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceJob?.cancel()
        Log.d(TAG, "KeepAliveService destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "按键重映射服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持按键重映射功能在后台运行"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("车机按键重映射")
            .setContentText("按键重映射服务正在后台运行")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * 开始监控无障碍服务状态
     */
    private fun startMonitoring() {
        serviceJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkAndRestoreServices()
                    delay(CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                    delay(CHECK_INTERVAL)
                }
            }
        }
    }
    
    /**
     * 检查并恢复服务状态
     */
    private fun checkAndRestoreServices() {
        try {
            // 检查无障碍服务状态
            val accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(this)
            Log.v(TAG, "Accessibility service enabled: $accessibilityEnabled")
            
            if (!accessibilityEnabled) {
                Log.w(TAG, "Accessibility service is not enabled")
                // 发送通知提醒用户启用服务
                sendServiceNotification("无障碍服务未启用", "请在设置中启用按键重映射服务")
            } else {
                // 检查服务实例是否存在
                val serviceRunning = KeyRemapAccessibilityService.instance != null
                Log.v(TAG, "Service instance running: $serviceRunning")
                
                if (!serviceRunning) {
                    Log.w(TAG, "Service instance not found but accessibility is enabled")
                }
            }
            
            // 更新通知状态
            updateNotification(accessibilityEnabled)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check service status", e)
        }
    }
    
    /**
     * 发送服务状态通知
     */
    private fun sendServiceNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    /**
     * 更新前台服务通知
     */
    private fun updateNotification(serviceEnabled: Boolean) {
        val statusText = if (serviceEnabled) {
            "按键重映射服务正在运行"
        } else {
            "按键重映射服务未启用"
        }
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("车机按键重映射")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
