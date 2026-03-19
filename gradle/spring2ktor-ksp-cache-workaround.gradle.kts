import org.gradle.api.tasks.Sync

fun registerKspBackupSync(
    taskName: String,
    backupPath: String,
    generatedPath: String,
    kspTaskName: String,
    compileTaskName: String,
    processResourcesTaskName: String,
) {
    val syncTask = tasks.register<Sync>(taskName) {
        val backupDir = layout.buildDirectory.dir(backupPath)
        from(backupDir)
        into(layout.buildDirectory.dir(generatedPath))
        onlyIf { backupDir.get().asFile.exists() }
    }

    tasks.matching { it.name == kspTaskName }.configureEach {
        finalizedBy(syncTask)
    }
    tasks.matching { it.name == compileTaskName }.configureEach {
        dependsOn(syncTask)
    }
    tasks.matching { it.name == processResourcesTaskName }.configureEach {
        dependsOn(syncTask)
    }
}

registerKspBackupSync(
    taskName = "syncMainKspBackupsToGenerated",
    backupPath = "kspCaches/main/backups",
    generatedPath = "generated/ksp/main",
    kspTaskName = "kspKotlin",
    compileTaskName = "compileKotlin",
    processResourcesTaskName = "processResources",
)

registerKspBackupSync(
    taskName = "syncJvmKspBackupsToGenerated",
    backupPath = "kspCaches/jvm/jvmMain/backups",
    generatedPath = "generated/ksp/jvm/jvmMain",
    kspTaskName = "kspKotlinJvm",
    compileTaskName = "compileKotlinJvm",
    processResourcesTaskName = "jvmProcessResources",
)
