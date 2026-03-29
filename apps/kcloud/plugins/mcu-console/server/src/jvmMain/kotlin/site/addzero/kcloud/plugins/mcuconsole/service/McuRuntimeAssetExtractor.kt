package site.addzero.kcloud.plugins.mcuconsole.service

import java.io.File

class McuRuntimeAssetExtractor(
    private val bundleCatalog: McuRuntimeBundleCatalog,
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
                extractResource(
                    bundleId = manifest.bundleId,
                    relativePath = relativePath,
                    outputDir = outputDir,
                )
            }

        val artifactFile = File(outputDir, manifest.artifactRelativePath)
        check(artifactFile.exists()) { "运行时固件不存在: ${artifactFile.absolutePath}" }
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

    private fun resolveRootDirectory(): File {
        val configured = System.getProperty("kcloud.mcu.runtime.dir")
            ?.takeIf { it.isNotBlank() }
        return File(
            configured ?: "${System.getProperty("user.home")}/.kcloud/mcu-runtime-bundles",
        )
    }
}

data class ExtractedRuntimeBundle(
    val manifest: McuRuntimeBundleManifest,
    val outputDir: File,
    val artifactFile: File,
)
