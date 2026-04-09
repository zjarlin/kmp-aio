package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenCoded
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.ProtocolScoped
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenBindingTargetMode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenDefinitionSourceKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

@Entity
@Table(name = "codegen_context_definition")
/**
 * 定义代码生成上下文定义实体。
 */
interface CodegenContextDefinition : BaseEntity, CodegenCoded, CodegenNamed, CodegenSorted, ProtocolScoped {

    /**
     * 目标类型。
     */
    val targetKind: CodegenNodeKind

    /**
     * 绑定目标模式。
     */
    val bindingTargetMode: CodegenBindingTargetMode

    /**
     * 来源类型。
     */
    val sourceKind: CodegenDefinitionSourceKind

    @OneToMany(mappedBy = "definition")
    /**
     * 参数定义列表。
     */
    val params: List<CodegenContextParamDefinition>
}
