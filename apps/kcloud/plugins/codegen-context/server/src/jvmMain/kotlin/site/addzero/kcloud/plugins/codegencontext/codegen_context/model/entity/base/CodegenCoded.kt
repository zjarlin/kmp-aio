package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 定义代码生成编码契约。
 */
interface CodegenCoded {
    /**
     * 编码。
     */
    val code: String
}
