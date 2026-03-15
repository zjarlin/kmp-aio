package com.kcloud.plugins.environment.server

import com.kcloud.plugins.environment.EnvironmentInstallTarget
import com.kcloud.plugins.environment.EnvironmentPackage
import com.kcloud.plugins.environment.EnvironmentSetupSettings
import com.kcloud.plugins.ssh.SshConnectionConfig
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnvironmentSetupServiceImplTest {
    @Test
    fun `previewInstall builds apt script and followup hints`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "apt-get")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(
                    EnvironmentPackage.JDK21,
                    EnvironmentPackage.POSTGRESQL,
                    EnvironmentPackage.REDIS
                )
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "run_root apt-get update")
        assertContains(result.script, "openjdk-21-jdk")
        assertContains(result.script, "postgresql")
        assertContains(result.script, "redis")
        assertContains(result.script, "run_root systemctl enable --now postgresql || true")
        assertContains(result.script, "run_root systemctl enable --now redis || run_root systemctl enable --now redis-server || true")
        assertTrue(result.hints.any { hint -> hint.contains("sudo -u postgres psql") })
        assertTrue(result.hints.any { hint -> hint.contains("redis-cli ping") })
        assertEquals(1, commandExecutor.localScripts.size)
    }

    @Test
    fun `install executes remote script through ssh executor`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            sshProbeResult = probeResult(packageManager = "brew"),
            sshInstallResult = EnvironmentCommandResult(
                exitCode = 0,
                output = "brew install mysql\nsuccess"
            )
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.install(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.SSH,
                selectedPackages = listOf(EnvironmentPackage.MYSQL),
                sshConfig = SshConnectionConfig(
                    host = "example.com",
                    username = "root",
                    password = "secret"
                )
            )
        )

        assertTrue(result.success)
        assertEquals(2, commandExecutor.sshScripts.size)
        assertContains(commandExecutor.sshScripts[1].second, "brew install mysql")
        assertContains(commandExecutor.sshScripts[1].second, "brew services start mysql || true")
        assertTrue(result.hints.any { hint -> hint.contains("SSH 会话直接执行") })
        assertTrue(result.hints.any { hint -> hint.contains("mysql_secure_installation") })
    }

    @Test
    fun `previewInstall rejects blank ssh host before command execution`() {
        val commandExecutor = FakeEnvironmentCommandExecutor()
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.SSH,
                selectedPackages = listOf(EnvironmentPackage.REDIS),
                sshConfig = SshConnectionConfig(username = "root", password = "secret")
            )
        )

        assertFalse(result.success)
        assertEquals("SSH Host 不能为空", result.message)
        assertTrue(commandExecutor.sshScripts.isEmpty())
    }

    @Test
    fun `previewInstall prepares nodejs pnpm and nginx tooling`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "apt-get")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(
                    EnvironmentPackage.GIT,
                    EnvironmentPackage.NODEJS,
                    EnvironmentPackage.PNPM,
                    EnvironmentPackage.NGINX
                )
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "run_root apt-get install -y git nginx nodejs npm")
        assertContains(result.script, "corepack enable")
        assertContains(result.script, "corepack prepare pnpm@latest --activate")
        assertContains(result.script, "run_root systemctl enable --now nginx || true")
        assertTrue(result.hints.any { hint -> hint.contains("git config --global user.name") })
        assertTrue(result.hints.any { hint -> hint.contains("pnpm -v") })
        assertTrue(result.hints.any { hint -> hint.contains("nginx -t") })
    }

    @Test
    fun `previewInstall builds brew docker stack with colima`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "brew")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(EnvironmentPackage.DOCKER)
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "brew install colima docker docker-compose")
        assertContains(result.script, "colima start || true")
        assertTrue(result.hints.any { hint -> hint.contains("colima start") })
    }

    @Test
    fun `previewInstall configures official docker apt repository`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "apt-get")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(EnvironmentPackage.DOCKER)
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "https://download.docker.com/linux/\$docker_repo_os/gpg")
        assertContains(result.script, "/etc/apt/sources.list.d/docker.sources")
        assertContains(result.script, "run_root apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin")
        assertTrue(result.hints.any { hint -> hint.contains("Docker 官方仓库安装") })
    }

    @Test
    fun `previewInstall configures official docker dnf repository`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "dnf")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(EnvironmentPackage.DOCKER)
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "run_root dnf -y install dnf-plugins-core")
        assertContains(result.script, "config-manager")
        assertContains(result.script, "download.docker.com/linux/\$docker_repo_os/docker-ce.repo")
        assertContains(result.script, "run_root dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin")
    }

    @Test
    fun `previewInstall configures official docker yum repository`() {
        val commandExecutor = FakeEnvironmentCommandExecutor(
            localProbeResult = probeResult(packageManager = "yum")
        )
        val service = EnvironmentSetupServiceImpl(
            settingsStore = InMemoryEnvironmentSettingsStore(),
            commandExecutor = commandExecutor
        )

        val result = service.previewInstall(
            EnvironmentSetupSettings(
                installTarget = EnvironmentInstallTarget.LOCAL,
                selectedPackages = listOf(EnvironmentPackage.DOCKER)
            )
        )

        assertTrue(result.success)
        assertContains(result.script, "run_root yum install -y yum-utils")
        assertContains(result.script, "run_root yum-config-manager --add-repo")
        assertContains(result.script, "download.docker.com/linux/\$docker_repo_os/docker-ce.repo")
        assertContains(result.script, "run_root yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin")
    }
}

private class InMemoryEnvironmentSettingsStore(
    private var settings: EnvironmentSetupSettings = EnvironmentSetupSettings()
) : EnvironmentSettingsStore {
    override fun load(): EnvironmentSetupSettings {
        return settings
    }

    override fun save(settings: EnvironmentSetupSettings) {
        this.settings = settings
    }
}

private class FakeEnvironmentCommandExecutor(
    private val localProbeResult: EnvironmentCommandResult = probeResult(),
    private val localInstallResult: EnvironmentCommandResult = EnvironmentCommandResult(
        exitCode = 0,
        output = "local install ok"
    ),
    private val sshProbeResult: EnvironmentCommandResult = probeResult(),
    private val sshInstallResult: EnvironmentCommandResult = EnvironmentCommandResult(
        exitCode = 0,
        output = "ssh install ok"
    )
) : EnvironmentCommandExecutor {
    val localScripts = mutableListOf<String>()
    val sshScripts = mutableListOf<Pair<SshConnectionConfig, String>>()

    override fun executeLocal(script: String): EnvironmentCommandResult {
        localScripts += script
        return if (script.isProbeScript()) {
            localProbeResult
        } else {
            localInstallResult
        }
    }

    override fun executeSsh(
        sshConfig: SshConnectionConfig,
        script: String
    ): EnvironmentCommandResult {
        sshScripts += sshConfig to script
        return if (script.isProbeScript()) {
            sshProbeResult
        } else {
            sshInstallResult
        }
    }
}

private fun probeResult(
    packageManager: String = "apt-get"
): EnvironmentCommandResult {
    return EnvironmentCommandResult(
        exitCode = 0,
        output = """
        unix=true
        osName=Linux
        hostName=test-host
        currentUser=tester
        workingDirectory=/tmp
        packageManager=$packageManager
        """.trimIndent()
    )
}

private fun String.isProbeScript(): Boolean {
    return contains("package_manager=\"unknown\"") &&
        contains("printf 'unix=true\\n'")
}
