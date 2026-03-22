package site.addzero.coding.playground.server.config

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

data class PlaygroundServerSettings(
    val dataDirectory: Path,
    val sqliteUrl: String,
    val serverHost: String,
    val serverPort: Int,
) {
    companion object {
        fun fromSystem(): PlaygroundServerSettings {
            val home = Paths.get(System.getProperty("user.home"))
            val dataDirectory = Paths.get(
                System.getProperty(
                    "coding.playground.data.dir",
                    home.resolve(".coding-playground").toString(),
                ),
            ).toAbsolutePath().normalize()
            dataDirectory.createDirectories()
            val sqliteUrl = System.getProperty(
                "coding.playground.db.url",
                "jdbc:sqlite:${dataDirectory.resolve("coding-playground.db")}",
            )
            val serverHost = System.getProperty("coding.playground.server.host", "127.0.0.1")
            val serverPort = System.getProperty("coding.playground.server.port")?.toIntOrNull() ?: 18181
            return PlaygroundServerSettings(
                dataDirectory = dataDirectory,
                sqliteUrl = sqliteUrl,
                serverHost = serverHost,
                serverPort = serverPort,
            )
        }
    }
}
