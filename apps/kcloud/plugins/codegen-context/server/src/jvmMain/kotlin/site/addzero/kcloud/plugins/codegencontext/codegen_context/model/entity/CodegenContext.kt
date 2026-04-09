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

    @OneToMany(mappedBy = "context")
    val schemas: List<CodegenSchema>
}
