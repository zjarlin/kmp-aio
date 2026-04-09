package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
/**
 * 提供代码生成模板相关服务。
 *
 * @property sql Jimmer SQL 客户端。
 */
class CodegenTemplateService(
    private val sql: KSqlClient,
) {
    private companion object {
        val SUPPORTED_PROTOCOL_TEMPLATE_CODES = setOf(
            "MODBUS_RTU_CLIENT",
            "MODBUS_TCP_CLIENT",
        )
    }

    /**
     * 列出协议模板。
     */
    fun listProtocolTemplates(): List<ProtocolTemplateOptionDto> {
        return sql.createQuery(ProtocolTemplate::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(site.addzero.kcloud.plugins.hostconfig.service.Fetchers.protocolTemplate))
        }.execute().filter { template ->
            template.code in SUPPORTED_PROTOCOL_TEMPLATE_CODES
        }.map { template ->
            ProtocolTemplateOptionDto(
                id = template.id,
                code = template.code,
                name = template.name,
                description = template.description,
                sortIndex = template.sortIndex,
            )
        }
    }
}
