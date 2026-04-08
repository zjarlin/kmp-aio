package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

@Entity
@Table(name = "codegen_context_field")
interface CodegenField : BaseEntity, CodegenNamed, CodegenSorted {

    val propertyName: String

    val transportType: CodegenTransportType

    val registerOffset: Int

    val bitOffset: Int

    val length: Int

    val translationHint: String?

    val defaultLiteral: String?

    @ManyToOne
    val schema: CodegenSchema
}
