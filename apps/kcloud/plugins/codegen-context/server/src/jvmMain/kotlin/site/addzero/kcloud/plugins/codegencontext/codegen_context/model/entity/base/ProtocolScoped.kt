package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.MappedSuperclass
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate

@MappedSuperclass
/**
 * 定义协议作用域契约。
 */
interface ProtocolScoped {
    @ManyToOne
    /**
     * 协议模板。
     */
    val protocolTemplate: ProtocolTemplate
}
