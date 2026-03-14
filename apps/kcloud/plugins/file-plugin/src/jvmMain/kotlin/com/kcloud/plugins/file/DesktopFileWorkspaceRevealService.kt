package com.kcloud.plugins.file

import com.kcloud.plugin.KCloudLocalPaths
import java.awt.Desktop
import java.io.File
import org.koin.core.annotation.Single

@Single
class DesktopFileWorkspaceRevealService : FileWorkspaceRevealService {
    override fun revealPath(path: String): FileWorkspaceActionResult {
        val localFile = File(KCloudLocalPaths.workspaceDir(), path)
        if (!localFile.exists()) {
            return FileWorkspaceActionResult(
                success = false,
                message = "本地文件不存在：${localFile.absolutePath}"
            )
        }

        return runCatching {
            Desktop.getDesktop().open(localFile.parentFile ?: localFile)
            FileWorkspaceActionResult(
                success = true,
                message = "已在系统文件管理器中打开：${localFile.parent}"
            )
        }.getOrElse { throwable ->
            FileWorkspaceActionResult(
                success = false,
                message = "打开目录失败：${throwable.message ?: localFile.absolutePath}"
            )
        }
    }
}
