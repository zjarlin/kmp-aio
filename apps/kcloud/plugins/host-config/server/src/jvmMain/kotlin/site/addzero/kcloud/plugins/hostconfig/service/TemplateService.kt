package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
/**
 * 提供模板相关服务。
 *
 * @property sql Jimmer SQL 客户端。
 */
class TemplateService(
    private val sql: KSqlClient,
) {

    private companion object {
        val SUPPORTED_PROTOCOL_TEMPLATE_CODES = setOf(
            "MODBUS_RTU_CLIENT",
            "MODBUS_TCP_CLIENT",
            "MQTT_CLIENT",
        )
    }

    /**
     * 列出协议模板。
     */
    fun listProtocolTemplates(): List<TemplateOptionResponse> {
        return sql.createQuery(ProtocolTemplate::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.protocolTemplate))
        }.execute().filter {
            it.code in SUPPORTED_PROTOCOL_TEMPLATE_CODES
        }.map {
            TemplateOptionResponse(
                id = it.id,
                code = it.code,
                name = it.name,
                description = it.description,
                sortIndex = it.sortIndex,
            )
        }
    }

    /**
     * 列出模块模板。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    fun listModuleTemplates(protocolTemplateId: Long): List<ModuleTemplateOptionResponse> {
        return sql.createQuery(ModuleTemplate::class) {
            where(table.protocolTemplate.id eq protocolTemplateId)
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.moduleTemplate))
        }.execute().map {
            ModuleTemplateOptionResponse(
                id = it.id,
                protocolTemplateId = it.protocolTemplate.id,
                code = it.code,
                name = it.name,
                description = it.description,
                sortIndex = it.sortIndex,
                channelCount = it.channelCount,
            )
        }
    }

    /**
     * 列出设备类型。
     */
    fun listDeviceTypes(): List<TemplateOptionResponse> {
        return sql.createQuery(DeviceType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.deviceType))
        }.execute().map {
            TemplateOptionResponse(
                id = it.id,
                code = it.code,
                name = it.name,
                description = it.description,
                sortIndex = it.sortIndex,
            )
        }
    }

    /**
     * 列出register类型。
     */
    fun listRegisterTypes(): List<TemplateOptionResponse> {
        return sql.createQuery(RegisterType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.registerType))
        }.execute().map {
            TemplateOptionResponse(
                id = it.id,
                code = it.code,
                name = it.name,
                description = it.description,
                sortIndex = it.sortIndex,
            )
        }
    }

    /**
     * 列出数据类型。
     */
    fun listDataTypes(): List<TemplateOptionResponse> {
        return sql.createQuery(DataType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.dataType))
        }.execute().map {
            TemplateOptionResponse(
                id = it.id,
                code = it.code,
                name = it.name,
                description = it.description,
                sortIndex = it.sortIndex,
            )
        }
    }
}
