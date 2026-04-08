package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection

@Entity
@Table(name = "codegen_context_schema")
interface CodegenSchema : BaseEntity, CodegenNamed, CodegenSorted {

    val direction: CodegenSchemaDirection

    val functionCode: CodegenFunctionCode

    val baseAddress: Int

    val methodName: String

    val modelName: String?

    @ManyToOne
    val context: CodegenContext

    @OneToMany(mappedBy = "schema")
    val fields: List<CodegenField>
}
