package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind

@Entity
@Table(name = "codegen_context_class")
/**
 * 定义代码生成类实体。
 */
interface CodegenClass : BaseEntity, CodegenNamed, CodegenSorted {

    /**
     * 类类型。
     */
    val classKind: CodegenClassKind

    /**
     * 类名。
     */
    val className: String

    /**
     * 包名。
     */
    val packageName: String?

    @ManyToOne
    /**
     * 上下文。
     */
    val context: CodegenContext

    @OneToMany(mappedBy = "ownerClass")
    /**
     * 方法。
     */
    val methods: List<CodegenMethod>

    @OneToMany(mappedBy = "ownerClass")
    /**
     * 属性。
     */
    val properties: List<CodegenProperty>

    @OneToMany(mappedBy = "ownerClass")
    /**
     * 绑定列表。
     */
    val bindings: List<CodegenContextBinding>
}
