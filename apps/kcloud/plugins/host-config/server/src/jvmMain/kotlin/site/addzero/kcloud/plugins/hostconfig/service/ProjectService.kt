package site.addzero.kcloud.plugins.hostconfig.service

import java.sql.ResultSet
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import site.addzero.util.db.SqlExecutor
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.TagTreeNode
import site.addzero.kmp.exp.ConflictException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

/**
 * 提供项目、协议、模块和设备的主机配置管理服务。
 *
 * @property sql Jimmer SQL 客户端。
 * @property jdbc 主机配置 JDBC 工具。
 */
@Single
class ProjectService(
    private val sql: KSqlClient,
    private val jdbc: SqlExecutor,
) {

    private companion object {
        val SUPPORTED_PROTOCOL_NAMES = setOf("ModbusRTU", "ModbusTCP", "MQTT")
    }

    /**
     * 列出项目。
     */
    fun listProjects(): List<ProjectResponse> {
        return sql.createQuery(Project::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.projectScalar))
        }.execute()
            .map { it.toResponse() }
    }

    /**
     * 获取项目。
     *
     * @param projectId 项目 ID。
     */
    fun getProject(projectId: Long): ProjectResponse {
        return loadProject(projectId).toResponse()
    }

    /**
     * 获取项目树。
     *
     * @param projectId 项目 ID。
     */
    fun getProjectTree(projectId: Long): ProjectTreeResponse {
        val project = sql.createQuery(Project::class) {
            where(table.id eq projectId)
            select(table.fetch(Fetchers.projectTree))
        }.execute().firstOrNull() ?: throw NotFoundException("Project not found")
        return project.toTreeResponse()
    }

    /**
     * 列出协议。
     */
    fun listProtocols(): List<ProtocolCatalogItemResponse> {
        return sql.createQuery(ProtocolInstance::class) {
            orderBy(table.name.asc(), table.id.asc())
            select(table.fetch(Fetchers.protocolScalar))
        }.execute().filter {
                it.name in SUPPORTED_PROTOCOL_NAMES
            }
            .map {
                ProtocolCatalogItemResponse(
                    id = it.id,
                    name = it.name,
                    pollingIntervalMs = it.pollingIntervalMs,
                    protocolTemplateId = it.protocolTemplate.id,
                    protocolTemplateCode = it.protocolTemplate.code,
                    protocolTemplateName = it.protocolTemplate.name,
                )
            }
    }

    /**
     * 创建项目。
     *
     * @param request 请求参数。
     */
    fun createProject(request: ProjectCreateRequest): ProjectResponse {
        val name = request.name.trim()
        ensureProjectNameUnique(name, null)
        val now = now()
        val entity = Project {
            this.name = name
            this.description = request.description.cleanNullable()
            this.remark = request.remark.cleanNullable()
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val project = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return project.toResponse()
    }

    /**
     * 更新项目。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun updateProject(projectId: Long, request: ProjectUpdateRequest): ProjectResponse {
        ensureProjectExists(projectId)
        val name = request.name.trim()
        ensureProjectNameUnique(name, projectId)
        val now = now()
        val entity = Project {
            id = projectId
            this.name = name
            this.description = request.description.cleanNullable()
            this.remark = request.remark.cleanNullable()
            this.sortIndex = request.sortIndex
            this.updatedAt = now
        }
        val project = sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return project.toResponse()
    }

    /**
     * 更新项目位置。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun updateProjectPosition(projectId: Long, request: ProjectPositionUpdateRequest): ProjectResponse {
        ensureProjectExists(projectId)
        reorderProjects(projectId, request.sortIndex)
        return loadProject(projectId).toResponse()
    }

    /**
     * 删除项目。
     *
     * @param projectId 项目 ID。
     */
    fun deleteProject(projectId: Long) {
        ensureProjectExists(projectId)
        val protocolIds = queryIds(
            "SELECT protocol_id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC",
            projectId,
        )
        executeUpdate("DELETE FROM host_config_project WHERE id = ?", projectId)
        deleteOrphanProtocols(protocolIds)
    }

    /**
     * 创建协议。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun createProtocol(projectId: Long, request: ProtocolCreateRequest): ProtocolResponse {
        ensureProjectExists(projectId)
        val protocolTemplate = loadProtocolTemplate(request.protocolTemplateId)
        ensureProjectProtocolTemplateUnique(
            projectId = projectId,
            protocolTemplateId = protocolTemplate.id,
        )
        val name = request.name.trim()
        ensureProtocolNameUnique(name, null)
        val now = now()
        val transportConfig = normalizeProtocolTransportConfig(
            protocolTemplateCode = protocolTemplate.code,
            requestConfig = request.transportConfig,
        )
        val entity = ProtocolInstance {
            this.protocolTemplateId = request.protocolTemplateId
            this.name = name
            this.pollingIntervalMs = request.pollingIntervalMs
            this.transportType = transportConfig?.transportType
            this.host = transportConfig?.host.cleanNullable()
            this.tcpPort = transportConfig?.tcpPort
            this.portName = transportConfig?.portName.cleanNullable()
            this.baudRate = transportConfig?.baudRate
            this.dataBits = transportConfig?.dataBits
            this.stopBits = transportConfig?.stopBits
            this.parity = transportConfig?.parity
            this.responseTimeoutMs = transportConfig?.responseTimeoutMs
            this.createdAt = now
            this.updatedAt = now
        }
        val protocol = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        val link = createProjectProtocolLink(projectId, protocol.id, request.sortIndex, now)
        reorderProtocolLinks(projectId, link.id, request.sortIndex)
        return loadProjectProtocol(projectId, protocol.id).toResponse()
    }

    /**
     * 关联协议。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
    fun linkProtocol(projectId: Long, request: LinkExistingProtocolRequest): ProtocolResponse {
        ensureProjectExists(projectId)
        val protocol = loadProtocol(request.protocolId)
        ensureProjectProtocolTemplateUnique(
            projectId = projectId,
            protocolTemplateId = protocol.protocolTemplate.id,
            excludeProtocolId = protocol.id,
        )
        ensureProtocolNotLinked(projectId, request.protocolId)
        val link = createProjectProtocolLink(projectId, request.protocolId, request.sortIndex, now())
        reorderProtocolLinks(projectId, link.id, request.sortIndex)
        return loadProjectProtocol(projectId, request.protocolId).toResponse()
    }

    /**
     * 更新协议。
     *
     * @param protocolId 协议 ID。
     * @param request 请求参数。
     */
    fun updateProtocol(protocolId: Long, request: ProtocolUpdateRequest): ProtocolResponse {
        val link = loadProjectProtocol(request.projectId, protocolId)
        val protocolTemplate = loadProtocolTemplate(request.protocolTemplateId)
        ensureProjectProtocolTemplateUnique(
            projectId = request.projectId,
            protocolTemplateId = protocolTemplate.id,
            excludeProtocolId = protocolId,
        )
        val name = request.name.trim()
        ensureProtocolNameUnique(name, protocolId)
        val transportConfig = normalizeProtocolTransportConfig(
            protocolTemplateCode = protocolTemplate.code,
            requestConfig = request.transportConfig,
        )
        val entity = ProtocolInstance {
            id = protocolId
            protocolTemplateId = request.protocolTemplateId
            this.name = name
            this.pollingIntervalMs = request.pollingIntervalMs
            this.transportType = transportConfig?.transportType
            this.host = transportConfig?.host.cleanNullable()
            this.tcpPort = transportConfig?.tcpPort
            this.portName = transportConfig?.portName.cleanNullable()
            this.baudRate = transportConfig?.baudRate
            this.dataBits = transportConfig?.dataBits
            this.stopBits = transportConfig?.stopBits
            this.parity = transportConfig?.parity
            this.responseTimeoutMs = transportConfig?.responseTimeoutMs
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        reorderProtocolLinks(request.projectId, link.id, request.sortIndex)
        return loadProjectProtocol(request.projectId, protocolId).toResponse()
    }

    /**
     * 更新协议位置。
     *
     * @param protocolId 协议 ID。
     * @param request 请求参数。
     */
    fun updateProtocolPosition(protocolId: Long, request: ProtocolPositionUpdateRequest): ProtocolResponse {
        ensureProjectExists(request.sourceProjectId)
        ensureProjectExists(request.targetProjectId)
        val link = loadProjectProtocol(request.sourceProjectId, protocolId)
        val protocol = loadProtocol(protocolId)
        ensureProjectProtocolTemplateUnique(
            projectId = request.targetProjectId,
            protocolTemplateId = protocol.protocolTemplate.id,
            excludeProtocolId = protocolId,
        )
        moveProtocol(link.id, protocolId, request.sourceProjectId, request.targetProjectId, request.sortIndex)
        return loadProjectProtocol(request.targetProjectId, protocolId).toResponse()
    }

    /**
     * 删除协议。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     */
    fun deleteProtocol(projectId: Long, protocolId: Long) {
        val link = loadProjectProtocol(projectId, protocolId)
        executeUpdate("DELETE FROM host_config_project_protocol WHERE id = ?", link.id)
        normalizeProtocolLinks(projectId)
        deleteOrphanProtocols(listOf(protocolId))
    }

    /**
     * 创建模块。
     *
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
    fun createModule(deviceId: Long, request: ModuleCreateRequest): ModuleResponse {
        val device = loadDevice(deviceId)
        val moduleTemplate = loadModuleTemplate(request.moduleTemplateId)
        ensureModuleTemplateMatchesProtocol(
            protocolTemplateId = device.protocol.protocolTemplate.id,
            moduleTemplate = moduleTemplate,
        )
        val name = request.name.trim()
        ensureModuleNameUnique(deviceId, name, null)
        val now = now()
        val entity = ModuleInstance {
            this.deviceId = deviceId
            this.protocolId = device.protocol.id
            this.moduleTemplateId = request.moduleTemplateId
            this.name = name
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val module = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadModule(module.id).toResponse()
    }

    /**
     * 更新模块。
     *
     * @param moduleId 模块 ID。
     * @param request 请求参数。
     */
    fun updateModule(moduleId: Long, request: ModuleUpdateRequest): ModuleResponse {
        val current = loadModule(moduleId)
        val moduleTemplate = loadModuleTemplate(request.moduleTemplateId)
        ensureModuleTemplateMatchesProtocol(
            protocolTemplateId = current.device.protocol.protocolTemplate.id,
            moduleTemplate = moduleTemplate,
        )
        val name = request.name.trim()
        ensureModuleNameUnique(current.device.id, name, moduleId)
        val entity = ModuleInstance {
            id = moduleId
            deviceId = current.device.id
            protocolId = current.protocol.id
            moduleTemplateId = request.moduleTemplateId
            this.name = name
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        val module = sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return loadModule(module.id).toResponse()
    }

    /**
     * 更新模块位置。
     *
     * @param moduleId 模块 ID。
     * @param request 请求参数。
     */
    fun updateModulePosition(moduleId: Long, request: ModulePositionUpdateRequest): ModuleResponse {
        val current = loadModule(moduleId)
        val targetDevice = loadDevice(request.deviceId)
        ensureModuleTemplateMatchesProtocol(
            protocolTemplateId = targetDevice.protocol.protocolTemplate.id,
            moduleTemplate = current.moduleTemplate,
        )
        ensureModuleNameUnique(targetDevice.id, current.name, moduleId)
        moveModule(
            moduleId = moduleId,
            currentDeviceId = current.device.id,
            targetDeviceId = targetDevice.id,
            currentProtocolId = current.protocol.id,
            targetProtocolId = targetDevice.protocol.id,
            sortIndex = request.sortIndex,
        )
        return loadModule(moduleId).toResponse()
    }

    /**
     * 删除模块。
     *
     * @param moduleId 模块 ID。
     */
    fun deleteModule(moduleId: Long) {
        ensureModuleExists(moduleId)
        executeUpdate("DELETE FROM host_config_module_instance WHERE id = ?", moduleId)
    }

    /**
     * 创建设备。
     *
     * @param protocolId 协议 ID。
     * @param request 请求参数。
     */
    fun createDevice(protocolId: Long, request: DeviceCreateRequest): DeviceResponse {
        loadProtocol(protocolId)
        ensureDeviceTypeExists(request.deviceTypeId)
        val name = request.name.trim()
        ensureDeviceNameUnique(protocolId, name, null)
        val now = now()
        val entity = Device {
            this.protocolId = protocolId
            this.deviceTypeId = request.deviceTypeId
            this.name = name
            this.stationNo = request.stationNo
            this.requestIntervalMs = request.requestIntervalMs
            this.writeIntervalMs = request.writeIntervalMs
            this.byteOrder2 = request.byteOrder2
            this.byteOrder4 = request.byteOrder4
            this.floatOrder = request.floatOrder
            this.batchAnalogStart = request.batchAnalogStart
            this.batchAnalogLength = request.batchAnalogLength
            this.batchDigitalStart = request.batchDigitalStart
            this.batchDigitalLength = request.batchDigitalLength
            this.disabled = request.disabled
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val device = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadDevice(device.id).toResponse()
    }

    /**
     * 更新设备。
     *
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
    fun updateDevice(deviceId: Long, request: DeviceUpdateRequest): DeviceResponse {
        val current = loadDevice(deviceId)
        ensureDeviceTypeExists(request.deviceTypeId)
        val name = request.name.trim()
        ensureDeviceNameUnique(current.protocol.id, name, deviceId)
        val entity = Device {
            id = deviceId
            protocolId = current.protocol.id
            deviceTypeId = request.deviceTypeId
            this.name = name
            this.stationNo = request.stationNo
            this.requestIntervalMs = request.requestIntervalMs
            this.writeIntervalMs = request.writeIntervalMs
            this.byteOrder2 = request.byteOrder2
            this.byteOrder4 = request.byteOrder4
            this.floatOrder = request.floatOrder
            this.batchAnalogStart = request.batchAnalogStart
            this.batchAnalogLength = request.batchAnalogLength
            this.batchDigitalStart = request.batchDigitalStart
            this.batchDigitalLength = request.batchDigitalLength
            this.disabled = request.disabled
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        val device = sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return loadDevice(device.id).toResponse()
    }

    /**
     * 更新设备位置。
     *
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
    fun updateDevicePosition(deviceId: Long, request: DevicePositionUpdateRequest): DeviceResponse {
        val current = loadDevice(deviceId)
        val targetProtocol = loadProtocol(request.protocolId)
        ensureDeviceModulesMatchProtocol(
            deviceId = deviceId,
            targetProtocolTemplateId = targetProtocol.protocolTemplate.id,
        )
        ensureDeviceNameUnique(request.protocolId, current.name, deviceId)
        moveDevice(deviceId, current.protocol.id, request.protocolId, request.sortIndex)
        return loadDevice(deviceId).toResponse()
    }

    /**
     * 删除设备。
     *
     * @param deviceId 设备 ID。
     */
    fun deleteDevice(deviceId: Long) {
        ensureDeviceExists(deviceId)
        executeUpdate("DELETE FROM host_config_device WHERE id = ?", deviceId)
    }

    /**
     * 确保项目名称唯一性。
     *
     * @param name 名称。
     * @param excludeId 需要排除的对象 ID。
     */
    private fun ensureProjectNameUnique(name: String, excludeId: Long?) {
        val exists = sql.exists(Project::class) {
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Project name already exists")
        }
    }

    /**
     * 确保协议名称唯一性。
     *
     * @param name 名称。
     * @param excludeId 需要排除的对象 ID。
     */
    private fun ensureProtocolNameUnique(name: String, excludeId: Long?) {
        val exists = sql.exists(ProtocolInstance::class) {
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Protocol name already exists")
        }
    }

    /**
     * 确保模块名称唯一性。
     *
     * @param protocolId 协议 ID。
     * @param name 名称。
     * @param excludeId 需要排除的对象 ID。
     */
    private fun ensureModuleNameUnique(deviceId: Long, name: String, excludeId: Long?) {
        val exists = sql.exists(ModuleInstance::class) {
            where(table.device.id eq deviceId)
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Module name already exists")
        }
    }

    /**
     * 确保设备名称唯一性。
     *
     * @param moduleId 模块 ID。
     * @param name 名称。
     * @param excludeId 需要排除的对象 ID。
     */
    private fun ensureDeviceNameUnique(protocolId: Long, name: String, excludeId: Long?) {
        val exists = sql.exists(Device::class) {
            where(table.protocol.id eq protocolId)
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Device name already exists")
        }
    }

    /**
     * 确保项目存在性。
     *
     * @param projectId 项目 ID。
     */
    private fun ensureProjectExists(projectId: Long) {
        val exists = sql.exists(Project::class) {
            where(table.id eq projectId)
        }
        if (!exists) {
            throw NotFoundException("Project not found")
        }
    }

    /**
     * 确保项目内协议模板关联唯一。
     *
     * @param projectId 项目 ID。
     * @param protocolTemplateId 协议模板 ID。
     * @param excludeProtocolId 需要排除的协议 ID。
     */
    private fun ensureProjectProtocolTemplateUnique(
        projectId: Long,
        protocolTemplateId: Long,
        excludeProtocolId: Long? = null,
    ) {
        val exists = sql.exists(ProjectProtocol::class) {
            where(table.project.id eq projectId)
            where(table.protocol.protocolTemplate.id eq protocolTemplateId)
            if (excludeProtocolId != null) {
                where(table.protocol.id ne excludeProtocolId)
            }
        }
        if (exists) {
            throw ConflictException("Project already links a protocol for this template")
        }
    }

    /**
     * 确保模块存在性。
     *
     * @param moduleId 模块 ID。
     */
    private fun ensureModuleExists(moduleId: Long) {
        val exists = sql.exists(ModuleInstance::class) {
            where(table.id eq moduleId)
        }
        if (!exists) {
            throw NotFoundException("Module not found")
        }
    }

    /**
     * 确保设备存在性。
     *
     * @param deviceId 设备 ID。
     */
    private fun ensureDeviceExists(deviceId: Long) {
        val exists = sql.exists(Device::class) {
            where(table.id eq deviceId)
        }
        if (!exists) {
            throw NotFoundException("Device not found")
        }
    }

    /**
     * 确保设备类型存在性。
     *
     * @param deviceTypeId 设备类型 ID。
     */
    private fun ensureDeviceTypeExists(deviceTypeId: Long) {
        val exists = sql.exists(DeviceType::class) {
            where(table.id eq deviceTypeId)
        }
        if (!exists) {
            throw NotFoundException("Device type not found")
        }
    }

    /**
     * 加载项目。
     *
     * @param projectId 项目 ID。
     */
    private fun loadProject(projectId: Long): Project {
        return sql.createQuery(Project::class) {
            where(table.id eq projectId)
            select(table.fetch(Fetchers.projectScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Project not found")
    }

    /**
     * 加载协议。
     *
     * @param protocolId 协议 ID。
     */
    private fun loadProtocol(protocolId: Long): ProtocolInstance {
        return sql.createQuery(ProtocolInstance::class) {
            where(table.id eq protocolId)
            select(table.fetch(Fetchers.protocolScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol not found")
    }

    /**
     * 加载项目协议。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     */
    private fun loadProjectProtocol(projectId: Long, protocolId: Long): ProjectProtocol {
        return sql.createQuery(ProjectProtocol::class) {
            where(table.project.id eq projectId)
            where(table.protocol.id eq protocolId)
            select(table.fetch(Fetchers.projectProtocolScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol relation not found")
    }

    /**
     * 创建项目协议关联。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     * @param sortIndex 目标排序序号。
     * @param createdAt 创建时间戳。
     */
    private fun createProjectProtocolLink(projectId: Long, protocolId: Long, sortIndex: Int, createdAt: Long): ProjectProtocol {
        return sql.saveCommand(
            ProjectProtocol {
                this.projectId = projectId
                this.protocolId = protocolId
                this.sortIndex = sortIndex
                this.createdAt = createdAt
                this.updatedAt = createdAt
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
    }

    /**
     * 加载模块。
     *
     * @param moduleId 模块 ID。
     */
    private fun loadModule(moduleId: Long): ModuleInstance {
        return sql.createQuery(ModuleInstance::class) {
            where(table.id eq moduleId)
            select(table.fetch(Fetchers.moduleScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Module not found")
    }

    /**
     * 加载模块模板。
     *
     * @param moduleTemplateId 模块模板 ID。
     */
    private fun loadModuleTemplate(moduleTemplateId: Long): ModuleTemplate {
        return sql.createQuery(ModuleTemplate::class) {
            where(table.id eq moduleTemplateId)
            select(table.fetch(Fetchers.moduleTemplate))
        }.execute().firstOrNull() ?: throw NotFoundException("Module template not found")
    }

    /**
     * 加载协议模板。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    private fun loadProtocolTemplate(protocolTemplateId: Long): ProtocolTemplate {
        return sql.createQuery(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
            select(table.fetch(Fetchers.protocolTemplate))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol template not found")
    }

    /**
     * 加载设备。
     *
     * @param deviceId 设备 ID。
     */
    private fun loadDevice(deviceId: Long): Device {
        return sql.createQuery(Device::class) {
            where(table.id eq deviceId)
            select(table.fetch(Fetchers.deviceScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Device not found")
    }

    /**
     * 处理项目。
     */
    private fun Project.toResponse(): ProjectResponse =
        ProjectResponse(
            id = id,
            name = name,
            description = description,
            remark = remark,
            sortIndex = sortIndex,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    /**
     * 处理项目协议。
     */
    private fun ProjectProtocol.toResponse(): ProtocolResponse =
        ProtocolResponse(
            id = protocol.id,
            name = protocol.name,
            pollingIntervalMs = protocol.pollingIntervalMs,
            sortIndex = sortIndex,
            protocolTemplateId = protocol.protocolTemplate.id,
            transportConfig = protocol.toTransportConfig(),
        )

    /**
     * 处理模块instance。
     */
    private fun ModuleInstance.toResponse(): ModuleResponse =
        ModuleResponse(
            id = id,
            name = name,
            deviceId = device.id,
            protocolId = protocol.id,
            sortIndex = sortIndex,
            moduleTemplateId = moduleTemplate.id,
        )

    /**
     * 处理设备。
     */
    private fun Device.toResponse(): DeviceResponse =
        DeviceResponse(
            id = id,
            name = name,
            stationNo = stationNo,
            requestIntervalMs = requestIntervalMs,
            writeIntervalMs = writeIntervalMs,
            byteOrder2 = byteOrder2,
            byteOrder4 = byteOrder4,
            floatOrder = floatOrder,
            batchAnalogStart = batchAnalogStart,
            batchAnalogLength = batchAnalogLength,
            batchDigitalStart = batchDigitalStart,
            batchDigitalLength = batchDigitalLength,
            disabled = disabled,
            sortIndex = sortIndex,
            protocolId = protocol.id,
            deviceTypeId = deviceType.id,
        )

    /**
     * 处理项目。
     */
    private fun Project.toTreeResponse(): ProjectTreeResponse =
        ProjectTreeResponse(
            id = id,
            name = name,
            description = description,
            remark = remark,
            sortIndex = sortIndex,
            protocols = protocolLinks
                .sortedWith(compareBy(ProjectProtocol::sortIndex, ProjectProtocol::id))
                .map { link ->
                    val protocol = link.protocol
                    ProtocolTreeNode(
                        id = protocol.id,
                        name = protocol.name,
                        pollingIntervalMs = protocol.pollingIntervalMs,
                        sortIndex = link.sortIndex,
                        protocolTemplateId = protocol.protocolTemplate.id,
                        protocolTemplateCode = protocol.protocolTemplate.code,
                        protocolTemplateName = protocol.protocolTemplate.name,
                        transportConfig = protocol.toTransportConfig(),
                        devices = protocol.devices
                            .sortedWith(compareBy(Device::sortIndex, Device::id))
                            .map { it.toTreeNode() },
                    )
                },
            modules = protocolLinks
                .sortedWith(compareBy(ProjectProtocol::sortIndex, ProjectProtocol::id))
                .flatMap { link ->
                    link.protocol.devices
                        .sortedWith(compareBy(Device::sortIndex, Device::id))
                        .flatMap { device ->
                            device.modules
                                .sortedWith(compareBy(ModuleInstance::sortIndex, ModuleInstance::id))
                                .map { it.toTreeNode() }
                        }
                },
        )

    /**
     * 处理模块instance。
     */
    private fun ModuleInstance.toTreeNode(): ModuleTreeNode =
        ModuleTreeNode(
            id = id,
            name = name,
            deviceId = device.id,
            protocolId = protocol.id,
            sortIndex = sortIndex,
            moduleTemplateId = moduleTemplate.id,
            moduleTemplateCode = moduleTemplate.code,
            moduleTemplateName = moduleTemplate.name,
        )

    /**
     * 处理设备。
     */
    private fun Device.toTreeNode(): DeviceTreeNode =
        DeviceTreeNode(
            id = id,
            name = name,
            stationNo = stationNo,
            requestIntervalMs = requestIntervalMs,
            writeIntervalMs = writeIntervalMs,
            byteOrder2 = byteOrder2,
            byteOrder4 = byteOrder4,
            floatOrder = floatOrder,
            batchAnalogStart = batchAnalogStart,
            batchAnalogLength = batchAnalogLength,
            batchDigitalStart = batchDigitalStart,
            batchDigitalLength = batchDigitalLength,
            disabled = disabled,
            sortIndex = sortIndex,
            protocolId = protocol.id,
            deviceTypeId = deviceType.id,
            deviceTypeCode = deviceType.code,
            deviceTypeName = deviceType.name,
            modules = modules
                .sortedWith(compareBy(ModuleInstance::sortIndex, ModuleInstance::id))
                .map { it.toTreeNode() },
            tags = tags
                .sortedWith(compareBy(Tag::sortIndex, Tag::id))
                .map {
                    TagTreeNode(
                        id = it.id,
                        name = it.name,
                        sortIndex = it.sortIndex,
                    )
                },
        )

    /**
     * 处理协议instance。
     */
    private fun ProtocolInstance.toTransportConfig(): ProtocolTransportConfig? {
        val currentTransportType = transportType ?: return null
        return ProtocolTransportConfig(
            transportType = currentTransportType,
            host = host,
            tcpPort = tcpPort,
            portName = portName,
            baudRate = baudRate,
            dataBits = dataBits,
            stopBits = stopBits,
            parity = parity,
            responseTimeoutMs = responseTimeoutMs,
        )
    }

    /**
     * 根据协议模板规范化传输配置。
     *
     * @param protocolTemplateCode 协议模板编码。
     * @param requestConfig 请求中的传输配置。
     */
    private fun normalizeProtocolTransportConfig(
        protocolTemplateCode: String,
        requestConfig: ProtocolTransportConfig?,
    ): ProtocolTransportConfig? {
        return when (protocolTemplateCode) {
            "MODBUS_RTU_CLIENT" -> ProtocolTransportConfig(
                transportType = TransportType.RTU,
                portName = requestConfig?.portName.cleanNullable(),
                baudRate = requestConfig?.baudRate,
                dataBits = requestConfig?.dataBits,
                stopBits = requestConfig?.stopBits,
                parity = requestConfig?.parity,
                responseTimeoutMs = requestConfig?.responseTimeoutMs,
            )

            "MODBUS_TCP_CLIENT" -> ProtocolTransportConfig(
                transportType = TransportType.TCP,
                host = requestConfig?.host.cleanNullable(),
                tcpPort = requestConfig?.tcpPort,
                responseTimeoutMs = requestConfig?.responseTimeoutMs,
            )

            else -> null
        }
    }

    /**
     * 校验模块模板与协议模板是否匹配。
     *
     * @param protocolTemplateId 协议模板 ID。
     * @param moduleTemplate 模块模板对象。
     */
    private fun ensureModuleTemplateMatchesProtocol(
        protocolTemplateId: Long,
        moduleTemplate: ModuleTemplate,
    ) {
        if (moduleTemplate.protocolTemplate.id != protocolTemplateId) {
            throw ConflictException("Module template does not match target protocol template")
        }
    }

    /**
     * 校验设备下已有模块是否允许迁移到目标协议。
     *
     * @param deviceId 设备 ID。
     * @param targetProtocolTemplateId 目标协议模板 ID。
     */
    private fun ensureDeviceModulesMatchProtocol(
        deviceId: Long,
        targetProtocolTemplateId: Long,
    ) {
        val hasConflict = sql.exists(ModuleInstance::class) {
            where(table.device.id eq deviceId)
            where(table.moduleTemplate.protocolTemplate.id ne targetProtocolTemplateId)
        }
        if (hasConflict) {
            throw ConflictException("Existing modules do not match target protocol template")
        }
    }

    /**
     * 重排项目。
     *
     * @param projectId 项目 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun reorderProjects(projectId: Long, sortIndex: Int) {
        val orderedIds = reorderIds(queryIds("SELECT id FROM host_config_project ORDER BY sort_index ASC, id ASC"), projectId, sortIndex)
        batchUpdateSort("UPDATE host_config_project SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    /**
     * 移动协议。
     *
     * @param linkId 关联记录 ID。
     * @param protocolId 协议 ID。
     * @param currentProjectId 当前项目 ID。
     * @param targetProjectId 目标项目 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun moveProtocol(linkId: Long, protocolId: Long, currentProjectId: Long, targetProjectId: Long, sortIndex: Int) {
        if (currentProjectId == targetProjectId) {
            reorderProtocolLinks(currentProjectId, linkId, sortIndex)
            return
        }

        ensureProtocolNotLinked(targetProjectId, protocolId)
        val now = now()
        executeUpdate(
            "UPDATE host_config_project_protocol SET project_id = ?, updated_at = ? WHERE id = ?",
            targetProjectId,
            now,
            linkId,
        )
        normalizeProtocolLinks(currentProjectId)
        reorderProtocolLinks(targetProjectId, linkId, sortIndex)
    }

    /**
     * 移动模块。
     *
     * @param moduleId 模块 ID。
     * @param currentDeviceId 当前设备 ID。
     * @param targetDeviceId 目标设备 ID。
     * @param currentProtocolId 当前协议 ID。
     * @param targetProtocolId 目标协议 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun moveModule(
        moduleId: Long,
        currentDeviceId: Long,
        targetDeviceId: Long,
        currentProtocolId: Long,
        targetProtocolId: Long,
        sortIndex: Int,
    ) {
        if (currentDeviceId == targetDeviceId) {
            val orderedIds = reorderIds(
                queryIds("SELECT id FROM host_config_module_instance WHERE device_id = ? ORDER BY sort_index ASC, id ASC", currentDeviceId),
                moduleId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        val now = now()
        executeUpdate(
            "UPDATE host_config_module_instance SET device_id = ?, protocol_id = ?, updated_at = ? WHERE id = ?",
            targetDeviceId,
            targetProtocolId,
            now,
            moduleId,
        )
        val oldIds = queryIds(
            "SELECT id FROM host_config_module_instance WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
            currentDeviceId,
        )
        val newIds = reorderIds(
            queryIds("SELECT id FROM host_config_module_instance WHERE device_id = ? ORDER BY sort_index ASC, id ASC", targetDeviceId),
            moduleId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
        if (currentProtocolId != targetProtocolId) {
            normalizeProtocolModules(currentProtocolId)
            normalizeProtocolModules(targetProtocolId)
        }
    }

    /**
     * 移动设备。
     *
     * @param deviceId 设备 ID。
     * @param currentProtocolId 当前协议 ID。
     * @param targetProtocolId 目标协议 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun moveDevice(deviceId: Long, currentProtocolId: Long, targetProtocolId: Long, sortIndex: Int) {
        if (currentProtocolId == targetProtocolId) {
            val orderedIds = reorderIds(
                queryIds("SELECT id FROM host_config_device WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC", currentProtocolId),
                deviceId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        val now = now()
        executeUpdate(
            "UPDATE host_config_device SET protocol_id = ?, updated_at = ? WHERE id = ?",
            targetProtocolId,
            now,
            deviceId,
        )
        executeUpdate(
            "UPDATE host_config_module_instance SET protocol_id = ?, updated_at = ? WHERE device_id = ?",
            targetProtocolId,
            now,
            deviceId,
        )
        val oldIds = queryIds(
            "SELECT id FROM host_config_device WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC",
            currentProtocolId,
        )
        val newIds = reorderIds(
            queryIds("SELECT id FROM host_config_device WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC", targetProtocolId),
            deviceId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
        normalizeProtocolModules(currentProtocolId)
        normalizeProtocolModules(targetProtocolId)
    }

    /**
     * 确保协议尚未关联到项目。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     */
    private fun ensureProtocolNotLinked(projectId: Long, protocolId: Long) {
        val exists = sql.exists(ProjectProtocol::class) {
            where(table.project.id eq projectId)
            where(table.protocol.id eq protocolId)
        }
        if (exists) {
            throw ConflictException("Protocol already linked to project")
        }
    }

    /**
     * 重排项目内的协议关联顺序。
     *
     * @param projectId 项目 ID。
     * @param linkId 关联记录 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun reorderProtocolLinks(projectId: Long, linkId: Long, sortIndex: Int) {
        val orderedIds = reorderIds(
            queryIds("SELECT id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC", projectId),
            linkId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_project_protocol SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    /**
     * 规范化项目内的协议关联排序。
     *
     * @param projectId 项目 ID。
     */
    private fun normalizeProtocolLinks(projectId: Long) {
        val orderedIds = queryIds("SELECT id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC", projectId)
        batchUpdateSort("UPDATE host_config_project_protocol SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    /**
     * 规范化协议下模块排序。
     *
     * 设备成为模块父级后，这里只负责把同一协议内的模块排序收敛到稳定值，
     * 便于聚合导出和项目级列表保持可预测顺序。
     *
     * @param protocolId 协议 ID。
     */
    private fun normalizeProtocolModules(protocolId: Long) {
        val orderedIds = queryIds(
            "SELECT id FROM host_config_module_instance WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC",
            protocolId,
        )
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    /**
     * 删除未被项目关联的孤立协议。
     *
     * @param protocolIds 协议 ID 列表。
     */
    private fun deleteOrphanProtocols(protocolIds: Collection<Long>) {
        protocolIds.toSet().forEach { protocolId ->
            val linkedCount = queryCount(
                "SELECT COUNT(*) FROM host_config_project_protocol WHERE protocol_id = ?",
                protocolId,
            )
            if (linkedCount == 0L) {
                executeUpdate("DELETE FROM host_config_protocol_instance WHERE id = ?", protocolId)
            }
        }
    }

    private fun executeUpdate(
        sql: String,
        vararg args: Any?,
    ): Int = jdbc.withTransaction { _ ->
        jdbc.executeUpdate(sql, *args)
    }

    private fun queryCount(
        sql: String,
        vararg args: Any?,
    ): Long = jdbc.withTransaction { _ ->
        jdbc.queryCount(sql, *args)
    }

    /**
     * 查询 ID 列表。
     *
     * @param sql SQL 语句。
     * @param args SQL 参数列表。
     */
    private fun queryIds(sql: String, vararg args: Any): MutableList<Long> =
        jdbc.withTransaction { _ ->
            jdbc.queryIds(sql, *args).toMutableList()
        }

    private fun <T> query(
        sql: String,
        vararg args: Any?,
        mapper: (ResultSet) -> T,
    ): List<T> = jdbc.withTransaction { _ ->
        jdbc.query(sql, *args, mapper = mapper)
    }

    /**
     * 按目标位置重排 ID 列表。
     *
     * @param ids ID 列表。
     * @param movedId 需要移动的 ID。
     * @param targetIndex 目标位置索引。
     */
    private fun reorderIds(ids: MutableList<Long>, movedId: Long, targetIndex: Int): MutableList<Long> {
        ids.remove(movedId)
        val normalizedIndex = targetIndex.coerceIn(0, ids.size)
        ids.add(normalizedIndex, movedId)
        return ids
    }

    /**
     * 批量更新排序字段。
     *
     * @param sql SQL 语句。
     * @param orderedIds 排序后的 ID 列表。
     */
    private fun batchUpdateSort(sql: String, orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) {
            return
        }
        val updatedAt = now()
        jdbc.withTransaction {
            jdbc.batchUpdate(
                sql,
                orderedIds.mapIndexed { index, id ->
                    listOf(index, updatedAt, id)
                },
            )
        }
    }

    /**
     * 处理string。
     */
    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    /**
     * 获取当前时间戳。
     */
    private fun now(): Long = System.currentTimeMillis()
}
