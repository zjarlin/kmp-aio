package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class PluginBuildCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val command: String,
)

interface PluginBuildCommandRunner {
    fun run(
        commandLine: String,
        workingDirectory: File,
        environment: Map<String, String> = emptyMap(),
        javaHome: String? = null,
    ): PluginBuildCommandResult
}

@Single(
    binds = [
        PluginBuildCommandRunner::class,
    ],
)
class JvmPluginBuildCommandRunner : PluginBuildCommandRunner {
    override fun run(
        commandLine: String,
        workingDirectory: File,
        environment: Map<String, String>,
        javaHome: String?,
    ): PluginBuildCommandResult {
        val builder = ProcessBuilder(shellCommand(commandLine))
            .directory(workingDirectory)
        builder.environment().putAll(environment)
        if (!javaHome.isNullOrBlank()) {
            builder.environment()["JAVA_HOME"] = javaHome
            val currentPath = builder.environment()["PATH"].orEmpty()
            builder.environment()["PATH"] = "${javaHome.trimEnd('/')}/bin${File.pathSeparator}$currentPath"
        }
        val process = builder.start()

        var stdout = ""
        var stderr = ""
        val stdoutThread = thread(name = "plugin-market-build-stdout", start = true) {
            stdout = process.inputStream.bufferedReader().use { it.readText() }.trim()
        }
        val stderrThread = thread(name = "plugin-market-build-stderr", start = true) {
            stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
        }

        val completed = process.waitFor(20, TimeUnit.MINUTES)
        if (!completed) {
            process.destroyForcibly()
            throw IllegalStateException("Gradle 构建超时: $commandLine")
        }
        stdoutThread.join()
        stderrThread.join()

        return PluginBuildCommandResult(
            exitCode = process.exitValue(),
            stdout = stdout,
            stderr = stderr,
            command = commandLine,
        )
    }

    private fun shellCommand(commandLine: String): List<String> {
        return if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            listOf("cmd", "/c", commandLine)
        } else {
            listOf(
                System.getenv("SHELL")
                    ?.takeIf { it.isNotBlank() }
                    ?: "/bin/zsh",
                "-lc",
                commandLine,
            )
        }
    }
}
