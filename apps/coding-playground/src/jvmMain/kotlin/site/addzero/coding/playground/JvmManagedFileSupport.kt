package site.addzero.coding.playground

import org.koin.core.annotation.Single
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.path.readText

/**
 * JVM 宿主上的托管文件实现。
 */
@Single
class JvmManagedFileSupport : ManagedFileSupport {
    override fun hashContent(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    override fun readFileHashOrNull(path: String): String? {
        val file = Paths.get(path)
        if (!Files.exists(file)) {
            return null
        }
        return runCatching { hashContent(file.readText()) }.getOrNull()
    }
}
