package site.addzero.coding.playground

/**
 * 托管源码文件的最小平台能力。
 * 只保留工作台自动同步真正需要的能力，避免状态层直接依赖 JVM 文件 API。
 */
interface ManagedFileSupport {
    fun hashContent(content: String): String

    fun readFileHashOrNull(path: String): String?
}
