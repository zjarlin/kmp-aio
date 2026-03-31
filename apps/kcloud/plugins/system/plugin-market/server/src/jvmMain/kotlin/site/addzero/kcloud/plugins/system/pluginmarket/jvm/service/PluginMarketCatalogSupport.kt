package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentArtifact
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentJob
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginImportRecord
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPackage
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPresetBinding
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginSourceFile
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.by
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Single
class PluginMarketCatalogSupport(
    val sqlClient: KSqlClient,
) {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun hashContent(content: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    fun packageRef(id: String): PluginPackage = new(PluginPackage::class).by { this.id = id }

    fun jobRef(id: String): PluginDeploymentJob = new(PluginDeploymentJob::class).by { this.id = id }

    fun packageOrThrow(id: String): PluginPackage {
        return sqlClient.findById(PluginPackage::class, id)
            ?: throw NoSuchElementException("插件包不存在: $id")
    }

    fun fileOrThrow(id: String): PluginSourceFile {
        return sqlClient.findById(PluginSourceFile::class, id)
            ?: throw NoSuchElementException("插件源码文件不存在: $id")
    }

    fun jobOrThrow(id: String): PluginDeploymentJob {
        return sqlClient.findById(PluginDeploymentJob::class, id)
            ?: throw NoSuchElementException("插件部署任务不存在: $id")
    }

    fun listPackages(): List<PluginPackage> {
        return sqlClient.createQuery(PluginPackage::class) { select(table) }
            .execute()
            .sortedWith(compareBy<PluginPackage> { it.pluginGroup.orEmpty() }.thenBy { it.pluginId })
    }

    fun listFiles(packageId: String? = null): List<PluginSourceFile> {
        return sqlClient.createQuery(PluginSourceFile::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedWith(compareBy<PluginSourceFile> { it.orderIndex }.thenBy { it.relativePath })
    }

    fun listPresetBindings(packageId: String? = null): List<PluginPresetBinding> {
        return sqlClient.createQuery(PluginPresetBinding::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.appliedAt }
    }

    fun listJobs(packageId: String? = null): List<PluginDeploymentJob> {
        return sqlClient.createQuery(PluginDeploymentJob::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.updatedAt }
    }

    fun listArtifacts(jobId: String? = null, packageId: String? = null): List<PluginDeploymentArtifact> {
        val jobsById = listJobs(packageId).associateBy { it.id }
        return sqlClient.createQuery(PluginDeploymentArtifact::class) { select(table) }
            .execute()
            .filter { artifact -> jobId == null || artifact.deploymentJob.id == jobId }
            .filter { artifact -> packageId == null || artifact.deploymentJob.id in jobsById }
            .sortedByDescending { it.createdAt }
    }

    fun listImportRecords(packageId: String? = null): List<PluginImportRecord> {
        return sqlClient.createQuery(PluginImportRecord::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.importedAt }
    }

    fun deletePackageCascade(packageId: String) {
        listArtifacts(packageId = packageId).forEach { sqlClient.deleteById(PluginDeploymentArtifact::class, it.id) }
        listJobs(packageId).forEach { sqlClient.deleteById(PluginDeploymentJob::class, it.id) }
        listImportRecords(packageId).forEach { sqlClient.deleteById(PluginImportRecord::class, it.id) }
        listPresetBindings(packageId).forEach { sqlClient.deleteById(PluginPresetBinding::class, it.id) }
        listFiles(packageId).forEach { sqlClient.deleteById(PluginSourceFile::class, it.id) }
        sqlClient.deleteById(PluginPackage::class, packageId)
    }
}
