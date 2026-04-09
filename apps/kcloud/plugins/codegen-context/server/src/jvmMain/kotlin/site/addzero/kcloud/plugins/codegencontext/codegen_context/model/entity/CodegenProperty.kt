package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted

@Entity
@Table(name = "codegen_context_property")
/**
 * 定义代码生成属性实体。
 */
interface CodegenProperty : BaseEntity, CodegenNamed, CodegenSorted {

    /**
     * 属性名。
     */
    val propertyName: String

    /**
     * 类型名。
     */
    val typeName: String

    /**
     * 是否可空。
     */
    val nullable: Boolean

    /**
     * 默认字面量。
     */
    val defaultLiteral: String?

    @ManyToOne
    /**
     * owner类。
     */
    val ownerClass: CodegenClass

    @OneToMany(mappedBy = "ownerProperty")
    /**
     * 绑定列表。
     */
    val bindings: List<CodegenContextBinding>
}
