package com.kcloud.plugins.quicktransfer.server

import com.kcloud.plugins.quicktransfer.QuickTransferDropService
import com.kcloud.plugins.quicktransfer.QuickTransferService
import org.koin.core.annotation.Single

@Single
class QuickTransferServiceBridge(
    private val delegate: QuickTransferDropService,
) : QuickTransferService by delegate
