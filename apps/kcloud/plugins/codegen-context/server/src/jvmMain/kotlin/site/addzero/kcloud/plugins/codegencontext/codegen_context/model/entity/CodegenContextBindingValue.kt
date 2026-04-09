package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "codegen_context_binding_value")
/**
 * 定义代码生成上下文绑定值实体。
 */
interface CodegenContextBindingValue : BaseEntity {

    /**
     * 值。
     */
    val value: String?

    @ManyToOne
    /**
     * 绑定。
     */
    val binding: CodegenContextBinding

    @ManyToOne
    /**
     * 参数定义。
     */
    val paramDefinition: CodegenContextParamDefinition
}
