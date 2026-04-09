package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 定义设备关联API契约。
 */
interface DeviceLinkApi {
    /**
     * 前端/调度用途: Modbus 页后台保活轮询、链路切换前的快速探测。
     *
     * 约定读取一位离散输入作为“设备在线且当前从站协议栈可正常响应”的探针。
     * 对下位机来说，只要本请求被成功解析并响应，就应视为“上位机在线”。
     *
     * 返回值建议固件固定为 `true`:
     * - `true`: 从站协议栈在线，且本次请求已成功闭环
     * - 异常/超时: 由主站通信层处理，视为本次探活失败
     *
     * 地址 `26` 目前紧跟在现有灯状态线圈 `0..25` 之后，
     * 作为一个廉价探针位使用。如果固件寄存器表另有规划，需要双方一起改。
     */
    @ModbusOperation(
        address = 26,
        functionCode = ModbusFunctionCode.READ_DISCRETE_INPUTS,
    )
    suspend fun probeMasterLink(): Boolean
}

/**
 * 链路轮询与切换的建议常量。
 *
 * 这里放的是“约定值”，不是协议硬限制。
 * 上位机调度器、后端服务、下位机超时状态机最好都优先复用这里的语义。
 */
object DeviceLinkPollingContract {
    /**
     * 默认POLL间隔毫秒。
     */
    const val DEFAULT_POLL_INTERVAL_MS: Long = 1_000

    /**
     * 下位机判定主站离线的超时时间。
     *
     * 按 1 秒一次轮询估算，给 3 个周期容错，
     * 可以覆盖偶发丢包、一次请求超时、串口瞬时忙等情况。
     */
    const val OFFLINE_TIMEOUT_MS: Long = 3_000

    /**
     * 上位机在主动切换线路前，允许的连续失败次数。
     *
     * 例如:
     * - 第 1 次失败: 重试
     * - 第 2 次失败: 再重试
     * - 第 3 次失败: 判定当前线路不可用，切备用线路
     */
    const val DEFAULT_MAX_CONSECUTIVE_FAILURES_BEFORE_SWITCH = 3
}
