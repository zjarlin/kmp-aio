package site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface CodegenSorted {
    val sortIndex: Int
}
