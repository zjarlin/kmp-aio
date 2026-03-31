package site.addzero.kbox.core.support

import java.io.File
import java.io.InputStream
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest

internal fun walkRegularFiles(
    roots: List<File>,
    visit: (File) -> Unit,
) {
    val visitedRoots = linkedSetOf<String>()
    roots.asSequence()
        .filter { it.exists() }
        .forEach { root ->
            val canonicalRoot = root.canonicalFile
            if (!visitedRoots.add(canonicalRoot.absolutePath)) {
                return@forEach
            }
            Files.walkFileTree(
                canonicalRoot.toPath(),
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes,
                    ): FileVisitResult {
                        if (attrs.isRegularFile) {
                            visit(file.toFile())
                        }
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFileFailed(
                        file: Path,
                        exc: java.io.IOException,
                    ): FileVisitResult {
                        return FileVisitResult.CONTINUE
                    }
                },
            )
        }
}

internal fun moveFileReplacing(
    source: File,
    target: File,
) {
    target.parentFile?.mkdirs()
    runCatching {
        Files.move(
            source.toPath(),
            target.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING,
        )
    }.recoverCatching {
        Files.copy(
            source.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.delete(source.toPath())
    }.getOrThrow()
}

internal fun stableShortHash(
    value: String,
): String {
    return digestHex(
        algorithm = "SHA-256",
        bytes = value.toByteArray(),
    )
        .take(10)
}

internal fun fileMd5(
    file: File,
): String {
    file.inputStream().buffered().use { input ->
        return streamDigestHex(
            algorithm = "MD5",
            input = input,
        )
    }
}

internal fun streamMd5(
    input: InputStream,
): String {
    return streamDigestHex(
        algorithm = "MD5",
        input = input,
    )
}

internal fun readFilePreview(
    file: File,
    maxBytes: Int,
): ByteArray {
    file.inputStream().buffered().use { input ->
        return readStreamPreview(
            input = input,
            maxBytes = maxBytes,
        )
    }
}

internal fun readStreamPreview(
    input: InputStream,
    maxBytes: Int,
): ByteArray {
    val buffer = ByteArray(maxBytes.coerceAtLeast(0))
    val count = input.read(buffer, 0, buffer.size)
    if (count <= 0) {
        return ByteArray(0)
    }
    return buffer.copyOf(count)
}

internal fun looksLikeText(
    bytes: ByteArray,
): Boolean {
    if (bytes.isEmpty()) {
        return true
    }
    var suspicious = 0
    bytes.forEach { byte ->
        val value = byte.toInt() and 0xFF
        val printable = value == 0x09 || value == 0x0A || value == 0x0D || value in 0x20..0x7E
        if (!printable && value < 0x80) {
            suspicious += 1
        }
    }
    return suspicious * 10 <= bytes.size
}

internal fun normalizeRelativePath(
    path: String,
): String {
    return path.replace('\\', '/')
        .replace(Regex("/{2,}"), "/")
        .trim('/')
}

internal fun sanitizeFileName(
    name: String,
): String {
    return name.replace(Regex("[^A-Za-z0-9._-]+"), "_")
        .trim('_')
        .ifBlank { "file" }
}

internal fun normalizeSegments(
    path: String,
): List<String> {
    val normalized = path.replace('\\', '/')
    if (normalized.matches(Regex("^[A-Za-z]:/.*"))) {
        val drive = normalized.substring(0, 1).uppercase()
        return listOf("WindowsDrive-$drive") + normalized
            .substring(3)
            .split('/')
            .filter { it.isNotBlank() }
    }
    return normalized.split('/')
        .filter { it.isNotBlank() }
}

internal fun deleteEmptyParentDirectories(
    startDirectory: File?,
    stopDirectories: Set<String>,
) {
    var current = startDirectory?.absoluteFile
    while (current != null && current.exists()) {
        if (stopDirectories.contains(current.absolutePath)) {
            break
        }
        val children = current.listFiles().orEmpty()
        if (children.isNotEmpty()) {
            break
        }
        if (!current.delete()) {
            break
        }
        current = current.parentFile?.absoluteFile
    }
}

private fun digestHex(
    algorithm: String,
    bytes: ByteArray,
): String {
    return MessageDigest.getInstance(algorithm)
        .digest(bytes)
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
}

private fun streamDigestHex(
    algorithm: String,
    input: InputStream,
): String {
    val digest = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val read = input.read(buffer)
        if (read <= 0) {
            break
        }
        digest.update(buffer, 0, read)
    }
    return digest.digest()
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
