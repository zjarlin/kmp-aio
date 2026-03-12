package com.kcloud.storage

import com.kcloud.plugin.KCloudLocalPaths
import java.io.File

fun getSettingsDirectory(): File {
    return KCloudLocalPaths.appSupportDir()
}
