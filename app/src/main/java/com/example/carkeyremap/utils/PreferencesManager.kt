package com.example.carkeyremap.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.carkeyremap.model.ActionType
import com.example.carkeyremap.model.KeyMapping
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 配置存储工具类
 */
class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("key_mappings", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_MAPPINGS = "mappings"
        private const val SERVICE_ENABLED = "service_enabled"
        private const val AUTO_START_ENABLED = "auto_start_enabled"
        private const val BATTERY_OPTIMIZATION_REQUESTED = "battery_optimization_requested"
    }
    
    /**
     * 保存按键映射列表
     */
    fun saveKeyMappings(mappings: List<KeyMapping>) {
        val json = gson.toJson(mappings)
        sharedPreferences.edit()
            .putString(KEY_MAPPINGS, json)
            .apply()
    }
    
    /**
     * 获取按键映射列表
     */
    fun getKeyMappings(): List<KeyMapping> {
        val json = sharedPreferences.getString(KEY_MAPPINGS, null)
        return if (json != null) {
            val type = object : TypeToken<List<KeyMapping>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    
    /**
     * 添加新的按键映射
     */
    fun addKeyMapping(mapping: KeyMapping) {
        val mappings = getKeyMappings().toMutableList()
        mappings.add(mapping)
        saveKeyMappings(mappings)
    }
    
    /**
     * 删除按键映射
     */
    fun removeKeyMapping(mappingId: String) {
        val mappings = getKeyMappings().toMutableList()
        mappings.removeAll { it.id == mappingId }
        saveKeyMappings(mappings)
    }
    
    /**
     * 更新按键映射
     */
    fun updateKeyMapping(mapping: KeyMapping) {
        val mappings = getKeyMappings().toMutableList()
        val index = mappings.indexOfFirst { it.id == mapping.id }
        if (index != -1) {
            mappings[index] = mapping
            saveKeyMappings(mappings)
        }
    }
    
    /**
     * 根据源按键代码查找映射
     */
    fun findMappingBySourceKey(keyCode: Int): KeyMapping? {
        return getKeyMappings().find { it.sourceKeyCode == keyCode && it.isEnabled }
    }
    
    /**
     * 设置服务启用状态
     */
    fun setServiceEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(SERVICE_ENABLED, enabled)
            .apply()
    }
    
    /**
     * 获取服务启用状态
     */
    fun isServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(SERVICE_ENABLED, false)
    }
    
    /**
     * 设置自动启动
     */
    fun setAutoStartEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(AUTO_START_ENABLED, enabled)
            .apply()
    }
    
    /**
     * 获取自动启动状态
     */
    fun isAutoStartEnabled(): Boolean {
        return sharedPreferences.getBoolean(AUTO_START_ENABLED, true) // 默认启用
    }
    
    /**
     * 设置电池优化请求状态
     */
    fun setBatteryOptimizationRequested(requested: Boolean) {
        sharedPreferences.edit()
            .putBoolean(BATTERY_OPTIMIZATION_REQUESTED, requested)
            .apply()
    }
    
    /**
     * 获取电池优化请求状态
     */
    fun isBatteryOptimizationRequested(): Boolean {
        return sharedPreferences.getBoolean(BATTERY_OPTIMIZATION_REQUESTED, false)
    }
}
