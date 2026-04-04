package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 板卡链路探活的 Modbus RTU 契约。
 *
 * 这份契约的目的不是表达“串口已建立连接”。
 * Modbus RTU 本身没有 TCP 那种 connect / disconnect 事件，
 * 所以下位机只能通过“最近是否收到主站合法请求”来判断上位机是否在线。
 *
 * 推荐实现约定:
 * 1. 上位机固定周期调用 [probeMasterLink]。
 * 2. 下位机只要收到任意一帧发给本机地址、CRC 正确、功能码合法的请求，
 *    就刷新 `lastMasterSeenAt`。
 * 3. 下位机若超过 [DeviceLinkPollingContract.OFFLINE_TIMEOUT_MS] 仍未刷新，
 *    就判定主站或当前线路失联，可进入备用线路切换流程。
 *
 * 为什么单独约定一个探活接口:
 * - 比反复读取大块设备信息更轻。
 * - 比“定时写心跳寄存器”更简单，不需要额外写状态。
 * - 下位机实际上不必关心主站读的是哪个寄存器，只要有合法请求就算在线；
 *   但上位机固定读同一个廉价地址，便于前后端和固件统一实现。
 *
 * 轮询建议:
 * - 正常轮询周期: [DeviceLinkPollingContract.DEFAULT_POLL_INTERVAL_MS]
 * - 单次请求超时: 300 ~ 800 ms
 * - 连续失败 [DeviceLinkPollingContract.DEFAULT_MAX_CONSECUTIVE_FAILURES_BEFORE_SWITCH] 次后，
 *   上位机可主动切换线路
 * - 下位机超过 [DeviceLinkPollingContract.OFFLINE_TIMEOUT_MS] 未见合法请求后，
 *   也可被动切换线路
 *
 * 不建议做法:
 * - 不要在 while(true) 里无间隔狂发，会把串口和总线占满。
 * - 不要把“收到任意字节”当成在线，至少要校验站号、功能码、CRC。
 * - 不要只靠上位机保存一个布尔连接状态，下位机仍应自己做超时判定。
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
     * 正常保活轮询周期。
     *
     * 一般让上位机每 1 秒读一次 [DeviceLinkApi.probeMasterLink] 即可，
     * 既能较快发现断线，也不至于把总线打满。
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
