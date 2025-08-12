package com.example.carkeyremap.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * 权限和服务状态检查工具类
 */
object PermissionUtils {
    
    /**
     * 检查无障碍服务是否已启用
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val packageName = context.packageName
        val serviceName = "$packageName/.service.KeyRemapAccessibilityService"
        
        return enabledServices.any { service ->
            service.id == serviceName
        }
    }
    
    /**
     * 检查是否有系统弹窗权限
     */
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    /**
     * 检查是否有写入安全设置权限
     */
    fun canWriteSecureSettings(context: Context): Boolean {
        return try {
            val resolver = context.contentResolver
            Settings.Secure.putString(resolver, "test_permission", "test")
            Settings.Secure.getString(resolver, "test_permission")
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
