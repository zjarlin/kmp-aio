package site.addzero.kcloud.plugins.mcuconsole.workbench

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.api.external.Apis
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusPowerLightsResponse

@Single
class McuConsoleRemoteService {
    private val modbusApi
        get() = Apis.mcuModbusAtomicRoutesApi

    suspend fun listPorts() = Apis.mcuSessionApi.listMcuPorts().items

    suspend fun getDeviceProfile(
        deviceKey: String?,
    ): McuDeviceProfileIso {
        return Apis.mcuSettingsApi.getMcuDeviceProfile(deviceKey)
    }

    suspend fun saveDeviceProfile(
        request: McuDeviceProfileIso,
    ): McuDeviceProfileIso {
        return Apis.mcuSettingsApi.saveMcuDeviceProfile(request)
    }

    suspend fun listTransportProfiles(): List<McuTransportProfileIso> {
        return Apis.mcuSettingsApi.listMcuTransportProfiles().items
    }

    suspend fun saveTransportProfile(
        request: McuTransportProfileIso,
    ): McuTransportProfileIso {
        return Apis.mcuSettingsApi.saveMcuTransportProfile(request)
    }

    suspend fun deleteTransportProfile(
        profileKey: String,
    ): List<McuTransportProfileIso> {
        return Apis.mcuSettingsApi.deleteMcuTransportProfile(profileKey).items
    }

    suspend fun getSession(): McuSessionSnapshot = Apis.mcuSessionApi.getMcuSession()

    suspend fun openSession(
        request: McuSessionOpenRequest,
    ): McuSessionSnapshot {
        return Apis.mcuSessionApi.openMcuSession(request)
    }

    suspend fun closeSession(): McuSessionSnapshot {
        return Apis.mcuSessionApi.closeMcuSession()
    }

    suspend fun resetSession(
        request: McuResetRequest,
    ): McuSessionSnapshot {
        return Apis.mcuSessionApi.resetMcuSession(request)
    }

    suspend fun sendSerialText(
        request: McuSerialTextSendRequest,
    ): McuSerialTextSendResponse {
        return Apis.mcuSessionApi.sendMcuSerialText(request)
    }

    suspend fun updateSignals(
        request: McuSignalRequest,
    ): McuSessionSnapshot {
        return Apis.mcuSessionApi.updateMcuSignals(request)
    }

    suspend fun readRecentLines(
        request: McuSessionLinesRequest,
    ): McuEventBatchResponse {
        return Apis.mcuSessionApi.readMcuRecentLines(request)
    }

    suspend fun readEvents(
        afterSeq: Long,
    ): McuEventBatchResponse {
        return Apis.mcuSessionApi.readMcuEvents(afterSeq)
    }

    suspend fun executeScript(
        request: McuScriptExecuteRequest,
    ): McuScriptStatusResponse {
        return Apis.mcuScriptApi.executeMcuScript(request)
    }

    suspend fun stopScript(
        request: McuScriptStopRequest = McuScriptStopRequest(),
    ): McuScriptStatusResponse {
        return Apis.mcuScriptApi.stopMcuScript(request)
    }

    suspend fun getScriptStatus(): McuScriptStatusResponse {
        return Apis.mcuScriptApi.getMcuScriptStatus()
    }

    suspend fun listFlashProfiles(): List<McuFlashProfileSummary> {
        return Apis.mcuFlashApi.listMcuFlashProfiles().items
    }

    suspend fun listFlashProbes(): List<McuFlashProbeSummary> {
        return Apis.mcuFlashApi.listMcuFlashProbes().items
    }

    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return Apis.mcuFlashApi.startMcuFlash(request)
    }

    suspend fun resetFlash(
        request: McuResetRequest,
        profileId: String?,
        probeSerialNumber: String?,
    ): McuFlashStatusResponse {
        return Apis.mcuFlashApi.resetMcuFlashTarget(
            request = request,
            profileId = profileId,
            probeSerialNumber = probeSerialNumber,
        )
    }

    suspend fun getFlashStatus(): McuFlashStatusResponse {
        return Apis.mcuFlashApi.getMcuFlashStatus()
    }

    suspend fun listRuntimeBundles(): List<McuRuntimeBundleSummary> {
        return Apis.mcuRuntimeApi.listMcuRuntimeBundles().items
    }

    suspend fun ensureRuntime(
        request: McuRuntimeEnsureRequest,
    ): McuRuntimeStatusResponse {
        return Apis.mcuRuntimeApi.ensureMcuRuntime(request)
    }

    suspend fun getRuntimeStatus(): McuRuntimeStatusResponse {
        return Apis.mcuRuntimeApi.getMcuRuntimeStatus()
    }

    suspend fun getDevicePowerLights(): McuModbusPowerLightsResponse {
        return Apis.mcuModbusDeviceRoutesApi.getMcuModbusPowerLights()
    }

    suspend fun getDeviceInfo(): McuModbusDeviceInfoResponse {
        return Apis.mcuModbusDeviceRoutesApi.getMcuModbusDeviceInfo()
    }

    suspend fun gpioWrite(
        request: McuModbusGpioWriteRequest,
    ): McuModbusCommandResponse {
        return modbusApi.writeMcuModbusGpio(request)
    }

    suspend fun gpioMode(
        request: McuModbusGpioModeRequest,
    ): McuModbusCommandResponse {
        return modbusApi.writeMcuModbusGpioMode(request)
    }

    suspend fun pwmDuty(
        request: McuModbusPwmDutyRequest,
    ): McuModbusCommandResponse {
        return modbusApi.writeMcuModbusPwmDuty(request)
    }

    suspend fun servoAngle(
        request: McuModbusServoAngleRequest,
    ): McuModbusCommandResponse {
        return modbusApi.writeMcuModbusServoAngle(request)
    }
}
