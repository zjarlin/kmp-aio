package com.kcloud.plugins.environment.server

import com.kcloud.plugin.KCloudLocalPaths
import com.kcloud.plugin.readKCloudJson
import com.kcloud.plugin.writeKCloudJson
import com.kcloud.plugins.environment.EnvironmentExecutionResult
import com.kcloud.plugins.environment.EnvironmentHostInfo
import com.kcloud.plugins.environment.EnvironmentInstallTarget
import com.kcloud.plugins.environment.EnvironmentPackage
import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.plugins.environment.EnvironmentSetupSettings
import com.kcloud.plugins.ssh.SshAuthMode
import com.kcloud.plugins.ssh.SshConnectionConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File
import java.util.concurrent.TimeUnit
import org.koin.core.annotation.Single

private const val ENVIRONMENT_PLUGIN_ID = "environment-plugin"

@Single
class EnvironmentSetupServiceImpl internal constructor(
    private val settingsStore: EnvironmentSettingsStore,
    private val commandExecutor: EnvironmentCommandExecutor
) : EnvironmentSetupService {
    override fun loadSettings(): EnvironmentSetupSettings {
        return normalizeSettings(settingsStore.load())
    }

    override fun saveSettings(settings: EnvironmentSetupSettings): EnvironmentSetupSettings {
        val normalized = normalizeSettings(settings)
        settingsStore.save(normalized)
        return normalized
    }

    override fun inspectEnvironment(settings: EnvironmentSetupSettings): EnvironmentHostInfo {
        return probeEnvironment(saveSettings(settings)).hostInfo
    }

    override fun previewInstall(settings: EnvironmentSetupSettings): EnvironmentExecutionResult {
        val normalized = saveSettings(settings)
        val probe = probeEnvironment(normalized)
        if (!probe.success) {
            return EnvironmentExecutionResult(
                success = false,
                installTarget = normalized.installTarget,
                message = probe.hostInfo.message,
                output = probe.rawOutput,
                hostInfo = probe.hostInfo
            )
        }

        val script = runCatching {
            buildInstallScript(probe.hostInfo, normalized)
        }.getOrElse { throwable ->
            return EnvironmentExecutionResult(
                success = false,
                installTarget = normalized.installTarget,
                message = throwable.message ?: "无法生成安装脚本",
                output = probe.rawOutput,
                hostInfo = probe.hostInfo
            )
        }

        return EnvironmentExecutionResult(
            success = true,
            installTarget = normalized.installTarget,
            message = "安装脚本已生成，可直接执行",
            script = script,
            output = probe.rawOutput,
            hints = buildHints(probe.hostInfo, normalized),
            hostInfo = probe.hostInfo
        )
    }

    override fun install(settings: EnvironmentSetupSettings): EnvironmentExecutionResult {
        val normalized = saveSettings(settings)
        val preview = previewInstall(normalized)
        if (!preview.success) {
            return preview
        }

        val execution = when (preview.installTarget) {
            EnvironmentInstallTarget.LOCAL -> commandExecutor.executeLocal(preview.script)
            EnvironmentInstallTarget.SSH -> commandExecutor.executeSsh(normalized.sshConfig, preview.script)
        }

        return EnvironmentExecutionResult(
            success = execution.exitCode == 0,
            installTarget = preview.installTarget,
            message = if (execution.exitCode == 0) {
                "安装脚本执行完成"
            } else {
                "安装脚本执行失败，退出码 ${execution.exitCode}"
            },
            script = preview.script,
            output = execution.output,
            hints = preview.hints,
            hostInfo = preview.hostInfo
        )
    }

    private fun normalizeSettings(settings: EnvironmentSetupSettings): EnvironmentSetupSettings {
        return settings.copy(
            selectedPackages = settings.selectedPackages.distinct().sortedBy { item -> item.ordinal },
            sshConfig = settings.sshConfig.copy(
                port = settings.sshConfig.port.takeIf { port -> port > 0 } ?: 22
            )
        )
    }

    private fun probeEnvironment(
        settings: EnvironmentSetupSettings
    ): EnvironmentProbe {
        return when (settings.installTarget) {
            EnvironmentInstallTarget.LOCAL -> probeLocalEnvironment(settings)
            EnvironmentInstallTarget.SSH -> probeRemoteEnvironment(settings)
        }
    }

    private fun probeLocalEnvironment(
        settings: EnvironmentSetupSettings
    ): EnvironmentProbe {
        if (!File("/bin/sh").exists()) {
            val hostInfo = EnvironmentHostInfo(
                installTarget = settings.installTarget,
                unix = false,
                osName = System.getProperty("os.name").orEmpty(),
                hostName = "localhost",
                currentUser = System.getProperty("user.name").orEmpty(),
                workingDirectory = System.getProperty("user.home").orEmpty(),
                packageManager = "unknown",
                message = "本机未检测到 /bin/sh，仅支持 Unix 环境"
            )
            return EnvironmentProbe(false, hostInfo, "")
        }

        val execution = commandExecutor.executeLocal(PROBE_SCRIPT)
        return toProbe(settings.installTarget, execution)
    }

    private fun probeRemoteEnvironment(
        settings: EnvironmentSetupSettings
    ): EnvironmentProbe {
        val sshConfig = settings.sshConfig
        if (sshConfig.host.isBlank()) {
            val hostInfo = EnvironmentHostInfo(
                installTarget = settings.installTarget,
                unix = false,
                osName = "unknown",
                hostName = "unknown",
                currentUser = sshConfig.username,
                workingDirectory = ".",
                packageManager = "unknown",
                message = "SSH Host 不能为空"
            )
            return EnvironmentProbe(false, hostInfo, "")
        }

        val execution = commandExecutor.executeSsh(sshConfig, PROBE_SCRIPT)
        return toProbe(settings.installTarget, execution)
    }

    private fun toProbe(
        installTarget: EnvironmentInstallTarget,
        execution: EnvironmentCommandResult
    ): EnvironmentProbe {
        if (execution.exitCode != 0) {
            val hostInfo = EnvironmentHostInfo(
                installTarget = installTarget,
                unix = false,
                osName = "unknown",
                hostName = "unknown",
                currentUser = "unknown",
                workingDirectory = ".",
                packageManager = "unknown",
                message = execution.output.ifBlank { "目标环境探测失败" }
            )
            return EnvironmentProbe(false, hostInfo, execution.output)
        }

        val values = execution.output.lineSequence()
            .mapNotNull { line ->
                val separatorIndex = line.indexOf('=')
                if (separatorIndex <= 0) {
                    null
                } else {
                    line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
                }
            }
            .toMap()

        val hostInfo = EnvironmentHostInfo(
            installTarget = installTarget,
            unix = values["unix"] == "true",
            osName = values["osName"].orEmpty(),
            hostName = values["hostName"].orEmpty(),
            currentUser = values["currentUser"].orEmpty(),
            workingDirectory = values["workingDirectory"].orEmpty(),
            packageManager = values["packageManager"].orEmpty(),
            message = when {
                values["unix"] != "true" -> "目标不是 Unix 环境"
                values["packageManager"].isNullOrBlank() || values["packageManager"] == "unknown" ->
                    "已连接到 Unix 环境，但未识别出支持的包管理器"
                else -> "已识别 ${values["osName"]} / ${values["packageManager"]} 环境"
            }
        )
        return EnvironmentProbe(
            success = hostInfo.unix && hostInfo.packageManager != "unknown",
            hostInfo = hostInfo,
            rawOutput = execution.output
        )
    }

    private fun buildInstallScript(
        hostInfo: EnvironmentHostInfo,
        settings: EnvironmentSetupSettings
    ): String {
        require(hostInfo.unix) { "仅支持 Unix 环境执行安装脚本" }
        require(settings.selectedPackages.isNotEmpty()) { "至少选择一个要安装的组件" }

        val packageManager = hostInfo.packageManager
        val unsupportedPackages = settings.selectedPackages
            .filter { environmentPackage ->
                packageNamesFor(environmentPackage, packageManager).isEmpty()
            }

        require(unsupportedPackages.isEmpty()) {
            "当前包管理器 $packageManager 暂不支持：${unsupportedPackages.joinToString { item -> item.displayName() }}"
        }

        val packageNames = settings.selectedPackages
            .flatMap { environmentPackage -> packageNamesFor(environmentPackage, packageManager) }
            .distinct()

        require(packageNames.isNotEmpty()) {
            "当前包管理器 $packageManager 暂未配置这些组件的安装包"
        }

        val installCommand = installCommandFor(packageManager, packageNames)
        val repositorySetupCommands = buildRepositorySetupCommands(packageManager, settings.selectedPackages)
        val followUpCommands = buildFollowUpCommands(packageManager, settings.selectedPackages)
        val sudoSetup = if (settings.useSudo) {
            """
            if [ "$(id -u)" -ne 0 ]; then
              if command -v sudo >/dev/null 2>&1; then
                SUDO="sudo"
              else
                echo "当前用户不是 root，且 sudo 不可用" >&2
                exit 1
              fi
            else
              SUDO=""
            fi
            """.trimIndent()
        } else {
            """SUDO="" """
        }

        return buildString {
            appendLine("set -eu")
            appendLine("export DEBIAN_FRONTEND=noninteractive")
            appendLine("run_root() {")
            appendLine("  if [ -n \"\$SUDO\" ]; then")
            appendLine("    \"\$SUDO\" \"\$@\"")
            appendLine("  else")
            appendLine("    \"\$@\"")
            appendLine("  fi")
            appendLine("}")
            appendLine()
            appendLine(sudoSetup)
            appendLine()
            appendLine("echo \"==> Target: ${hostInfo.hostName.ifBlank { "localhost" }} (${hostInfo.osName})\"")
            appendLine("echo \"==> Package manager: $packageManager\"")
            appendLine("echo \"==> Components: ${settings.selectedPackages.joinToString { item -> item.displayName() }}\"")
            appendLine()
            if (repositorySetupCommands.isNotBlank()) {
                appendLine(repositorySetupCommands)
                appendLine()
            }
            appendLine(installCommand)
            if (followUpCommands.isNotBlank()) {
                appendLine()
                appendLine(followUpCommands)
            }
            if (packageManager == "brew" && settings.selectedPackages.any { item -> item == EnvironmentPackage.JDK17 || item == EnvironmentPackage.JDK21 }) {
                appendLine()
                appendLine("echo \"Homebrew JDK 安装完成后，可能还需要把对应 openjdk 的 bin 路径加入 PATH。\"")
            }
        }.trim()
    }

    private fun buildHints(
        hostInfo: EnvironmentHostInfo,
        settings: EnvironmentSetupSettings
    ): List<String> {
        val selectedPackages = settings.selectedPackages
        val hints = mutableListOf<String>()

        if (settings.installTarget == EnvironmentInstallTarget.SSH) {
            hints += "远程模式通过 SSH 会话直接执行脚本；若普通用户权限不足，请开启 sudo 或改用具备 sudo 的账户。"
        }

        if (hostInfo.packageManager == "brew" && selectedPackages.any { item ->
                item == EnvironmentPackage.JDK17 || item == EnvironmentPackage.JDK21
            }
        ) {
            hints += "Homebrew 的 OpenJDK 通常不会自动加入当前 shell 的 PATH，可按 brew info openjdk 的提示补环境变量。"
        }

        if (EnvironmentPackage.GIT in selectedPackages) {
            hints += "Git 装完后建议先配置 git config --global user.name 和 git config --global user.email。"
        }

        if (EnvironmentPackage.MYSQL in selectedPackages) {
            hints += "MySQL 装完后建议尽快执行 mysql_secure_installation，并确认 root 登录方式是否符合预期。"
        }

        if (EnvironmentPackage.POSTGRESQL in selectedPackages) {
            hints += "PostgreSQL 常用检查命令是 psql --version；Linux 上首次进入数据库常见做法是 sudo -u postgres psql。"
            if (hostInfo.packageManager == "dnf" || hostInfo.packageManager == "yum") {
                hints += "部分 RPM 系发行版首次启用 PostgreSQL 前，可能还要先初始化数据目录。"
            }
        }

        if (EnvironmentPackage.REDIS in selectedPackages) {
            hints += "Redis 装完后可用 redis-cli ping 快速确认服务是否可用。"
        }

        if (EnvironmentPackage.NGINX in selectedPackages) {
            hints += "Nginx 改完配置后可先跑 nginx -t，再决定是否 reload。"
        }

        if (EnvironmentPackage.DOCKER in selectedPackages) {
            if (hostInfo.packageManager == "apt-get" || hostInfo.packageManager == "dnf" || hostInfo.packageManager == "yum") {
                hints += "Docker 在当前平台会走 Docker 官方仓库安装；如果机器上已装过 docker.io、podman-docker、docker-engine 等发行版包，建议先卸载再执行。"
            }
            if (hostInfo.packageManager == "brew") {
                hints += "macOS / Homebrew 模式通常需要先执行 colima start，之后再使用 docker 命令。"
            } else {
                hints += "Docker 装完后可用 docker --version 验证；非 root 用户常常还需要加入 docker 用户组后重新登录。"
            }
        }

        if (EnvironmentPackage.NODEJS in selectedPackages) {
            if (hostInfo.packageManager == "apt-get" || hostInfo.packageManager == "dnf" || hostInfo.packageManager == "yum") {
                hints += "Node.js 在当前平台会走 NodeSource 官方 LTS 源安装，默认更偏向稳定版本线。"
            }
            hints += "Node.js 装完后可用 node -v 和 npm -v 快速确认版本。"
        }

        if (EnvironmentPackage.PNPM in selectedPackages) {
            hints += "pnpm 装完后建议先执行 pnpm -v；如配合 monorepo，可继续设置 pnpm config。"
        }

        return hints.distinct()
    }

    private fun buildRepositorySetupCommands(
        packageManager: String,
        selectedPackages: List<EnvironmentPackage>
    ): String {
        val needsDockerRepo = EnvironmentPackage.DOCKER in selectedPackages
        val needsNodeSourceRepo = selectedPackages.any { item ->
            item == EnvironmentPackage.NODEJS || item == EnvironmentPackage.PNPM
        }

        if (!needsDockerRepo && !needsNodeSourceRepo) {
            return ""
        }

        val commands = mutableListOf<String>()
        if (needsDockerRepo) {
            when (packageManager) {
                "apt-get" -> commands += dockerAptRepositorySetupCommand()
                "dnf" -> commands += dockerDnfRepositorySetupCommand()
                "yum" -> commands += dockerYumRepositorySetupCommand()
            }
        }
        if (needsNodeSourceRepo) {
            when (packageManager) {
                "apt-get" -> commands += nodeSourceAptRepositorySetupCommand()
                "dnf", "yum" -> commands += nodeSourceRpmRepositorySetupCommand()
            }
        }
        return commands.filter { item -> item.isNotBlank() }.joinToString("\n\n")
    }

    private fun dockerAptRepositorySetupCommand(): String {
        return """
            docker_repo_os=""
            docker_suite=""
            if [ -r /etc/os-release ]; then
              . /etc/os-release
              case "${'$'}{ID:-}" in
                ubuntu)
                  docker_repo_os="ubuntu"
                  docker_suite="${'$'}{UBUNTU_CODENAME:-${'$'}VERSION_CODENAME}"
                  ;;
                debian)
                  docker_repo_os="debian"
                  docker_suite="${'$'}{VERSION_CODENAME:-}"
                  ;;
                *)
                  echo "Docker 官方仓库安装仅支持 Ubuntu / Debian，当前系统 ID=${'$'}{ID:-unknown}" >&2
                  exit 1
                  ;;
              esac
            else
              echo "/etc/os-release 不存在，无法识别 Docker 官方仓库目标系统" >&2
              exit 1
            fi
            if [ -z "${'$'}docker_suite" ]; then
              echo "无法识别 Docker 官方仓库的发行版代号" >&2
              exit 1
            fi
            run_root apt-get update
            run_root apt-get install -y ca-certificates curl
            run_root install -m 0755 -d /etc/apt/keyrings
            run_root curl -fsSL "https://download.docker.com/linux/${'$'}docker_repo_os/gpg" -o /etc/apt/keyrings/docker.asc
            run_root chmod a+r /etc/apt/keyrings/docker.asc
            run_root tee /etc/apt/sources.list.d/docker.sources >/dev/null <<EOF
            Types: deb
            URIs: https://download.docker.com/linux/${'$'}docker_repo_os
            Suites: ${'$'}docker_suite
            Components: stable
            Signed-By: /etc/apt/keyrings/docker.asc
            EOF
            run_root apt-get update
        """.trimIndent()
    }

    private fun dockerDnfRepositorySetupCommand(): String {
        return """
            docker_repo_os=""
            if [ -r /etc/os-release ]; then
              . /etc/os-release
              case "${'$'}{ID:-}" in
                fedora)
                  docker_repo_os="fedora"
                  ;;
                rhel)
                  docker_repo_os="rhel"
                  ;;
                centos|centos-stream)
                  docker_repo_os="centos"
                  ;;
                *)
                  echo "Docker 官方 RPM 仓库仅支持 Fedora / RHEL / CentOS，当前系统 ID=${'$'}{ID:-unknown}" >&2
                  exit 1
                  ;;
              esac
            else
              echo "/etc/os-release 不存在，无法识别 Docker 官方仓库目标系统" >&2
              exit 1
            fi
            run_root dnf -y install dnf-plugins-core
            if [ "${'$'}docker_repo_os" = "fedora" ]; then
              run_root dnf config-manager addrepo --from-repofile "https://download.docker.com/linux/${'$'}docker_repo_os/docker-ce.repo"
            else
              run_root dnf config-manager --add-repo "https://download.docker.com/linux/${'$'}docker_repo_os/docker-ce.repo"
            fi
        """.trimIndent()
    }

    private fun dockerYumRepositorySetupCommand(): String {
        return """
            docker_repo_os=""
            if [ -r /etc/os-release ]; then
              . /etc/os-release
              case "${'$'}{ID:-}" in
                rhel)
                  docker_repo_os="rhel"
                  ;;
                centos|centos-stream)
                  docker_repo_os="centos"
                  ;;
                *)
                  echo "Docker 官方 YUM 仓库仅支持 RHEL / CentOS，当前系统 ID=${'$'}{ID:-unknown}" >&2
                  exit 1
                  ;;
              esac
            else
              echo "/etc/os-release 不存在，无法识别 Docker 官方仓库目标系统" >&2
              exit 1
            fi
            run_root yum install -y yum-utils
            run_root yum-config-manager --add-repo "https://download.docker.com/linux/${'$'}docker_repo_os/docker-ce.repo"
        """.trimIndent()
    }

    private fun nodeSourceAptRepositorySetupCommand(): String {
        return """
            run_root apt-get update
            run_root apt-get install -y curl
            tmp_script="/tmp/nodesource_setup.sh"
            curl -fsSL https://deb.nodesource.com/setup_lts.x -o "${'$'}tmp_script"
            run_root bash "${'$'}tmp_script"
            rm -f "${'$'}tmp_script"
        """.trimIndent()
    }

    private fun nodeSourceRpmRepositorySetupCommand(): String {
        return """
            if command -v dnf >/dev/null 2>&1; then
              run_root dnf install -y curl
            else
              run_root yum install -y curl
            fi
            tmp_script="/tmp/nodesource_setup.sh"
            curl -fsSL https://rpm.nodesource.com/setup_lts.x -o "${'$'}tmp_script"
            run_root bash "${'$'}tmp_script"
            rm -f "${'$'}tmp_script"
        """.trimIndent()
    }

    private fun installCommandFor(
        packageManager: String,
        packageNames: List<String>
    ): String {
        val packages = packageNames.joinToString(" ")
        return when (packageManager) {
            "apt-get" -> """
                run_root apt-get update
                run_root apt-get install -y $packages
            """.trimIndent()
            "dnf" -> """run_root dnf install -y $packages"""
            "yum" -> """run_root yum install -y $packages"""
            "zypper" -> """run_root zypper --non-interactive install $packages"""
            "pacman" -> """
                run_root pacman -Sy --noconfirm $packages
            """.trimIndent()
            "apk" -> """run_root apk add --no-cache $packages"""
            "brew" -> """brew install $packages"""
            else -> error("暂不支持包管理器 $packageManager")
        }
    }

    private fun buildFollowUpCommands(
        packageManager: String,
        selectedPackages: List<EnvironmentPackage>
    ): String {
        val commands = mutableListOf<String>()
        if (packageManager == "brew") {
            if (EnvironmentPackage.PNPM in selectedPackages) {
                commands += pnpmBootstrapCommandFor(packageManager)
            }
            if (EnvironmentPackage.MYSQL in selectedPackages) {
                commands += "brew services start mysql || true"
            }
            if (EnvironmentPackage.POSTGRESQL in selectedPackages) {
                commands += "brew services start postgresql || true"
            }
            if (EnvironmentPackage.REDIS in selectedPackages) {
                commands += "brew services start redis || true"
            }
            if (EnvironmentPackage.NGINX in selectedPackages) {
                commands += "brew services start nginx || true"
            }
            if (EnvironmentPackage.DOCKER in selectedPackages) {
                commands += "colima start || true"
            }
            return commands.joinToString("\n")
        }

        if (EnvironmentPackage.PNPM in selectedPackages) {
            commands += pnpmBootstrapCommandFor(packageManager)
        }

        if (packageManager == "apk") {
            if (EnvironmentPackage.NGINX in selectedPackages) {
                commands += "run_root rc-service nginx start || run_root service nginx start || true"
            }
            if (EnvironmentPackage.DOCKER in selectedPackages) {
                commands += "run_root rc-update add docker boot || true"
                commands += "run_root service docker start || true"
            }
        }

        if (selectedPackages.any { item ->
                item == EnvironmentPackage.MYSQL ||
                    item == EnvironmentPackage.POSTGRESQL ||
                    item == EnvironmentPackage.REDIS ||
                    item == EnvironmentPackage.NGINX ||
                    item == EnvironmentPackage.DOCKER
            }
        ) {
            commands += "if command -v systemctl >/dev/null 2>&1; then"
            if (EnvironmentPackage.MYSQL in selectedPackages) {
                commands += "  run_root systemctl enable --now mysql || run_root systemctl enable --now mysqld || true"
            }
            if (EnvironmentPackage.POSTGRESQL in selectedPackages) {
                commands += "  run_root systemctl enable --now postgresql || true"
            }
            if (EnvironmentPackage.REDIS in selectedPackages) {
                commands += "  run_root systemctl enable --now redis || run_root systemctl enable --now redis-server || true"
            }
            if (EnvironmentPackage.NGINX in selectedPackages) {
                commands += "  run_root systemctl enable --now nginx || true"
            }
            if (EnvironmentPackage.DOCKER in selectedPackages) {
                commands += "  run_root systemctl enable --now docker || true"
            }
            commands += "fi"
        }
        return commands.joinToString("\n")
    }

    private fun pnpmBootstrapCommandFor(
        packageManager: String
    ): String {
        val installCommand = if (packageManager == "brew") {
            "npm install -g pnpm"
        } else {
            "run_root npm install -g pnpm"
        }

        return """
            if command -v corepack >/dev/null 2>&1; then
              ${if (packageManager == "brew") "corepack enable || true\n  corepack prepare pnpm@latest --activate || true" else "run_root corepack enable || true\n  run_root corepack prepare pnpm@latest --activate || true"}
            elif command -v npm >/dev/null 2>&1; then
              $installCommand
            else
              echo "npm 未安装，无法继续安装 pnpm" >&2
              exit 1
            fi
        """.trimIndent()
    }

    private fun packageNamesFor(
        environmentPackage: EnvironmentPackage,
        packageManager: String
    ): List<String> {
        return when (environmentPackage) {
            EnvironmentPackage.JDK17 -> when (packageManager) {
                "apt-get" -> listOf("openjdk-17-jdk")
                "dnf", "yum", "zypper" -> listOf("java-17-openjdk-devel")
                "pacman" -> listOf("jdk17-openjdk")
                "apk" -> listOf("openjdk17-jdk")
                "brew" -> listOf("openjdk@17")
                else -> emptyList()
            }
            EnvironmentPackage.JDK21 -> when (packageManager) {
                "apt-get" -> listOf("openjdk-21-jdk")
                "dnf", "yum", "zypper" -> listOf("java-21-openjdk-devel")
                "pacman" -> listOf("jdk21-openjdk")
                "apk" -> listOf("openjdk21-jdk")
                "brew" -> listOf("openjdk@21")
                else -> emptyList()
            }
            EnvironmentPackage.GIT -> when (packageManager) {
                "apt-get", "dnf", "yum", "zypper", "pacman", "apk", "brew" -> listOf("git")
                else -> emptyList()
            }
            EnvironmentPackage.MYSQL -> when (packageManager) {
                "apt-get", "dnf", "yum", "zypper" -> listOf("mysql-server")
                "brew", "pacman" -> listOf("mysql")
                "apk" -> listOf("mysql", "mysql-client")
                else -> emptyList()
            }
            EnvironmentPackage.POSTGRESQL -> when (packageManager) {
                "apt-get" -> listOf("postgresql")
                "dnf", "yum", "zypper" -> listOf("postgresql-server")
                "pacman", "brew" -> listOf("postgresql")
                "apk" -> listOf("postgresql", "postgresql-client")
                else -> emptyList()
            }
            EnvironmentPackage.REDIS -> when (packageManager) {
                "apt-get", "dnf", "yum", "zypper", "pacman", "apk", "brew" -> listOf("redis")
                else -> emptyList()
            }
            EnvironmentPackage.NGINX -> when (packageManager) {
                "apt-get", "dnf", "yum", "zypper", "pacman", "apk", "brew" -> listOf("nginx")
                else -> emptyList()
            }
            EnvironmentPackage.DOCKER -> when (packageManager) {
                "apt-get", "dnf", "yum" -> listOf(
                    "docker-ce",
                    "docker-ce-cli",
                    "containerd.io",
                    "docker-buildx-plugin",
                    "docker-compose-plugin"
                )
                "zypper" -> listOf("docker")
                "pacman" -> listOf("docker", "docker-compose")
                "apk" -> listOf("docker", "docker-cli-compose")
                "brew" -> listOf("colima", "docker", "docker-compose")
                else -> emptyList()
            }
            EnvironmentPackage.NODEJS -> when (packageManager) {
                "apt-get", "dnf", "yum" -> listOf("nodejs")
                "zypper", "pacman", "apk" -> listOf("nodejs", "npm")
                "brew" -> listOf("node")
                else -> emptyList()
            }
            EnvironmentPackage.PNPM -> when (packageManager) {
                "apt-get", "dnf", "yum" -> listOf("nodejs")
                "zypper", "pacman", "apk" -> listOf("nodejs", "npm")
                "brew" -> listOf("node")
                else -> emptyList()
            }
        }
    }
}

internal interface EnvironmentSettingsStore {
    fun load(): EnvironmentSetupSettings
    fun save(settings: EnvironmentSetupSettings)
}

@Single
internal class FileEnvironmentSettingsStore : EnvironmentSettingsStore {
    private val settingsFile = File(KCloudLocalPaths.pluginDir(ENVIRONMENT_PLUGIN_ID), "settings.json")

    override fun load(): EnvironmentSetupSettings {
        return readKCloudJson(settingsFile) {
            EnvironmentSetupSettings(
                selectedPackages = listOf(
                    EnvironmentPackage.JDK21,
                    EnvironmentPackage.REDIS
                ),
                sshConfig = SshConnectionConfig(remoteRootPath = "/")
            )
        }
    }

    override fun save(settings: EnvironmentSetupSettings) {
        writeKCloudJson(settingsFile, settings)
    }
}

internal interface EnvironmentCommandExecutor {
    fun executeLocal(script: String): EnvironmentCommandResult
    fun executeSsh(sshConfig: SshConnectionConfig, script: String): EnvironmentCommandResult
}

@Single
internal class DefaultEnvironmentCommandExecutor : EnvironmentCommandExecutor {
    override fun executeLocal(script: String): EnvironmentCommandResult {
        return runCatching {
            val process = ProcessBuilder("/bin/sh", "-lc", script)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            EnvironmentCommandResult(exitCode = exitCode, output = output.ifBlank { "(no output)" })
        }.getOrElse { throwable ->
            EnvironmentCommandResult(exitCode = -1, output = throwable.message ?: "本地命令执行失败")
        }
    }

    override fun executeSsh(
        sshConfig: SshConnectionConfig,
        script: String
    ): EnvironmentCommandResult {
        return runCatching {
            withSshCommand(sshConfig, script)
        }.getOrElse { throwable ->
            EnvironmentCommandResult(exitCode = -1, output = throwable.message ?: "SSH 命令执行失败")
        }
    }

    private fun withSshCommand(
        sshConfig: SshConnectionConfig,
        script: String
    ): EnvironmentCommandResult {
        require(sshConfig.host.isNotBlank()) { "SSH Host 不能为空" }
        require(sshConfig.username.isNotBlank()) { "SSH 用户名不能为空" }

        when (sshConfig.authMode) {
            SshAuthMode.PASSWORD -> require(sshConfig.password.isNotBlank()) { "SSH 密码不能为空" }
            SshAuthMode.PRIVATE_KEY -> require(sshConfig.privateKeyPath.isNotBlank()) { "SSH 私钥路径不能为空" }
        }

        val client = SSHClient().apply {
            addHostKeyVerifier(PromiscuousVerifier())
            connect(sshConfig.host, sshConfig.port)
            when (sshConfig.authMode) {
                SshAuthMode.PASSWORD -> authPassword(sshConfig.username, sshConfig.password)
                SshAuthMode.PRIVATE_KEY -> authPublickey(sshConfig.username, sshConfig.privateKeyPath)
            }
        }

        return client.use { ssh ->
            ssh.startSession().use { session ->
                executeSessionCommand(session, "sh -lc ${shellQuote(script)}")
            }
        }
    }

    private fun executeSessionCommand(
        session: Session,
        commandText: String
    ): EnvironmentCommandResult {
        val command = session.exec(commandText)
        command.join(30, TimeUnit.MINUTES)
        val stdout = command.inputStream.bufferedReader().readText()
        val stderr = command.errorStream.bufferedReader().readText()
        val output = listOf(stdout, stderr)
            .filter { item -> item.isNotBlank() }
            .joinToString(separator = "\n")
            .ifBlank { "(no output)" }
        return EnvironmentCommandResult(
            exitCode = command.exitStatus ?: -1,
            output = output
        )
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }
}

internal data class EnvironmentCommandResult(
    val exitCode: Int,
    val output: String
)

private data class EnvironmentProbe(
    val success: Boolean,
    val hostInfo: EnvironmentHostInfo,
    val rawOutput: String
)

private fun EnvironmentPackage.displayName(): String {
    return when (this) {
        EnvironmentPackage.JDK17 -> "JDK 17"
        EnvironmentPackage.JDK21 -> "JDK 21"
        EnvironmentPackage.GIT -> "Git"
        EnvironmentPackage.MYSQL -> "MySQL"
        EnvironmentPackage.POSTGRESQL -> "PostgreSQL"
        EnvironmentPackage.REDIS -> "Redis"
        EnvironmentPackage.NGINX -> "Nginx"
        EnvironmentPackage.DOCKER -> "Docker"
        EnvironmentPackage.NODEJS -> "Node.js"
        EnvironmentPackage.PNPM -> "pnpm"
    }
}

private val PROBE_SCRIPT = """
set -eu
os_name="${'$'}(uname -s 2>/dev/null || printf 'unknown')"
host_name="${'$'}(hostname 2>/dev/null || printf 'unknown')"
current_user="${'$'}(id -un 2>/dev/null || whoami 2>/dev/null || printf 'unknown')"
working_directory="${'$'}(pwd 2>/dev/null || printf '.')"
package_manager="unknown"
for candidate in apt-get dnf yum zypper pacman apk brew; do
  if command -v "${'$'}candidate" >/dev/null 2>&1; then
    package_manager="${'$'}candidate"
    break
  fi
done
printf 'unix=true\n'
printf 'osName=%s\n' "${'$'}os_name"
printf 'hostName=%s\n' "${'$'}host_name"
printf 'currentUser=%s\n' "${'$'}current_user"
printf 'workingDirectory=%s\n' "${'$'}working_directory"
printf 'packageManager=%s\n' "${'$'}package_manager"
""".trimIndent()
