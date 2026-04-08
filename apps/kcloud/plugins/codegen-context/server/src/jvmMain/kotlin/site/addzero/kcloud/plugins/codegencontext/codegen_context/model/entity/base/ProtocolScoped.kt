package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.MappedSuperclass
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate

@MappedSuperclass
interface ProtocolScoped {
    @ManyToOne
    val protocolTemplate: ProtocolTemplate
}
