package com.kcloud.features.packages.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.features.packages.ManagedPackageItem
import com.kcloud.features.packages.PackageOrganizerService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PackageOrganizerScreen(
    service: PackageOrganizerService = koinInject()
) {
    val scope = rememberCoroutineScope()
    var scanDirectory by remember { mutableStateOf("") }
    var targetDirectory by remember { mutableStateOf("") }
    var packages by remember { mutableStateOf<List<ManagedPackageItem>>(emptyList()) }
    var status by remember { mutableStateOf("准备扫描安装包目录") }

    fun refresh() {
        packages = service.scanPackages()
        status = "发现 ${packages.size} 个安装包候选项"
    }

    LaunchedEffect(Unit) {
        val settings = service.loadSettings()
        scanDirectory = settings.scanDirectory
        targetDirectory = settings.targetDirectory
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("安装包归纳管理", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = scanDirectory,
            onValueChange = { scanDirectory = it },
            label = { Text("扫描目录") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = targetDirectory,
            onValueChange = { targetDirectory = it },
            label = { Text("归档目录") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    service.saveSettings(
                        service.loadSettings().copy(
                            scanDirectory = scanDirectory,
                            targetDirectory = targetDirectory
                        )
                    )
                    refresh()
                }
            ) {
                Text("保存配置")
            }
            Button(onClick = ::refresh) {
                Text("重新扫描")
            }
            Button(
                onClick = {
                    scope.launch {
                        val result = service.organizePackages()
                        refresh()
                        status = buildString {
                            append("已移动 ${result.movedCount} 个，跳过 ${result.skippedCount} 个")
                            if (result.errors.isNotEmpty()) {
                                append("，错误 ${result.errors.size} 个")
                            }
                        }
                    }
                }
            ) {
                Text("一键归档")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(packages, key = { item -> item.path }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text(item.path, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "分类：${item.category} · 扩展名：${item.extension} · 大小：${item.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
