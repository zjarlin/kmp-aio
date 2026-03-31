package site.addzero.kcloud.plugins.mcuconsole.service

import org.koin.core.annotation.Single
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class McuFlashCommandResult(
    val exitCode: Int = 0,
    val stdout: String = "",
    val stderr: String = "",
)

interface McuFlashCommandRunner {
    fun run(
        commandLine: String,
        workingDirectory: File? = null,
    ): McuFlashCommandResult
}

@Single(
    binds = [
        McuFlashCommandRunner::class,
    ],
)
class JvmMcuFlashCommandRunner : McuFlashCommandRunner {
    override fun run(
        commandLine: String,
        workingDirectory: File?,
    ): McuFlashCommandResult {
        val process = ProcessBuilder(shellCommand(commandLine))
            .directory(workingDirectory)
            .start()

        var stdout = ""
        var stderr = ""
        val stdoutThread = thread(name = "mcu-flash-stdout", start = true) {
            stdout = process.inputStream.bufferedReader().use { it.readText() }.trim()
        }
        val stderrThread = thread(name = "mcu-flash-stderr", start = true) {
            stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
        }

        val completed = process.waitFor(10, TimeUnit.MINUTES)
        if (!completed) {
            process.destroyForcibly()
            throw IllegalStateException("烧录命令超时: $commandLine")
        }
        stdoutThread.join()
        stderrThread.join()

        return McuFlashCommandResult(
            exitCode = process.exitValue(),
            stdout = stdout,
            stderr = stderr,
        )
    }

    private fun shellCommand(
        commandLine: String,
    ): List<String> {
        return when {
            isWindows() -> listOf("cmd", "/c", commandLine)
            else -> listOf(
                System.getenv("SHELL")
                    ?.takeIf { it.isNotBlank() }
                    ?: "/bin/zsh",
                "-lc",
                commandLine,
            )
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name")
            ?.contains("Windows", ignoreCase = true) == true
    }
}
