package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenNamed
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base.CodegenSorted

@Entity
@Table(name = "codegen_context_method")
/**
 * 定义代码生成方法实体。
 */
interface CodegenMethod : BaseEntity, CodegenNamed, CodegenSorted {

    /**
     * 方法名。
     */
    val methodName: String

    /**
     * 请求类名。
     */
    val requestClassName: String?

    /**
     * 响应类名。
     */
    val responseClassName: String?

    @ManyToOne
    /**
     * owner类。
     */
    val ownerClass: CodegenClass

    @OneToMany(mappedBy = "ownerMethod")
    /**
     * 绑定列表。
     */
    val bindings: List<CodegenContextBinding>
}
