package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Path

/**
 * 展开家目录缩写。
 */
internal fun String.expandHomeDirectoryTokens(): String {
    val homeDirectory = System.getProperty("user.home")?.trim()?.takeIf(String::isNotBlank) ?: return this
    return when {
        this == "~" -> homeDirectory
        this.startsWith("~/") || this.startsWith("~\\") -> homeDirectory + this.drop(1)
        this == "\$HOME" -> homeDirectory
        this.startsWith("\$HOME/") || this.startsWith("\$HOME\\") -> homeDirectory + this.removePrefix("\$HOME")
        this == "\${HOME}" -> homeDirectory
        this.startsWith("\${HOME}/") || this.startsWith("\${HOME}\\") -> homeDirectory + this.removePrefix("\${HOME}")
        else -> this
    }
}

/**
 * 解析并展开文件系统路径。
 */
internal fun String.toExpandedPath(): Path {
    return Path.of(expandHomeDirectoryTokens())
}

