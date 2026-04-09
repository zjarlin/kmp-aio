package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted

@Entity
@Table(name = "codegen_context_binding")
/**
 * 定义代码生成上下文绑定实体。
 */
interface CodegenContextBinding : BaseEntity, CodegenSorted {

    @ManyToOne
    /**
     * 定义。
     */
    val definition: CodegenContextDefinition

    @ManyToOne
    /**
     * owner类。
     */
    val ownerClass: CodegenClass?

    @ManyToOne
    /**
     * owner方法。
     */
    val ownerMethod: CodegenMethod?

    @ManyToOne
    /**
     * owner属性。
     */
    val ownerProperty: CodegenProperty?

    @OneToMany(mappedBy = "binding")
    /**
     * 绑定值列表。
     */
    val values: List<CodegenContextBindingValue>
}
