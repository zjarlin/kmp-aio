package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
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
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.TagTreeNode
import site.addzero.kcloud.plugins.hostconfig.routes.common.ConflictException
import site.addzero.kcloud.plugins.hostconfig.routes.common.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
class ProjectService(
    private val sql: KSqlClient,
    private val jdbc: HostConfigJdbc,
) {

    private companion object {
        val SUPPORTED_PROTOCOL_NAMES = setOf("ModbusRTU", "ModbusTCP", "MQTT")
    }

    fun listProjects(): List<ProjectResponse> {
        return sql.createQuery(Project::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.projectScalar))
        }.execute()
            .map { it.toResponse() }
    }

    fun getProject(projectId: Long): ProjectResponse {
        return loadProject(projectId).toResponse()
    }

    fun getProjectTree(projectId: Long): ProjectTreeResponse {
        val project = sql.createQuery(Project::class) {
            where(table.id eq projectId)
            select(table.fetch(Fetchers.projectTree))
        }.execute().firstOrNull() ?: throw NotFoundException("Project not found")
        return project.toTreeResponse()
    }

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

    fun updateProjectPosition(projectId: Long, request: ProjectPositionUpdateRequest): ProjectResponse {
        ensureProjectExists(projectId)
        reorderProjects(projectId, request.sortIndex)
        return loadProject(projectId).toResponse()
    }

    fun deleteProject(projectId: Long) {
        ensureProjectExists(projectId)
        val protocolIds = queryIds(
            "SELECT protocol_id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC",
            projectId,
        )
        jdbc.update("DELETE FROM host_config_project WHERE id = ?", projectId)
        deleteOrphanProtocols(protocolIds)
    }

    fun createProtocol(projectId: Long, request: ProtocolCreateRequest): ProtocolResponse {
        ensureProjectExists(projectId)
        ensureProtocolTemplateExists(request.protocolTemplateId)
        val name = request.name.trim()
        ensureProtocolNameUnique(name, null)
        val now = now()
        val entity = ProtocolInstance {
            this.protocolTemplateId = request.protocolTemplateId
            this.name = name
            this.pollingIntervalMs = request.pollingIntervalMs
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

    fun linkProtocol(projectId: Long, request: LinkExistingProtocolRequest): ProtocolResponse {
        ensureProjectExists(projectId)
        ensureProtocolExists(request.protocolId)
        ensureProtocolNotLinked(projectId, request.protocolId)
        val link = createProjectProtocolLink(projectId, request.protocolId, request.sortIndex, now())
        reorderProtocolLinks(projectId, link.id, request.sortIndex)
        return loadProjectProtocol(projectId, request.protocolId).toResponse()
    }

    fun updateProtocol(protocolId: Long, request: ProtocolUpdateRequest): ProtocolResponse {
        val link = loadProjectProtocol(request.projectId, protocolId)
        ensureProtocolTemplateExists(request.protocolTemplateId)
        val name = request.name.trim()
        ensureProtocolNameUnique(name, protocolId)
        val entity = ProtocolInstance {
            id = protocolId
            protocolTemplateId = request.protocolTemplateId
            this.name = name
            this.pollingIntervalMs = request.pollingIntervalMs
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        reorderProtocolLinks(request.projectId, link.id, request.sortIndex)
        return loadProjectProtocol(request.projectId, protocolId).toResponse()
    }

    fun updateProtocolPosition(protocolId: Long, request: ProtocolPositionUpdateRequest): ProtocolResponse {
        ensureProjectExists(request.sourceProjectId)
        ensureProjectExists(request.targetProjectId)
        val link = loadProjectProtocol(request.sourceProjectId, protocolId)
        moveProtocol(link.id, protocolId, request.sourceProjectId, request.targetProjectId, request.sortIndex)
        return loadProjectProtocol(request.targetProjectId, protocolId).toResponse()
    }

    fun deleteProtocol(projectId: Long, protocolId: Long) {
        val link = loadProjectProtocol(projectId, protocolId)
        jdbc.update("DELETE FROM host_config_project_protocol WHERE id = ?", link.id)
        normalizeProtocolLinks(projectId)
        deleteOrphanProtocols(listOf(protocolId))
    }

    fun createModule(protocolId: Long, request: ModuleCreateRequest): ModuleResponse {
        ensureProtocolExists(protocolId)
        ensureModuleTemplateExists(request.moduleTemplateId)
        val name = request.name.trim()
        ensureModuleNameUnique(protocolId, name, null)
        val now = now()
        val entity = ModuleInstance {
            this.protocolId = protocolId
            this.moduleTemplateId = request.moduleTemplateId
            this.name = name
            this.portName = request.portName.cleanNullable()
            this.baudRate = request.baudRate
            this.dataBits = request.dataBits
            this.stopBits = request.stopBits
            this.parity = request.parity
            this.responseTimeoutMs = request.responseTimeoutMs
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val module = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadModule(module.id).toResponse()
    }

    fun createProjectModule(projectId: Long, request: ModuleCreateRequest): ModuleResponse {
        ensureProjectExists(projectId)
        val moduleTemplate = loadModuleTemplate(request.moduleTemplateId)
        val protocolId = resolveProjectProtocolId(projectId, moduleTemplate.protocolTemplate.id)
        return createModule(protocolId, request)
    }

    fun updateModule(moduleId: Long, request: ModuleUpdateRequest): ModuleResponse {
        val current = loadModule(moduleId)
        ensureModuleTemplateExists(request.moduleTemplateId)
        val name = request.name.trim()
        ensureModuleNameUnique(current.protocol.id, name, moduleId)
        val entity = ModuleInstance {
            id = moduleId
            protocolId = current.protocol.id
            moduleTemplateId = request.moduleTemplateId
            this.name = name
            this.portName = request.portName.cleanNullable()
            this.baudRate = request.baudRate
            this.dataBits = request.dataBits
            this.stopBits = request.stopBits
            this.parity = request.parity
            this.responseTimeoutMs = request.responseTimeoutMs
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        val module = sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return loadModule(module.id).toResponse()
    }

    fun updateModulePosition(moduleId: Long, request: ModulePositionUpdateRequest): ModuleResponse {
        val current = loadModule(moduleId)
        when {
            request.protocolId != null -> {
                val targetProtocolId = request.protocolId!!
                ensureProtocolExists(targetProtocolId)
                ensureModuleNameUnique(targetProtocolId, current.name, moduleId)
                moveModule(moduleId, current.protocol.id, targetProtocolId, request.sortIndex)
            }

            request.projectId != null -> {
                val targetProjectId = request.projectId!!
                ensureProjectExists(targetProjectId)
                val targetProtocolId = resolveProjectProtocolId(targetProjectId, current.moduleTemplate.protocolTemplate.id)
                ensureModuleNameUnique(targetProtocolId, current.name, moduleId)
                moveModuleInProjectTree(
                    moduleId = moduleId,
                    currentProtocolId = current.protocol.id,
                    targetProtocolId = targetProtocolId,
                    sourceProjectId = request.sourceProjectId ?: resolveProjectIdByProtocol(current.protocol.id),
                    targetProjectId = targetProjectId,
                    sortIndex = request.sortIndex,
                )
            }

            else -> throw ConflictException("Target protocol or project is required")
        }
        return loadModule(moduleId).toResponse()
    }

    fun deleteModule(moduleId: Long) {
        ensureModuleExists(moduleId)
        jdbc.update("DELETE FROM host_config_module_instance WHERE id = ?", moduleId)
    }

    fun createDevice(moduleId: Long, request: DeviceCreateRequest): DeviceResponse {
        ensureModuleExists(moduleId)
        ensureDeviceTypeExists(request.deviceTypeId)
        val name = request.name.trim()
        ensureDeviceNameUnique(moduleId, name, null)
        val now = now()
        val entity = Device {
            this.moduleId = moduleId
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

    fun updateDevice(deviceId: Long, request: DeviceUpdateRequest): DeviceResponse {
        val current = loadDevice(deviceId)
        ensureDeviceTypeExists(request.deviceTypeId)
        val name = request.name.trim()
        ensureDeviceNameUnique(current.module.id, name, deviceId)
        val entity = Device {
            id = deviceId
            moduleId = current.module.id
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

    fun updateDevicePosition(deviceId: Long, request: DevicePositionUpdateRequest): DeviceResponse {
        val current = loadDevice(deviceId)
        ensureModuleExists(request.moduleId)
        ensureDeviceNameUnique(request.moduleId, current.name, deviceId)
        moveDevice(deviceId, current.module.id, request.moduleId, request.sortIndex)
        return loadDevice(deviceId).toResponse()
    }

    fun deleteDevice(deviceId: Long) {
        ensureDeviceExists(deviceId)
        jdbc.update("DELETE FROM host_config_device WHERE id = ?", deviceId)
    }

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

    private fun ensureModuleNameUnique(protocolId: Long, name: String, excludeId: Long?) {
        val exists = sql.exists(ModuleInstance::class) {
            where(table.protocol.id eq protocolId)
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Module name already exists")
        }
    }

    private fun ensureDeviceNameUnique(moduleId: Long, name: String, excludeId: Long?) {
        val exists = sql.exists(Device::class) {
            where(table.module.id eq moduleId)
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Device name already exists")
        }
    }

    private fun ensureProjectExists(projectId: Long) {
        val exists = sql.exists(Project::class) {
            where(table.id eq projectId)
        }
        if (!exists) {
            throw NotFoundException("Project not found")
        }
    }

    private fun ensureProtocolExists(protocolId: Long) {
        val exists = sql.exists(ProtocolInstance::class) {
            where(table.id eq protocolId)
        }
        if (!exists) {
            throw NotFoundException("Protocol not found")
        }
    }

    private fun ensureModuleExists(moduleId: Long) {
        val exists = sql.exists(ModuleInstance::class) {
            where(table.id eq moduleId)
        }
        if (!exists) {
            throw NotFoundException("Module not found")
        }
    }

    private fun ensureDeviceExists(deviceId: Long) {
        val exists = sql.exists(Device::class) {
            where(table.id eq deviceId)
        }
        if (!exists) {
            throw NotFoundException("Device not found")
        }
    }

    private fun ensureProtocolTemplateExists(protocolTemplateId: Long) {
        val exists = sql.exists(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
        }
        if (!exists) {
            throw NotFoundException("Protocol template not found")
        }
    }

    private fun ensureModuleTemplateExists(moduleTemplateId: Long) {
        val exists = sql.exists(ModuleTemplate::class) {
            where(table.id eq moduleTemplateId)
        }
        if (!exists) {
            throw NotFoundException("Module template not found")
        }
    }

    private fun ensureDeviceTypeExists(deviceTypeId: Long) {
        val exists = sql.exists(DeviceType::class) {
            where(table.id eq deviceTypeId)
        }
        if (!exists) {
            throw NotFoundException("Device type not found")
        }
    }

    private fun loadProject(projectId: Long): Project {
        return sql.createQuery(Project::class) {
            where(table.id eq projectId)
            select(table.fetch(Fetchers.projectScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Project not found")
    }

    private fun loadProjectProtocol(projectId: Long, protocolId: Long): ProjectProtocol {
        return sql.createQuery(ProjectProtocol::class) {
            where(table.project.id eq projectId)
            where(table.protocol.id eq protocolId)
            select(table.fetch(Fetchers.projectProtocolScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Protocol relation not found")
    }

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

    private fun loadModule(moduleId: Long): ModuleInstance {
        return sql.createQuery(ModuleInstance::class) {
            where(table.id eq moduleId)
            select(table.fetch(Fetchers.moduleScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Module not found")
    }

    private fun loadModuleTemplate(moduleTemplateId: Long): ModuleTemplate {
        return sql.createQuery(ModuleTemplate::class) {
            where(table.id eq moduleTemplateId)
            select(table.fetch(Fetchers.moduleTemplate))
        }.execute().firstOrNull() ?: throw NotFoundException("Module template not found")
    }

    private fun loadDevice(deviceId: Long): Device {
        return sql.createQuery(Device::class) {
            where(table.id eq deviceId)
            select(table.fetch(Fetchers.deviceScalar))
        }.execute().firstOrNull() ?: throw NotFoundException("Device not found")
    }

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

    private fun ProjectProtocol.toResponse(): ProtocolResponse =
        ProtocolResponse(
            id = protocol.id,
            name = protocol.name,
            pollingIntervalMs = protocol.pollingIntervalMs,
            sortIndex = sortIndex,
            protocolTemplateId = protocol.protocolTemplate.id,
        )

    private fun ModuleInstance.toResponse(): ModuleResponse =
        ModuleResponse(
            id = id,
            name = name,
            protocolId = protocol.id,
            portName = portName,
            baudRate = baudRate,
            dataBits = dataBits,
            stopBits = stopBits,
            parity = parity,
            responseTimeoutMs = responseTimeoutMs,
            sortIndex = sortIndex,
            moduleTemplateId = moduleTemplate.id,
        )

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
            deviceTypeId = deviceType.id,
        )

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
                        modules = protocol.modules
                            .sortedWith(compareBy(ModuleInstance::sortIndex, ModuleInstance::id))
                            .map { it.toTreeNode() },
                    )
                },
            modules = protocolLinks
                .sortedWith(compareBy(ProjectProtocol::sortIndex, ProjectProtocol::id))
                .flatMap { link ->
                    link.protocol.modules
                        .sortedWith(compareBy(ModuleInstance::sortIndex, ModuleInstance::id))
                        .map { it.toTreeNode() }
                },
        )

    private fun ModuleInstance.toTreeNode(): ModuleTreeNode =
        ModuleTreeNode(
            id = id,
            name = name,
            protocolId = protocol.id,
            portName = portName,
            baudRate = baudRate,
            dataBits = dataBits,
            stopBits = stopBits,
            parity = parity,
            responseTimeoutMs = responseTimeoutMs,
            sortIndex = sortIndex,
            moduleTemplateId = moduleTemplate.id,
            moduleTemplateCode = moduleTemplate.code,
            moduleTemplateName = moduleTemplate.name,
            devices = devices
                .sortedWith(compareBy(Device::sortIndex, Device::id))
                .map { it.toTreeNode() },
        )

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
            deviceTypeId = deviceType.id,
            deviceTypeCode = deviceType.code,
            deviceTypeName = deviceType.name,
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

    private fun reorderProjects(projectId: Long, sortIndex: Int) {
        val orderedIds = reorderIds(queryIds("SELECT id FROM host_config_project ORDER BY sort_index ASC, id ASC"), projectId, sortIndex)
        batchUpdateSort("UPDATE host_config_project SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    private fun moveProtocol(linkId: Long, protocolId: Long, currentProjectId: Long, targetProjectId: Long, sortIndex: Int) {
        if (currentProjectId == targetProjectId) {
            reorderProtocolLinks(currentProjectId, linkId, sortIndex)
            return
        }

        ensureProtocolNotLinked(targetProjectId, protocolId)
        val now = now()
        jdbc.update(
            "UPDATE host_config_project_protocol SET project_id = ?, updated_at = ? WHERE id = ?",
            targetProjectId,
            now,
            linkId,
        )
        normalizeProtocolLinks(currentProjectId)
        reorderProtocolLinks(targetProjectId, linkId, sortIndex)
    }

    private fun moveModule(moduleId: Long, currentProtocolId: Long, targetProtocolId: Long, sortIndex: Int) {
        if (currentProtocolId == targetProtocolId) {
            val orderedIds = reorderIds(
                queryIds("SELECT id FROM module_instance WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC", currentProtocolId),
                moduleId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        val now = now()
        jdbc.update(
            "UPDATE host_config_module_instance SET protocol_id = ?, updated_at = ? WHERE id = ?",
            targetProtocolId,
            now,
            moduleId,
        )
        val oldIds = queryIds(
            "SELECT id FROM host_config_module_instance WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC",
            currentProtocolId,
        )
        val newIds = reorderIds(
            queryIds("SELECT id FROM host_config_module_instance WHERE protocol_id = ? ORDER BY sort_index ASC, id ASC", targetProtocolId),
            moduleId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
    }

    private fun moveModuleInProjectTree(
        moduleId: Long,
        currentProtocolId: Long,
        targetProtocolId: Long,
        sourceProjectId: Long,
        targetProjectId: Long,
        sortIndex: Int,
    ) {
        if (sourceProjectId == targetProjectId) {
            if (currentProtocolId != targetProtocolId) {
                jdbc.update(
                    "UPDATE host_config_module_instance SET protocol_id = ?, updated_at = ? WHERE id = ?",
                    targetProtocolId,
                    now(),
                    moduleId,
                )
            }
            val orderedIds = reorderIds(listProjectModuleIds(targetProjectId), moduleId, sortIndex)
            batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        val oldIds = listProjectModuleIds(sourceProjectId).filterNot { it == moduleId }.toMutableList()
        jdbc.update(
            "UPDATE host_config_module_instance SET protocol_id = ?, updated_at = ? WHERE id = ?",
            targetProtocolId,
            now(),
            moduleId,
        )
        val newIds = reorderIds(listProjectModuleIds(targetProjectId), moduleId, sortIndex)
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_module_instance SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
    }

    private fun moveDevice(deviceId: Long, currentModuleId: Long, targetModuleId: Long, sortIndex: Int) {
        if (currentModuleId == targetModuleId) {
            val orderedIds = reorderIds(
                queryIds("SELECT id FROM host_config_device WHERE module_id = ? ORDER BY sort_index ASC, id ASC", currentModuleId),
                deviceId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        val now = now()
        jdbc.update(
            "UPDATE host_config_device SET module_id = ?, updated_at = ? WHERE id = ?",
            targetModuleId,
            now,
            deviceId,
        )
        val oldIds = queryIds(
            "SELECT id FROM host_config_device WHERE module_id = ? ORDER BY sort_index ASC, id ASC",
            currentModuleId,
        )
        val newIds = reorderIds(
            queryIds("SELECT id FROM host_config_device WHERE module_id = ? ORDER BY sort_index ASC, id ASC", targetModuleId),
            deviceId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_device SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
    }

    private fun ensureProtocolNotLinked(projectId: Long, protocolId: Long) {
        val exists = sql.exists(ProjectProtocol::class) {
            where(table.project.id eq projectId)
            where(table.protocol.id eq protocolId)
        }
        if (exists) {
            throw ConflictException("Protocol already linked to project")
        }
    }

    private fun reorderProtocolLinks(projectId: Long, linkId: Long, sortIndex: Int) {
        val orderedIds = reorderIds(
            queryIds("SELECT id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC", projectId),
            linkId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_project_protocol SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    private fun normalizeProtocolLinks(projectId: Long) {
        val orderedIds = queryIds("SELECT id FROM host_config_project_protocol WHERE project_id = ? ORDER BY sort_index ASC, id ASC", projectId)
        batchUpdateSort("UPDATE host_config_project_protocol SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
    }

    private fun deleteOrphanProtocols(protocolIds: Collection<Long>) {
        protocolIds.toSet().forEach { protocolId ->
            val linkedCount = jdbc.queryCount(
                "SELECT COUNT(*) FROM host_config_project_protocol WHERE protocol_id = ?",
                protocolId,
            )
            if (linkedCount == 0L) {
                jdbc.update("DELETE FROM host_config_protocol_instance WHERE id = ?", protocolId)
            }
        }
    }

    private fun queryIds(sql: String, vararg args: Any): MutableList<Long> =
        jdbc.queryIds(sql, *args)

    private fun listProjectModuleIds(projectId: Long): MutableList<Long> =
        queryIds(
            """
            SELECT DISTINCT m.id
            FROM host_config_module_instance m
            JOIN host_config_project_protocol pp ON pp.protocol_id = m.protocol_id
            WHERE pp.project_id = ?
            ORDER BY m.sort_index ASC, m.id ASC
            """.trimIndent(),
            projectId,
        )

    private fun resolveProjectProtocolId(projectId: Long, protocolTemplateId: Long): Long {
        return jdbc.queryList(
            """
            SELECT pp.protocol_id
            FROM host_config_project_protocol pp
            JOIN host_config_protocol_instance pi ON pi.id = pp.protocol_id
            WHERE pp.project_id = ? AND pi.protocol_template_id = ?
            ORDER BY pp.sort_index ASC, pp.id ASC
            LIMIT 1
            """.trimIndent(),
            projectId,
            protocolTemplateId,
        ) { rs -> rs.getLong(1) }.firstOrNull()
            ?: throw ConflictException("Please link a matching protocol before creating or moving the module")
    }

    private fun resolveProjectIdByProtocol(protocolId: Long): Long {
        return jdbc.queryList(
            """
            SELECT project_id
            FROM host_config_project_protocol
            WHERE protocol_id = ?
            ORDER BY sort_index ASC, id ASC
            LIMIT 1
            """.trimIndent(),
            protocolId,
        ) { rs -> rs.getLong(1) }.firstOrNull() ?: throw NotFoundException("Protocol relation not found")
    }

    private fun reorderIds(ids: MutableList<Long>, movedId: Long, targetIndex: Int): MutableList<Long> {
        ids.remove(movedId)
        val normalizedIndex = targetIndex.coerceIn(0, ids.size)
        ids.add(normalizedIndex, movedId)
        return ids
    }

    private fun batchUpdateSort(sql: String, orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) {
            return
        }
        jdbc.batchUpdateSort(
            sql = sql,
            orderedIds = orderedIds,
            updatedAt = now(),
        )
    }

    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    private fun now(): Long = System.currentTimeMillis()
}
