package com.kcloud.feature

import com.kcloud.paths.KCloudPaths
import java.io.File

object KCloudLocalPaths {
    fun appSupportDir(): File {
        return KCloudPaths.appSupportDir()
    }

    fun featureDir(featureId: String): File {
        return File(KCloudPaths.featuresDir(), featureId).ensureDirectory()
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
