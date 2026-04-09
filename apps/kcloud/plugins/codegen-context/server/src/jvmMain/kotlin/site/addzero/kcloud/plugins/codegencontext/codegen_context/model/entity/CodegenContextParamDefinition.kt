package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenCoded
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType

@Entity
@Table(name = "codegen_context_param_definition")
/**
 * 定义代码生成上下文参数定义实体。
 */
interface CodegenContextParamDefinition : BaseEntity, CodegenCoded, CodegenNamed, CodegenSorted {

    /**
     * 取值类型。
     */
    val valueType: CodegenContextValueType

    /**
     * 是否必填。
     */
    val required: Boolean

    /**
     * 默认值。
     */
    val defaultValue: String?

    /**
     * 枚举选项列表。
     */
    val enumOptions: String?

    /**
     * 占位提示。
     */
    val placeholder: String?

    @ManyToOne
    /**
     * 定义。
     */
    val definition: CodegenContextDefinition
}
