package site.addzero.kcloud.plugins.mcuconsole.workbench

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.api.external.McuConsoleApiClient
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse

@Single
class McuConsoleRemoteService {
    suspend fun listPorts() = McuConsoleApiClient.sessionApi.listMcuPorts().items

    suspend fun getDeviceProfile(
        deviceKey: String?,
    ): McuDeviceProfileIso {
        return McuConsoleApiClient.settingsApi.getMcuDeviceProfile(deviceKey)
    }

    suspend fun saveDeviceProfile(
        request: McuDeviceProfileIso,
    ): McuDeviceProfileIso {
        return McuConsoleApiClient.settingsApi.saveMcuDeviceProfile(request)
    }

    suspend fun listTransportProfiles(): List<McuTransportProfileIso> {
        return McuConsoleApiClient.settingsApi.listMcuTransportProfiles().items
    }

    suspend fun saveTransportProfile(
        request: McuTransportProfileIso,
    ): McuTransportProfileIso {
        return McuConsoleApiClient.settingsApi.saveMcuTransportProfile(request)
    }

    suspend fun deleteTransportProfile(
        profileKey: String,
    ): List<McuTransportProfileIso> {
        return McuConsoleApiClient.settingsApi.deleteMcuTransportProfile(profileKey).items
    }

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

    suspend fun sendSerialText(
        request: McuSerialTextSendRequest,
    ): McuSerialTextSendResponse {
        return McuConsoleApiClient.sessionApi.sendMcuSerialText(request)
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

    suspend fun listFlashProbes(): List<McuFlashProbeSummary> {
        return McuConsoleApiClient.flashApi.listMcuFlashProbes().items
    }

    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return McuConsoleApiClient.flashApi.startMcuFlash(request)
    }

    suspend fun resetFlash(
        request: McuResetRequest,
        profileId: String?,
        probeSerialNumber: String?,
    ): McuFlashStatusResponse {
        return McuConsoleApiClient.flashApi.resetMcuFlashTarget(
            request = request,
            profileId = profileId,
            probeSerialNumber = probeSerialNumber,
        )
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

    suspend fun getDevicePowerLights(): McuModbusPowerLightsResponse {
        return McuConsoleApiClient.modbusDeviceApi.getMcuModbusPowerLights()
    }

    suspend fun getDeviceInfo(): McuModbusDeviceInfoResponse {
        return McuConsoleApiClient.modbusDeviceApi.getMcuModbusDeviceInfo()
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
