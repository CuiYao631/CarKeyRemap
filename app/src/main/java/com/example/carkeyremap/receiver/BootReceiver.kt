package com.example.carkeyremap.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.carkeyremap.service.KeepAliveService
import com.example.carkeyremap.utils.PreferencesManager

/**
 * 开机启动广播接收器
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received broadcast: $action")
        
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.i(TAG, "Boot completed or package replaced, starting services")
                startServices(context)
            }
        }
    }
    
    private fun startServices(context: Context) {
        try {
            val preferencesManager = PreferencesManager(context)
            
            // 检查是否启用了自动启动
            if (preferencesManager.isAutoStartEnabled()) {
                Log.d(TAG, "Auto-start is enabled, starting keep alive service")
                
                // 启动保活服务
                val serviceIntent = Intent(context, KeepAliveService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // 发送广播通知应用已自动启动
                val notificationIntent = Intent("com.example.carkeyremap.AUTO_STARTED")
                context.sendBroadcast(notificationIntent)
                
                Log.i(TAG, "Services started successfully")
            } else {
                Log.d(TAG, "Auto-start is disabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start services", e)
        }
    }
}
