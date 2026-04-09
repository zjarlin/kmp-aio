package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.ProtocolScoped
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget

@Entity
@Table(name = "codegen_context_context")
/**
 * 定义代码生成上下文实体。
 */
interface CodegenContext : BaseEntity, CodegenNamed, ProtocolScoped {

    @Key
    /**
     * 编码。
     */
    val code: String

    /**
     * 是否启用。
     */
    val enabled: Boolean

    /**
     * 消费目标。
     */
    val consumerTarget: CodegenConsumerTarget

    @Column(name = "external_c_output_root")
    /**
     * 外部 C 输出根目录。
     */
    val externalCOutputRoot: String?

    @Column(name = "server_output_root")
    /**
     * 服务端输出根目录。
     */
    val serverOutputRoot: String?

    @Column(name = "shared_output_root")
    /**
     * 共享输出根目录。
     */
    val sharedOutputRoot: String?

    @Column(name = "gateway_output_root")
    /**
     * 网关输出根目录。
     */
    val gatewayOutputRoot: String?

    @Column(name = "api_client_output_root")
    /**
     * API 客户端输出根目录。
     */
    val apiClientOutputRoot: String?

    @Column(name = "api_client_package_name")
    /**
     * API 客户端包名。
     */
    val apiClientPackageName: String?

    @Column(name = "spring_route_output_root")
    /**
     * Spring 路由输出根目录。
     */
    val springRouteOutputRoot: String?

    @Column(name = "c_output_root")
    /**
     * C 输出根目录。
     */
    val cOutputRoot: String?

    @Column(name = "markdown_output_root")
    /**
     * Markdown 输出根目录。
     */
    val markdownOutputRoot: String?

    @Column(name = "kotlin_client_transports")
    /**
     * Kotlin 调用侧启用的传输集合。
     */
    val kotlinClientTransports: String?

    @Column(name = "c_expose_transports")
    /**
     * C 暴露侧启用的传输集合。
     */
    val cExposeTransports: String?

    @Column(name = "artifact_kinds")
    /**
     * 导出目标集合。
     */
    val artifactKinds: String?

    @Column(name = "c_output_project_dir")
    /**
     * C 工程根目录。
     */
    val cOutputProjectDir: String?

    @Column(name = "bridge_impl_path")
    /**
     * Bridge 实现相对路径。
     */
    val bridgeImplPath: String?

    @Column(name = "keil_uvprojx_path")
    /**
     * Keil 工程文件路径。
     */
    val keilUvprojxPath: String?

    @Column(name = "keil_target_name")
    /**
     * Keil 目标名称。
     */
    val keilTargetName: String?

    @Column(name = "keil_group_name")
    /**
     * Keil 分组名称。
     */
    val keilGroupName: String?

    @Column(name = "mxproject_path")
    /**
     * CubeMX 工程文件路径。
     */
    val mxprojectPath: String?

    @Column(name = "rtu_port_path")
    /**
     * RTU端口路径。
     */
    val rtuPortPath: String

    @Column(name = "rtu_unit_id")
    /**
     * RTU单元 ID。
     */
    val rtuUnitId: Int

    @Column(name = "rtu_baud_rate")
    /**
     * RTU波特率。
     */
    val rtuBaudRate: Int

    @Column(name = "rtu_data_bits")
    /**
     * RTU数据位。
     */
    val rtuDataBits: Int

    @Column(name = "rtu_stop_bits")
    /**
     * RTU停止位。
     */
    val rtuStopBits: Int

    @Column(name = "rtu_parity")
    /**
     * RTU校验位。
     */
    val rtuParity: String

    @Column(name = "rtu_timeout_ms")
    /**
     * RTU超时时间（毫秒）。
     */
    val rtuTimeoutMs: Long

    @Column(name = "rtu_retries")
    /**
     * RTU重试次数。
     */
    val rtuRetries: Int

    @Column(name = "tcp_host")
    /**
     * TCP主机。
     */
    val tcpHost: String

    @Column(name = "tcp_port")
    /**
     * TCP端口。
     */
    val tcpPort: Int

    @Column(name = "tcp_unit_id")
    /**
     * TCP单元 ID。
     */
    val tcpUnitId: Int

    @Column(name = "tcp_timeout_ms")
    /**
     * TCP超时时间（毫秒）。
     */
    val tcpTimeoutMs: Long

    @Column(name = "tcp_retries")
    /**
     * TCP重试次数。
     */
    val tcpRetries: Int

    @Column(name = "mqtt_broker_url")
    /**
     * MQTT代理地址。
     */
    val mqttBrokerUrl: String

    @Column(name = "mqtt_client_id")
    /**
     * MQTT客户端 ID。
     */
    val mqttClientId: String

    @Column(name = "mqtt_request_topic")
    /**
     * MQTT请求主题。
     */
    val mqttRequestTopic: String

    @Column(name = "mqtt_response_topic")
    /**
     * MQTT响应主题。
     */
    val mqttResponseTopic: String

    @Column(name = "mqtt_qos")
    /**
     * MQTTQoS。
     */
    val mqttQos: Int

    @Column(name = "mqtt_timeout_ms")
    /**
     * MQTT超时时间（毫秒）。
     */
    val mqttTimeoutMs: Long

    @Column(name = "mqtt_retries")
    /**
     * MQTT重试次数。
     */
    val mqttRetries: Int

    @OneToMany(mappedBy = "context")
    /**
     * 类定义列表。
     */
    val classes: List<CodegenClass>
}
