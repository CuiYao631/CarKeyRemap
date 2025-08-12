package com.example.carkeyremap.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.carkeyremap.model.ActionType
import com.example.carkeyremap.model.KeyCodes
import com.example.carkeyremap.model.KeyMapping
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMappingDialog(
    onDismiss: () -> Unit,
    onConfirm: (KeyMapping) -> Unit
) {
    var selectedSourceKey by remember { mutableStateOf(KeyCodes.VOLUME_UP) }
    var selectedActionType by remember { mutableStateOf(ActionType.SCREEN_CLICK) }
    var selectedTargetKey by remember { mutableStateOf(KeyCodes.BACK) }
    var clickX by remember { mutableStateOf("") }
    var clickY by remember { mutableStateOf("") }
    
    var sourceKeyExpanded by remember { mutableStateOf(false) }
    var actionTypeExpanded by remember { mutableStateOf(false) }
    var targetKeyExpanded by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "添加按键映射",
                    style = MaterialTheme.typography.titleLarge
                )
                
                // 源按键选择
                ExposedDropdownMenuBox(
                    expanded = sourceKeyExpanded,
                    onExpandedChange = { sourceKeyExpanded = !sourceKeyExpanded }
                ) {
                    OutlinedTextField(
                        value = KeyCodes.getKeyName(selectedSourceKey),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("源按键") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceKeyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sourceKeyExpanded,
                        onDismissRequest = { sourceKeyExpanded = false }
                    ) {
                        KeyCodes.keyCodeNames.forEach { (keyCode, keyName) ->
                            DropdownMenuItem(
                                text = { Text(keyName) },
                                onClick = {
                                    selectedSourceKey = keyCode
                                    sourceKeyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // 动作类型选择
                ExposedDropdownMenuBox(
                    expanded = actionTypeExpanded,
                    onExpandedChange = { actionTypeExpanded = !actionTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = when (selectedActionType) {
                            ActionType.KEY_EVENT -> "按键事件"
                            ActionType.SCREEN_CLICK -> "屏幕点击"
                            ActionType.GESTURE -> "手势"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("动作类型") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionTypeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = actionTypeExpanded,
                        onDismissRequest = { actionTypeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("按键事件") },
                            onClick = {
                                selectedActionType = ActionType.KEY_EVENT
                                actionTypeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("屏幕点击") },
                            onClick = {
                                selectedActionType = ActionType.SCREEN_CLICK
                                actionTypeExpanded = false
                            }
                        )
                    }
                }
                
                // 根据动作类型显示不同的配置项
                when (selectedActionType) {
                    ActionType.KEY_EVENT -> {
                        // 目标按键选择
                        ExposedDropdownMenuBox(
                            expanded = targetKeyExpanded,
                            onExpandedChange = { targetKeyExpanded = !targetKeyExpanded }
                        ) {
                            OutlinedTextField(
                                value = KeyCodes.getKeyName(selectedTargetKey),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("目标按键") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetKeyExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = targetKeyExpanded,
                                onDismissRequest = { targetKeyExpanded = false }
                            ) {
                                KeyCodes.keyCodeNames.forEach { (keyCode, keyName) ->
                                    DropdownMenuItem(
                                        text = { Text(keyName) },
                                        onClick = {
                                            selectedTargetKey = keyCode
                                            targetKeyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    ActionType.SCREEN_CLICK -> {
                        // 点击坐标输入
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = clickX,
                                onValueChange = { clickX = it },
                                label = { Text("X坐标") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = clickY,
                                onValueChange = { clickY = it },
                                label = { Text("Y坐标") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    ActionType.GESTURE -> {
                        Text(
                            text = "手势功能暂未实现",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mapping = KeyMapping(
                                id = UUID.randomUUID().toString(),
                                sourceKeyCode = selectedSourceKey,
                                sourceKeyName = KeyCodes.getKeyName(selectedSourceKey),
                                actionType = selectedActionType,
                                targetKeyCode = if (selectedActionType == ActionType.KEY_EVENT) selectedTargetKey else null,
                                targetKeyName = if (selectedActionType == ActionType.KEY_EVENT) KeyCodes.getKeyName(selectedTargetKey) else null,
                                clickX = if (selectedActionType == ActionType.SCREEN_CLICK) clickX.toIntOrNull() else null,
                                clickY = if (selectedActionType == ActionType.SCREEN_CLICK) clickY.toIntOrNull() else null,
                                isEnabled = true
                            )
                            onConfirm(mapping)
                        },
                        enabled = when (selectedActionType) {
                            ActionType.KEY_EVENT -> true
                            ActionType.SCREEN_CLICK -> clickX.toIntOrNull() != null && clickY.toIntOrNull() != null
                            ActionType.GESTURE -> false
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
