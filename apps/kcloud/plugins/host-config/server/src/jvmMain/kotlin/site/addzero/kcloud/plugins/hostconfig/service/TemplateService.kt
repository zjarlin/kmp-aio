package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.routes.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.routes.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
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
