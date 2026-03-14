package com.kcloud.plugin

import com.kcloud.paths.KCloudPaths
import java.io.File

object KCloudLocalPaths {
    fun appSupportDir(): File {
        return KCloudPaths.appSupportDir()
    }

    fun pluginDir(pluginId: String): File {
        return File(KCloudPaths.pluginsDir(), pluginId).ensureDirectory()
    }

    fun workspaceDir(): File {
        return KCloudPaths.workspaceDir()
    }

    private fun File.ensureDirectory(): File {
        if (!exists()) {
            mkdirs()
        }
        return this
    }
}
