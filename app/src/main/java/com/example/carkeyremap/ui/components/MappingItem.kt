package com.example.carkeyremap.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carkeyremap.model.ActionType
import com.example.carkeyremap.model.KeyMapping

@Composable
fun MappingItem(
    mapping: KeyMapping,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mapping.sourceKeyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when (mapping.actionType) {
                            ActionType.KEY_EVENT -> "→ ${mapping.targetKeyName ?: "未知按键"}"
                            ActionType.SCREEN_CLICK -> "→ 点击 (${mapping.clickX}, ${mapping.clickY})"
                            ActionType.GESTURE -> "→ 手势动作"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = mapping.isEnabled,
                            onCheckedChange = onToggle
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (mapping.isEnabled) "已启用" else "已禁用",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除映射",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
