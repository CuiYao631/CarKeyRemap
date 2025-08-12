package com.example.carkeyremap.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager

/**
 * 诊断工具类，用于检查服务状态和权限
 */
object DiagnosticUtils {
    private const val TAG = "DiagnosticUtils"
    
    /**
     * 全面诊断服务状态
     */
    fun diagnoseService(context: Context): DiagnosticResult {
        val result = DiagnosticResult()
        
        // 检查无障碍服务是否启用
        result.accessibilityServiceEnabled = isAccessibilityServiceEnabled(context)
        Log.d(TAG, "Accessibility service enabled: ${result.accessibilityServiceEnabled}")
        
        // 检查具体的服务配置
        if (result.accessibilityServiceEnabled) {
            val serviceInfo = getServiceInfo(context)
            result.canFilterKeyEvents = serviceInfo?.flags?.and(AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS) != 0
            result.serviceFlags = serviceInfo?.flags ?: 0
            result.eventTypes = serviceInfo?.eventTypes ?: 0
            
            Log.d(TAG, "Service flags: ${result.serviceFlags}")
            Log.d(TAG, "Can filter key events: ${result.canFilterKeyEvents}")
            Log.d(TAG, "Event types: ${result.eventTypes}")
        }
        
        // 检查其他权限
        result.canDrawOverlays = PermissionUtils.canDrawOverlays(context)
        result.canWriteSecureSettings = PermissionUtils.canWriteSecureSettings(context)
        
        Log.d(TAG, "Can draw overlays: ${result.canDrawOverlays}")
        Log.d(TAG, "Can write secure settings: ${result.canWriteSecureSettings}")
        
        return result
    }
    
    /**
     * 检查无障碍服务是否已启用
     */
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val packageName = context.packageName
        val serviceName = "$packageName/.service.KeyRemapAccessibilityService"
        
        return enabledServices.any { service ->
            service.id == serviceName
        }
    }
    
    /**
     * 获取服务信息
     */
    private fun getServiceInfo(context: Context): AccessibilityServiceInfo? {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val packageName = context.packageName
        val serviceName = "$packageName/.service.KeyRemapAccessibilityService"
        
        return enabledServices.find { service ->
            service.id == serviceName
        }
    }
    
    /**
     * 获取推荐的解决方案
     */
    fun getRecommendations(result: DiagnosticResult): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (!result.accessibilityServiceEnabled) {
            recommendations.add("请在系统设置中启用无障碍服务")
            recommendations.add("设置 > 无障碍 > CarKey Remap > 开启服务")
        }
        
        if (!result.canFilterKeyEvents) {
            recommendations.add("无障碍服务未获得按键过滤权限")
            recommendations.add("请确保服务配置正确并重新启用服务")
        }
        
        if (!result.canDrawOverlays) {
            recommendations.add("建议授予悬浮窗权限以获得更好的用户体验")
        }
        
        if (!result.canWriteSecureSettings) {
            recommendations.add("某些高级功能需要系统级权限或root权限")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("所有权限检查通过，如果仍无法监听按键，可能需要：")
            recommendations.add("1. 重启设备")
            recommendations.add("2. 在部分设备上需要关闭电池优化")
            recommendations.add("3. 某些车机系统可能需要特殊配置")
        }
        
        return recommendations
    }
}

/**
 * 诊断结果数据类
 */
data class DiagnosticResult(
    var accessibilityServiceEnabled: Boolean = false,
    var canFilterKeyEvents: Boolean = false,
    var canDrawOverlays: Boolean = false,
    var canWriteSecureSettings: Boolean = false,
    var serviceFlags: Int = 0,
    var eventTypes: Int = 0
)
