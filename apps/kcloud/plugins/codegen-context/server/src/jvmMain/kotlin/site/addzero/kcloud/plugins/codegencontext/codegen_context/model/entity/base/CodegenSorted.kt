package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 定义代码生成排序契约。
 */
interface CodegenSorted {
    /**
     * 排序序号。
     */
    val sortIndex: Int
}
