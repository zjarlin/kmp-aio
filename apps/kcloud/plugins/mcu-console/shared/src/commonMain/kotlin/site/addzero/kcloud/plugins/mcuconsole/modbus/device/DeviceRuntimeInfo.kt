package site.addzero.kcloud.plugins.mcuconsole.modbus.device

/**
 * 板卡运行信息。
 */
data class DeviceRuntimeInfo(
    /** 板子固件版本号。 */
    val firmwareVersion: String,
    /** CPU 型号。 */
    val cpuModel: String,
    /** 晶振频率，单位 Hz。 */
    val xtalFrequencyHz: Int,
    /** Flash 容量，单位字节。 */
    val flashSizeBytes: Int,
    /** MAC 地址。 */
    val macAddress: String,
)
