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
interface CodegenContext : BaseEntity, CodegenNamed, ProtocolScoped {

    @Key
    val code: String

    val enabled: Boolean

    val consumerTarget: CodegenConsumerTarget

    @Column(name = "external_c_output_root")
    val externalCOutputRoot: String?

    @Column(name = "server_output_root")
    val serverOutputRoot: String?

    @Column(name = "shared_output_root")
    val sharedOutputRoot: String?

    @Column(name = "gateway_output_root")
    val gatewayOutputRoot: String?

    @Column(name = "api_client_output_root")
    val apiClientOutputRoot: String?

    @Column(name = "api_client_package_name")
    val apiClientPackageName: String?

    @Column(name = "spring_route_output_root")
    val springRouteOutputRoot: String?

    @Column(name = "c_output_root")
    val cOutputRoot: String?

    @Column(name = "markdown_output_root")
    val markdownOutputRoot: String?

    @Column(name = "rtu_port_path")
    val rtuPortPath: String

    @Column(name = "rtu_unit_id")
    val rtuUnitId: Int

    @Column(name = "rtu_baud_rate")
    val rtuBaudRate: Int

    @Column(name = "rtu_data_bits")
    val rtuDataBits: Int

    @Column(name = "rtu_stop_bits")
    val rtuStopBits: Int

    @Column(name = "rtu_parity")
    val rtuParity: String

    @Column(name = "rtu_timeout_ms")
    val rtuTimeoutMs: Long

    @Column(name = "rtu_retries")
    val rtuRetries: Int

    @Column(name = "tcp_host")
    val tcpHost: String

    @Column(name = "tcp_port")
    val tcpPort: Int

    @Column(name = "tcp_unit_id")
    val tcpUnitId: Int

    @Column(name = "tcp_timeout_ms")
    val tcpTimeoutMs: Long

    @Column(name = "tcp_retries")
    val tcpRetries: Int

    @OneToMany(mappedBy = "context")
    val schemas: List<CodegenSchema>
}
