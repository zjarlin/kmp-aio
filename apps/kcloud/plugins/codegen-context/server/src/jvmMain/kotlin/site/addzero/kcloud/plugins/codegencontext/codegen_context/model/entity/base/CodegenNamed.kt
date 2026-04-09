package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 定义代码生成命名契约。
 */
interface CodegenNamed {
    /**
     * 名称。
     */
    val name: String
    /**
     * 描述。
     */
    val description: String?
}
