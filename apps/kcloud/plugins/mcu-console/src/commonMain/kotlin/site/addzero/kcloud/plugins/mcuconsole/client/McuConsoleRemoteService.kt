package site.addzero.kcloud.plugins.mcuconsole.client

import site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest
import site.addzero.kcloud.plugins.mcuconsole.api.external.McuConsoleApiClient

class McuConsoleRemoteService {
    suspend fun listPorts() = McuConsoleApiClient.api.listPorts().items

    suspend fun getSession(): McuSessionSnapshot = McuConsoleApiClient.api.getSession()

    suspend fun openSession(
        request: McuSessionOpenRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.api.openSession(request)
    }

    suspend fun closeSession(): McuSessionSnapshot {
        return McuConsoleApiClient.api.closeSession()
    }

    suspend fun resetSession(
        request: McuResetRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.api.resetSession(request)
    }

    suspend fun updateSignals(
        request: McuSignalRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.api.updateSignals(request)
    }

    suspend fun readRecentLines(
        request: McuSessionLinesRequest,
    ): McuEventBatchResponse {
        return McuConsoleApiClient.api.readRecentLines(request)
    }

    suspend fun readEvents(
        afterSeq: Long,
    ): McuEventBatchResponse {
        return McuConsoleApiClient.api.readEvents(afterSeq)
    }

    suspend fun executeScript(
        request: McuScriptExecuteRequest,
    ): McuScriptStatusResponse {
        return McuConsoleApiClient.api.executeScript(request)
    }

    suspend fun stopScript(
        request: McuScriptStopRequest = McuScriptStopRequest(),
    ): McuScriptStatusResponse {
        return McuConsoleApiClient.api.stopScript(request)
    }

    suspend fun getScriptStatus(): McuScriptStatusResponse {
        return McuConsoleApiClient.api.getScriptStatus()
    }

    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return McuConsoleApiClient.api.startFlash(request)
    }

    suspend fun getFlashStatus(): McuFlashStatusResponse {
        return McuConsoleApiClient.api.getFlashStatus()
    }
}
