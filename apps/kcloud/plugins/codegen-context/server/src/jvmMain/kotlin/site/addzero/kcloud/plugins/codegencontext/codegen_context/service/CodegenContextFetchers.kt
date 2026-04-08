package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenContext
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenField
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.CodegenSchema
import site.addzero.kcloud.plugins.codegencontext.codegen_context.model.entity.by

object CodegenContextFetchers {
    val contextSummary: Fetcher<CodegenContext> = newFetcher(CodegenContext::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
    }

    val contextDetail: Fetcher<CodegenContext> = newFetcher(CodegenContext::class).by {
        allScalarFields()
        protocolTemplate {
            allScalarFields()
        }
        schemas {
            allScalarFields()
            fields {
                allScalarFields()
            }
        }
    }

    val schemaDetail: Fetcher<CodegenSchema> = newFetcher(CodegenSchema::class).by {
        allScalarFields()
        fields {
            allScalarFields()
        }
    }

    val fieldDetail: Fetcher<CodegenField> = newFetcher(CodegenField::class).by {
        allScalarFields()
    }
}
