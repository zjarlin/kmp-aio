package site.addzero.kcloud.plugins.mcuconsole.client

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.api.external.McuConsoleApiClient

@Single
class McuConsoleRemoteService {
    suspend fun listPorts() = McuConsoleApiClient.sessionApi.listMcuPorts().items

    suspend fun getSession(): McuSessionSnapshot = McuConsoleApiClient.sessionApi.getMcuSession()

    suspend fun openSession(
        request: McuSessionOpenRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.sessionApi.openMcuSession(request)
    }

    suspend fun closeSession(): McuSessionSnapshot {
        return McuConsoleApiClient.sessionApi.closeMcuSession()
    }

    suspend fun resetSession(
        request: McuResetRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.sessionApi.resetMcuSession(request)
    }

    suspend fun updateSignals(
        request: McuSignalRequest,
    ): McuSessionSnapshot {
        return McuConsoleApiClient.sessionApi.updateMcuSignals(request)
    }

    suspend fun readRecentLines(
        request: McuSessionLinesRequest,
    ): McuEventBatchResponse {
        return McuConsoleApiClient.sessionApi.readMcuRecentLines(request)
    }

    suspend fun readEvents(
        afterSeq: Long,
    ): McuEventBatchResponse {
        return McuConsoleApiClient.sessionApi.readMcuEvents(afterSeq)
    }

    suspend fun executeScript(
        request: McuScriptExecuteRequest,
    ): McuScriptStatusResponse {
        return McuConsoleApiClient.scriptApi.executeMcuScript(request)
    }

    suspend fun stopScript(
        request: McuScriptStopRequest = McuScriptStopRequest(),
    ): McuScriptStatusResponse {
        return McuConsoleApiClient.scriptApi.stopMcuScript(request)
    }

    suspend fun getScriptStatus(): McuScriptStatusResponse {
        return McuConsoleApiClient.scriptApi.getMcuScriptStatus()
    }

    suspend fun listFlashProfiles(): List<McuFlashProfileSummary> {
        return McuConsoleApiClient.flashApi.listMcuFlashProfiles().items
    }

    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return McuConsoleApiClient.flashApi.startMcuFlash(request)
    }

    suspend fun downloadFlashFirmware(
        request: McuFlashDownloadRequest,
    ): McuFlashDownloadResponse {
        return McuConsoleApiClient.flashApi.downloadMcuFlashFirmware(request)
    }

    suspend fun getFlashStatus(): McuFlashStatusResponse {
        return McuConsoleApiClient.flashApi.getMcuFlashStatus()
    }

    suspend fun listRuntimeBundles(): List<McuRuntimeBundleSummary> {
        return McuConsoleApiClient.runtimeApi.listMcuRuntimeBundles().items
    }

    suspend fun ensureRuntime(
        request: McuRuntimeEnsureRequest,
    ): McuRuntimeStatusResponse {
        return McuConsoleApiClient.runtimeApi.ensureMcuRuntime(request)
    }

    suspend fun getRuntimeStatus(): McuRuntimeStatusResponse {
        return McuConsoleApiClient.runtimeApi.getMcuRuntimeStatus()
    }

    suspend fun gpioWrite(
        request: McuModbusGpioWriteRequest,
    ): McuModbusCommandResponse {
        return McuConsoleApiClient.modbusApi.gpioWrite(request)
    }

    suspend fun gpioMode(
        request: McuModbusGpioModeRequest,
    ): McuModbusCommandResponse {
        return McuConsoleApiClient.modbusApi.gpioMode(request)
    }

    suspend fun pwmDuty(
        request: McuModbusPwmDutyRequest,
    ): McuModbusCommandResponse {
        return McuConsoleApiClient.modbusApi.pwmDuty(request)
    }

    suspend fun servoAngle(
        request: McuModbusServoAngleRequest,
    ): McuModbusCommandResponse {
        return McuConsoleApiClient.modbusApi.servoAngle(request)
    }
}
