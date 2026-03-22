package com.kcloud.features.quicktransfer

import java.io.File

interface QuickTransferDropService : QuickTransferService {
    suspend fun stageDroppedFiles(files: List<File>): QuickTransferActionResult
}
