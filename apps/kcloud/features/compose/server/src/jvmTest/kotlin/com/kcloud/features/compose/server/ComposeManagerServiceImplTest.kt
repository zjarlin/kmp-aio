package com.kcloud.features.compose.server

import com.kcloud.model.AuthType
import com.kcloud.model.ServerConfig
import com.kcloud.features.compose.ComposeManagerSettings
import com.kcloud.features.compose.ComposeStackDraft
import com.kcloud.features.compose.ComposeStackStatus
import com.kcloud.features.compose.ComposeTargetMode
import com.kcloud.features.servermanagement.ServerManagementMutationResult
import com.kcloud.features.servermanagement.ServerManagementService
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeManagerServiceImplTest {
    @Test
    fun `saveStack validates then writes compose file into local stack root`() {
        val rootDir = createTempDirectory(prefix = "compose-local-root").toFile()
        val settingsStore = InMemoryComposeSettingsStore(
            ComposeManagerSettings(localStacksPath = rootDir.absolutePath)
        )
        val commandRunner = FakeComposeCommandRunner(
            localResponder = { command ->
                when {
                    "config -q" in command -> ShellCommandResult(0, "ok")
                    else -> ShellCommandResult(0, "[]")
                }
            }
        )
        val service = ComposeManagerServiceImpl(
            settingsStore = settingsStore,
            serverManagementService = FakeServerManagementService(),
            commandRunner = commandRunner
        )

        val result = kotlinx.coroutines.runBlocking {
            service.saveStack(
                ComposeStackDraft(
                    name = "demo",
                    composeYaml = """
                        services:
                          web:
                            image: nginx:alpine
                    """.trimIndent()
                )
            )
        }

        assertTrue(result.success)
        val composeFile = File(rootDir, "demo/compose.yaml")
        assertTrue(composeFile.exists())
        assertContains(composeFile.readText(), "image: nginx:alpine")
        assertTrue(commandRunner.localCommands.any { command -> "config -q" in command })
    }

    @Test
    fun `listStacks maps docker ps output into running summary`() {
        val rootDir = createTempDirectory(prefix = "compose-stack-list").toFile()
        File(rootDir, "blog").mkdirs()
        File(rootDir, "blog/compose.yaml").writeText(
            """
                services:
                  web:
                    image: nginx:alpine
            """.trimIndent()
        )
        val settingsStore = InMemoryComposeSettingsStore(
            ComposeManagerSettings(localStacksPath = rootDir.absolutePath)
        )
        val commandRunner = FakeComposeCommandRunner(
            localResponder = { command ->
                when {
                    "ps -a --format json" in command -> ShellCommandResult(
                        0,
                        """
                            [
                              {
                                "Name": "blog-web-1",
                                "Service": "web",
                                "State": "running",
                                "Health": "",
                                "Publishers": [
                                  {
                                    "URL": "0.0.0.0",
                                    "PublishedPort": 8080,
                                    "TargetPort": 80,
                                    "Protocol": "tcp"
                                  }
                                ]
                              }
                            ]
                        """.trimIndent()
                    )

                    "version" in command -> ShellCommandResult(0, "Docker Compose version v2.27.0")
                    "config --services" in command -> ShellCommandResult(0, "web")
                    else -> ShellCommandResult(0, "(no output)")
                }
            }
        )
        val service = ComposeManagerServiceImpl(
            settingsStore = settingsStore,
            serverManagementService = FakeServerManagementService(),
            commandRunner = commandRunner
        )

        val stacks = kotlinx.coroutines.runBlocking { service.listStacks() }
        val draft = kotlinx.coroutines.runBlocking { service.readStack("blog") }
        val debugMessage = buildString {
            append("commands=")
            append(commandRunner.localCommands.joinToString(" | "))
            append(", stacks=")
            append(stacks)
            append(", draft=")
            append(draft)
        }

        assertEquals(1, stacks.size)
        assertEquals(ComposeStackStatus.RUNNING, stacks.first().status, debugMessage)
        assertEquals(1, stacks.first().runningCount)
        assertEquals(1, draft?.services?.size)
        assertEquals("web", draft?.services?.first()?.service)
        assertContains(draft?.services?.first()?.publishers?.first().orEmpty(), "8080->80/tcp")
    }

    @Test
    fun `inspectRuntime reports missing remote server selection`() {
        val service = ComposeManagerServiceImpl(
            settingsStore = InMemoryComposeSettingsStore(
                ComposeManagerSettings(
                    targetMode = ComposeTargetMode.SERVER,
                    selectedServerId = null
                )
            ),
            serverManagementService = FakeServerManagementService(),
            commandRunner = FakeComposeCommandRunner()
        )

        val result = kotlinx.coroutines.runBlocking { service.inspectRuntime() }

        assertTrue(!result.success)
        assertContains(result.message, "远程模式")
    }

    @Test
    fun `deleteStack removes local stack directory`() {
        val rootDir = createTempDirectory(prefix = "compose-delete-stack").toFile()
        val stackDir = File(rootDir, "demo").apply { mkdirs() }
        File(stackDir, "compose.yaml").writeText(
            """
                services:
                  web:
                    image: nginx:alpine
            """.trimIndent()
        )
        val settingsStore = InMemoryComposeSettingsStore(
            ComposeManagerSettings(localStacksPath = rootDir.absolutePath)
        )
        val commandRunner = FakeComposeCommandRunner(
            localResponder = { ShellCommandResult(0, "(no output)") }
        )
        val service = ComposeManagerServiceImpl(
            settingsStore = settingsStore,
            serverManagementService = FakeServerManagementService(),
            commandRunner = commandRunner
        )

        val result = kotlinx.coroutines.runBlocking { service.deleteStack("demo") }

        assertTrue(result.success)
        assertTrue(!stackDir.exists())
    }
}

private class InMemoryComposeSettingsStore(
    private var settings: ComposeManagerSettings
) : ComposeSettingsStore {
    override fun load(): ComposeManagerSettings = settings

    override fun save(settings: ComposeManagerSettings) {
        this.settings = settings
    }

    override fun defaultLocalStacksPath(): String {
        return settings.localStacksPath
    }
}

private class FakeComposeCommandRunner(
    private val localResponder: (String) -> ShellCommandResult = { ShellCommandResult(0, "(no output)") }
) : ComposeCommandRunner {
    val localCommands = mutableListOf<String>()
    val remoteCommands = mutableListOf<Pair<ServerConfig, String>>()

    override suspend fun executeLocal(command: String, timeoutSeconds: Long): ShellCommandResult {
        localCommands += command
        return localResponder(command)
    }

    override suspend fun executeRemote(
        server: ServerConfig,
        command: String,
        timeoutSeconds: Long
    ): ShellCommandResult {
        remoteCommands += server to command
        return ShellCommandResult(0, "(no output)")
    }
}

private class FakeServerManagementService : ServerManagementService {
    override fun listServers(): List<ServerConfig> = emptyList()

    override fun findServer(serverId: String): ServerConfig? = null

    override fun saveServer(server: ServerConfig): ServerManagementMutationResult {
        return ServerManagementMutationResult(success = false, message = "unused")
    }

    override fun deleteServer(serverId: String): ServerManagementMutationResult {
        return ServerManagementMutationResult(success = false, message = "unused")
    }
}
