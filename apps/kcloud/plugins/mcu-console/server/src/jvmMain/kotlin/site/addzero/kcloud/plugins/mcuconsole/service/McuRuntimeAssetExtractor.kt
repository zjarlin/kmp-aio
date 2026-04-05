package site.addzero.kcloud.plugins.mcuconsole.service

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.config.McuConsoleConfigKeys
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.kcloud.plugins.system.configcenter.spi.RUNTIME_CONFIG_CENTER_ACTIVE_KEY
import site.addzero.kcloud.plugins.system.configcenter.spi.requireRuntimeConfigCenterActive
import java.io.File

@Single
class McuRuntimeAssetExtractor(
    private val bundleCatalog: McuRuntimeBundleCatalog,
    private val applicationConfig: ApplicationConfig,
    private val configValueService: ConfigValueServiceSpi,
) {
    private val classLoader = javaClass.classLoader

    fun extractBundle(
        bundleId: String,
    ): ExtractedRuntimeBundle {
        val manifest = bundleCatalog.resolveManifest(bundleId)
        val outputDir = File(resolveRootDirectory(), manifest.bundleId)
        outputDir.mkdirs()

        manifest.resourceFiles
            .ifEmpty { listOf(manifest.artifactRelativePath) }
            .distinct()
            .forEach { relativePath ->
                val targetFile = File(outputDir, relativePath)
                if (shouldReuseExistingFile(targetFile)) {
                    return@forEach
                }
                extractResource(
                    bundleId = manifest.bundleId,
                    relativePath = relativePath,
                    outputDir = outputDir,
                )
            }

        val artifactFile = File(outputDir, manifest.artifactRelativePath)
        check(artifactFile.exists()) { "运行时固件不存在: ${artifactFile.absolutePath}" }
        require(!isPlaceholderArtifact(artifactFile)) {
            buildPlaceholderMessage(
                bundleId = manifest.bundleId,
                artifactFile = artifactFile,
            )
        }
        return ExtractedRuntimeBundle(
            manifest = manifest,
            outputDir = outputDir,
            artifactFile = artifactFile,
        )
    }

    private fun extractResource(
        bundleId: String,
        relativePath: String,
        outputDir: File,
    ) {
        val resourcePath = bundleCatalog.resolveResourcePath(bundleId, relativePath)
        val input = classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("缺少运行时资源文件: $resourcePath")
        val targetFile = File(outputDir, relativePath)
        targetFile.parentFile?.mkdirs()
        input.use { stream ->
            targetFile.outputStream().use { output ->
                stream.copyTo(output)
            }
        }
    }

    private fun shouldReuseExistingFile(
        targetFile: File,
    ): Boolean {
        if (!targetFile.isFile) {
            return false
        }
        return !isPlaceholderArtifact(targetFile)
    }

    private fun isPlaceholderArtifact(
        file: File,
    ): Boolean {
        if (!file.isFile) {
            return false
        }
        val prefixBytes = file.inputStream().use { input ->
            input.readNBytes(placeholderProbeBytes)
        }
        if (prefixBytes.isEmpty()) {
            return false
        }
        val prefixText = prefixBytes.decodeToString()
        return placeholderMarkers.any { marker -> prefixText.contains(marker) }
    }

    private fun buildPlaceholderMessage(
        bundleId: String,
        artifactFile: File,
    ): String {
        return "运行时包 $bundleId 仍是仓库占位固件，不能直接刷写。请先把真实板卡固件放到 ${artifactFile.absolutePath}，" +
            "或在配置中心 namespace=${McuConsoleConfigKeys.NAMESPACE} " +
            "key=${McuConsoleConfigKeys.RUNTIME_BUNDLE_ROOT_DIR} 配置真实运行时目录。"
    }

    private fun resolveRootDirectory(): File {
        val active = requireRuntimeConfigCenterActive(
            applicationConfig.propertyOrNull(RUNTIME_CONFIG_CENTER_ACTIVE_KEY)?.getString(),
        )
        val configured = configValueService.readValue(
            namespace = McuConsoleConfigKeys.NAMESPACE,
            key = McuConsoleConfigKeys.RUNTIME_BUNDLE_ROOT_DIR,
            active = active,
        ).value
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error(
                "配置中心缺少必填项 namespace=${McuConsoleConfigKeys.NAMESPACE} " +
                    "active=$active key=${McuConsoleConfigKeys.RUNTIME_BUNDLE_ROOT_DIR}",
            )
        return File(configured).absoluteFile
    }

    companion object {
        private const val placeholderProbeBytes = 4096
        private val placeholderMarkers = listOf(
            "REPLACE_WITH_REAL_BOARD_FIRMWARE_BEFORE_PRODUCTION",
            "MICROPYTHON_DEFAULT_GENERIC_RUNTIME_BUNDLE",
        )
    }
}

data class ExtractedRuntimeBundle(
    val manifest: McuRuntimeBundleManifest,
    val outputDir: File,
    val artifactFile: File,
)
