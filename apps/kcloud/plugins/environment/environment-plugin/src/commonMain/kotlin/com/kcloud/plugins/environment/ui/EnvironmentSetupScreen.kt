package com.kcloud.plugins.environment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kcloud.plugins.environment.EnvironmentExecutionResult
import com.kcloud.plugins.environment.EnvironmentHostInfo
import com.kcloud.plugins.environment.EnvironmentInstallTarget
import com.kcloud.plugins.environment.EnvironmentPackage
import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.plugins.environment.EnvironmentSetupSettings
import com.kcloud.plugins.ssh.SshAuthMode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EnvironmentSetupScreen(
    service: EnvironmentSetupService = koinInject()
) {
    val scope = rememberCoroutineScope()
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var settings by remember { mutableStateOf(service.loadSettings()) }
    var hostInfo by remember { mutableStateOf<EnvironmentHostInfo?>(null) }
    var result by remember { mutableStateOf<EnvironmentExecutionResult?>(null) }
    var status by remember { mutableStateOf("仅支持 Unix 环境，本机执行或通过 SSH 到远程 Unix 主机执行安装脚本") }
    var busy by remember { mutableStateOf(false) }

    fun updatePackages(environmentPackage: EnvironmentPackage, enabled: Boolean) {
        val nextPackages = settings.selectedPackages.toMutableList().apply {
            if (enabled) {
                if (environmentPackage !in this) {
                    add(environmentPackage)
                }
            } else {
                remove(environmentPackage)
            }
        }.distinct().sortedBy { item -> item.ordinal }
        settings = settings.copy(selectedPackages = nextPackages)
    }

    fun copyText(label: String, value: String) {
        if (value.isBlank()) {
            status = "$label 为空，未复制"
            return
        }

        clipboardManager.setText(AnnotatedString(value))
        status = "$label 已复制到剪贴板"
    }

    fun runAction(
        action: suspend (EnvironmentSetupSettings) -> Unit
    ) {
        scope.launch {
            busy = true
            try {
                val savedSettings = service.saveSettings(settings)
                settings = savedSettings
                action(savedSettings)
            } catch (throwable: Throwable) {
                val message = throwable.message ?: throwable::class.simpleName ?: "操作失败"
                status = message
                result = EnvironmentExecutionResult(
                    success = false,
                    installTarget = settings.installTarget,
                    message = message,
                    output = throwable.stackTraceToString(),
                    hostInfo = hostInfo
                )
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("环境搭建", style = MaterialTheme.typography.headlineSmall)
        Text(
            "内置 JDK / Git / MySQL / PostgreSQL / Redis / Nginx / Docker / Node.js / pnpm 安装脚本；远程模式通过 SSH 会话执行，不做端口转发。",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        InstallTargetSelector(
            selectedTarget = settings.installTarget,
            enabled = !busy,
            onTargetSelected = { installTarget ->
                settings = settings.copy(installTarget = installTarget)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Checkbox(
                    checked = settings.useSudo,
                    enabled = !busy,
                    onCheckedChange = { checked ->
                        settings = settings.copy(useSudo = checked)
                    }
                )
                Text("优先使用 sudo")
            }
        }

        PackageSelectionSection(
            selectedPackages = settings.selectedPackages,
            enabled = !busy,
            onPackageChecked = ::updatePackages
        )

        if (settings.installTarget == EnvironmentInstallTarget.SSH) {
            SshSettingsSection(
                settings = settings,
                enabled = !busy,
                onSettingsChanged = { updatedSettings ->
                    settings = updatedSettings
                }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !busy,
                onClick = {
                    runAction { savedSettings ->
                        val inspected = service.inspectEnvironment(savedSettings)
                        hostInfo = inspected
                        result = null
                        status = inspected.message
                    }
                }
            ) {
                Text("探测环境")
            }
            Button(
                enabled = !busy,
                onClick = {
                    runAction { savedSettings ->
                        val preview = service.previewInstall(savedSettings)
                        hostInfo = preview.hostInfo
                        result = preview
                        status = preview.message
                    }
                }
            ) {
                Text("预览脚本")
            }
            Button(
                enabled = !busy,
                onClick = {
                    runAction { savedSettings ->
                        val installResult = service.install(savedSettings)
                        hostInfo = installResult.hostInfo
                        result = installResult
                        status = installResult.message
                    }
                }
            ) {
                Text(if (busy) "执行中..." else "一键安装")
            }
        }

        hostInfo?.let { currentHostInfo ->
            HostInfoSection(currentHostInfo)
        }
        result?.let { currentResult ->
            ResultSection(
                result = currentResult,
                onCopyScript = { copyText("脚本", currentResult.script) },
                onCopyOutput = { copyText("输出", currentResult.output) }
            )
        }
    }
}

@Composable
private fun InstallTargetSelector(
    selectedTarget: EnvironmentInstallTarget,
    enabled: Boolean,
    onTargetSelected: (EnvironmentInstallTarget) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            enabled = enabled,
            onClick = {
                onTargetSelected(EnvironmentInstallTarget.LOCAL)
            }
        ) {
            Text(if (selectedTarget == EnvironmentInstallTarget.LOCAL) "本机 ✓" else "本机")
        }
        Button(
            enabled = enabled,
            onClick = {
                onTargetSelected(EnvironmentInstallTarget.SSH)
            }
        ) {
            Text(if (selectedTarget == EnvironmentInstallTarget.SSH) "SSH 远程 ✓" else "SSH 远程")
        }
    }
}

@Composable
private fun PackageSelectionSection(
    selectedPackages: List<EnvironmentPackage>,
    enabled: Boolean,
    onPackageChecked: (EnvironmentPackage, Boolean) -> Unit
) {
    Text("选择组件", style = MaterialTheme.typography.titleMedium)
    environmentPackageOptions().forEach { item ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(
                checked = item.packageType in selectedPackages,
                enabled = enabled,
                onCheckedChange = { checked ->
                    onPackageChecked(item.packageType, checked)
                }
            )
            Column {
                Text(item.title)
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SshSettingsSection(
    settings: EnvironmentSetupSettings,
    enabled: Boolean,
    onSettingsChanged: (EnvironmentSetupSettings) -> Unit
) {
    Text("SSH 连接", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = settings.sshConfig.host,
        enabled = enabled,
        onValueChange = { value ->
            onSettingsChanged(settings.copy(sshConfig = settings.sshConfig.copy(host = value)))
        },
        label = { Text("Host") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = settings.sshConfig.port.toString(),
        enabled = enabled,
        onValueChange = { value ->
            onSettingsChanged(
                settings.copy(
                    sshConfig = settings.sshConfig.copy(port = value.toIntOrNull() ?: 22)
                )
            )
        },
        label = { Text("Port") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = settings.sshConfig.username,
        enabled = enabled,
        onValueChange = { value ->
            onSettingsChanged(settings.copy(sshConfig = settings.sshConfig.copy(username = value)))
        },
        label = { Text("用户名") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            enabled = enabled,
            onClick = {
                onSettingsChanged(
                    settings.copy(
                        sshConfig = settings.sshConfig.copy(
                            authMode = if (settings.sshConfig.authMode == SshAuthMode.PASSWORD) {
                                SshAuthMode.PRIVATE_KEY
                            } else {
                                SshAuthMode.PASSWORD
                            }
                        )
                    )
                )
            }
        ) {
            Text("认证：${if (settings.sshConfig.authMode == SshAuthMode.PASSWORD) "密码" else "私钥"}")
        }
    }

    when (settings.sshConfig.authMode) {
        SshAuthMode.PASSWORD -> {
            OutlinedTextField(
                value = settings.sshConfig.password,
                enabled = enabled,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = { value ->
                    onSettingsChanged(settings.copy(sshConfig = settings.sshConfig.copy(password = value)))
                },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SshAuthMode.PRIVATE_KEY -> {
            OutlinedTextField(
                value = settings.sshConfig.privateKeyPath,
                enabled = enabled,
                onValueChange = { value ->
                    onSettingsChanged(
                        settings.copy(
                            sshConfig = settings.sshConfig.copy(privateKeyPath = value)
                        )
                    )
                },
                label = { Text("私钥路径") },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "这里填的是当前这台运行 KCloud 的机器上的私钥文件路径。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HostInfoSection(info: EnvironmentHostInfo) {
    Text("目标环境", style = MaterialTheme.typography.titleMedium)
    Text("模式：${if (info.installTarget == EnvironmentInstallTarget.LOCAL) "本机" else "SSH 远程"}")
    Text("Unix：${if (info.unix) "是" else "否"}")
    Text("系统：${info.osName}")
    Text("主机：${info.hostName}")
    Text("用户：${info.currentUser}")
    Text("工作目录：${info.workingDirectory}")
    Text("包管理器：${info.packageManager}")
}

@Composable
private fun ResultSection(
    result: EnvironmentExecutionResult,
    onCopyScript: () -> Unit,
    onCopyOutput: () -> Unit
) {
    Text("执行结果", style = MaterialTheme.typography.titleMedium)
    Text("成功：${if (result.success) "是" else "否"}")
    Text(result.message)

    if (result.hints.isNotEmpty()) {
        Text("后续提示", style = MaterialTheme.typography.titleSmall)
        result.hints.forEach { hint ->
            Text("• $hint", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (result.script.isNotBlank()) {
        TextBlockSection(
            title = "脚本预览",
            text = result.script,
            copyButtonText = "复制脚本",
            onCopy = onCopyScript
        )
    }

    if (result.output.isNotBlank()) {
        TextBlockSection(
            title = "执行输出",
            text = result.output,
            copyButtonText = "复制输出",
            onCopy = onCopyOutput
        )
    }
}

@Composable
private fun TextBlockSection(
    title: String,
    text: String,
    copyButtonText: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Button(onClick = onCopy) {
            Text(copyButtonText)
        }
    }
    SelectionContainer {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private data class EnvironmentPackageOption(
    val packageType: EnvironmentPackage,
    val title: String,
    val description: String
)

private fun environmentPackageOptions(): List<EnvironmentPackageOption> {
    return listOf(
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.JDK17,
            title = "JDK 17",
            description = "适合较老的 Java 服务和多数 LTS 项目"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.JDK21,
            title = "JDK 21",
            description = "当前更常见的新 LTS 选择"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.GIT,
            title = "Git",
            description = "常用版本控制工具"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.MYSQL,
            title = "MySQL",
            description = "安装 MySQL 服务"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.POSTGRESQL,
            title = "PostgreSQL",
            description = "安装 PostgreSQL 服务"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.REDIS,
            title = "Redis",
            description = "安装 Redis 服务"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.NGINX,
            title = "Nginx",
            description = "安装常见反向代理与静态站服务"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.DOCKER,
            title = "Docker",
            description = "安装容器运行环境；Homebrew 模式会一并准备 Colima"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.NODEJS,
            title = "Node.js",
            description = "安装 Node.js 运行时和 npm"
        ),
        EnvironmentPackageOption(
            packageType = EnvironmentPackage.PNPM,
            title = "pnpm",
            description = "自动补齐 Node.js 依赖后安装 pnpm"
        )
    )
}
