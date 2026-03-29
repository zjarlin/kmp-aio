package site.addzero.coding.playground.server.service

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.util.packagePath
import site.addzero.coding.playground.shared.dto.GenerationTargetDto
import site.addzero.coding.playground.shared.dto.PathPreviewDto
import site.addzero.coding.playground.shared.dto.SourceFileMetaDto
import java.nio.file.Path
import java.nio.file.Paths

@Single
class CodegenPathResolver {
    fun preview(target: GenerationTargetDto): PathPreviewDto {
        val root = resolveRootDir(target.rootDir, target.variables)
        val sourceRoot = root.resolve("src").resolve(target.sourceSet).resolve("kotlin")
        return PathPreviewDto(
            targetId = target.id,
            resolvedRootDir = root.toString(),
            sourceRoot = sourceRoot.toString(),
        )
    }

    fun resolveFilePath(target: GenerationTargetDto, file: SourceFileMetaDto): Path {
        val root = resolveRootDir(target.rootDir, target.variables)
        return root.resolve("src")
            .resolve(target.sourceSet)
            .resolve("kotlin")
            .resolve(file.packageName.packagePath())
            .resolve(file.fileName)
            .toAbsolutePath()
            .normalize()
    }

    fun resolveRootDir(rootDir: String, variables: Map<String, String>): Path {
        val resolved = resolveVariables(rootDir, variables)
        return Paths.get(resolved).toAbsolutePath().normalize()
    }

    private fun resolveVariables(template: String, variables: Map<String, String>): String {
        val builtins = mapOf(
            "HOME" to System.getProperty("user.home").orEmpty(),
            "CWD" to System.getProperty("user.dir").orEmpty(),
            "TMP" to System.getProperty("java.io.tmpdir").orEmpty(),
        )
        val regex = Regex("""\$\{?([A-Za-z_][A-Za-z0-9_]*)}?""")
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            variables[key]
                ?: System.getenv(key)
                ?: System.getProperty(key)
                ?: builtins[key]
                ?: match.value
        }
    }
}
