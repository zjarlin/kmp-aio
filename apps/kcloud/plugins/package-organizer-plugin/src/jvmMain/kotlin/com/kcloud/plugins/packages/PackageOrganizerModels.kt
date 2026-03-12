package com.kcloud.plugins.packages

import kotlinx.serialization.Serializable

@Serializable
data class PackageOrganizerSettings(
    val scanDirectory: String = "",
    val targetDirectory: String = ""
)

@Serializable
data class ManagedPackageItem(
    val name: String,
    val path: String,
    val category: String,
    val extension: String,
    val size: Long,
    val modifiedAt: Long
)

@Serializable
data class PackageOrganizeResult(
    val movedCount: Int = 0,
    val skippedCount: Int = 0,
    val errors: List<String> = emptyList()
)

interface PackageOrganizerService {
    fun loadSettings(): PackageOrganizerSettings
    fun saveSettings(settings: PackageOrganizerSettings): PackageOrganizerSettings
    fun scanPackages(): List<ManagedPackageItem>
    fun organizePackages(): PackageOrganizeResult
}
