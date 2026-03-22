package site.addzero.coding.playground.server.service

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.service.PathVariableResolver
import java.nio.file.Path
import java.nio.file.Paths

@Single
class PathVariableResolverImpl : PathVariableResolver {
    override fun resolve(rawPath: String, variables: Map<String, String>): String {
        val builtins = mapOf(
            "HOME" to System.getProperty("user.home"),
            "USER_HOME" to System.getProperty("user.home"),
            "PWD" to System.getProperty("user.dir"),
            "TMP" to System.getProperty("java.io.tmpdir"),
        )
        var resolved = rawPath
        val merged = linkedMapOf<String, String>()
        merged.putAll(builtins)
        System.getProperties().forEach { key, value ->
            merged[key.toString()] = value.toString()
        }
        System.getenv().forEach { (key, value) ->
            merged[key] = value
        }
        merged.putAll(variables)

        merged.forEach { (key, value) ->
            resolved = resolved.replace("\${$key}", value).replace("$$key", value)
        }
        if (Regex("\\$\\{?[A-Za-z_][A-Za-z0-9_]*}?").containsMatchIn(resolved)) {
            throw IllegalArgumentException("Unresolved path variable in '$rawPath'")
        }
        val path = Paths.get(resolved)
        return if (path.isAbsolute) {
            path.normalize().toString()
        } else {
            Paths.get(System.getProperty("user.dir")).resolve(path).normalize().toString()
        }
    }
}
