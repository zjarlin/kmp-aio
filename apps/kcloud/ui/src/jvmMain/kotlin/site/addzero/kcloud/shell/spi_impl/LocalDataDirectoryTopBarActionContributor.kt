package site.addzero.kcloud.shell.spi_impl

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.menu.WorkbenchTopBarActionContributor
import site.addzero.kcloud.shell.overlay.GlobalUiNotification
import site.addzero.kcloud.shell.overlay.GlobalUiNotificationCenter

@Single
class LocalDataDirectoryTopBarActionContributor : WorkbenchTopBarActionContributor {
    override val order = 10

    @Composable
    override fun RowScope.Render() {
        WorkbenchIconButton(
            onClick = {
                runCatching {
                    openKCloudLocalDataDirectory()
                }.onSuccess {
                    GlobalUiNotificationCenter.show(
                        GlobalUiNotification(
                            title = "本地数据目录",
                            message = kcloudLocalDataDirectory().absolutePath,
                        ),
                    )
                }.onFailure { throwable ->
                    GlobalUiNotificationCenter.showError(
                        title = "打开本地数据目录失败",
                        message = throwable.message ?: "无法打开 SQLite 数据目录",
                    )
                }
            },
            tooltip = "打开本地数据目录",
            variant = WorkbenchButtonVariant.Outline,
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
            )
        }
    }
}
