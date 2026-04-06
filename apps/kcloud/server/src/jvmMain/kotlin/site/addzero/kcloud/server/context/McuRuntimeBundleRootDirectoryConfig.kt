package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.kcloud.plugins.mcuconsole.config.McuConsoleConfigKeys
import site.addzero.kcloud.plugins.mcuconsole.spi.McuRuntimeBundleRootDirectorySpi
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive

@Single
class McuRuntimeBundleRootDirectoryConfig(
    private val env: ConfigCenterEnv,
    private val runtimeConfigCenterActive: RuntimeConfigCenterActive,
) : McuRuntimeBundleRootDirectorySpi {
    override val rootDirectoryPath: String
        get() = env.string(McuConsoleConfigKeys.RUNTIME_BUNDLE_ROOT_DIR)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error(
                "配置中心缺少必填项 namespace=${McuConsoleConfigKeys.NAMESPACE} " +
                    "active=${runtimeConfigCenterActive.value} path=${McuConsoleConfigKeys.RUNTIME_BUNDLE_ROOT_DIR}",
            )
}
