package site.addzero.kcloud.plugins.rbac

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

@Composable
private fun RbacPlaceholderGroup(
    title: String,
    lines: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            lines.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Single(binds = [Screen::class])
class UserScreen : Screen {
    override val id = "rbac"
    override val name = "RBAC权限管理"
    override val icon = Icons.Default.AdminPanelSettings
    override val sort = 20
    override val content: (@Composable () -> Unit) = {
        Card(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "系统 RBAC",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "先把模型脚手架占住，后续再接角色、权限点、资源范围和审计流。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RbacPlaceholderGroup(
                    title = "第一期骨架",
                    lines = listOf(
                        "角色模板：超级管理员 / 运维 / 只读访客",
                        "权限域：主机、Compose、文件、笔记、系统设置",
                        "范围模型：全局 / 场景 / 资源实例",
                    ),
                )
                RbacPlaceholderGroup(
                    title = "后续落地",
                    lines = listOf(
                        "成员与角色绑定",
                        "权限矩阵编辑器",
                        "操作审计与变更记录",
                    ),
                )
            }
        }
    }
}
