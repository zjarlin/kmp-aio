package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.LlvmCompileArtifact
import site.addzero.coding.playground.server.entity.LlvmCompileJob
import site.addzero.coding.playground.server.entity.LlvmCompileProfile
import site.addzero.coding.playground.server.entity.decodeStringList
import site.addzero.coding.playground.server.entity.decodeStringMap
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.LlvmCompileJobService
import site.addzero.coding.playground.shared.service.LlvmCompileProfileService
import site.addzero.coding.playground.shared.service.LlvmLlExportService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Single
class LlvmCompileProfileServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmCompileProfileService {
    override suspend fun create(request: CreateLlvmCompileProfileRequest): LlvmCompileProfileDto {
        support.moduleOrThrow(request.moduleId)
        requireText(request.name, "compile profile name")
        requireText(request.outputDirectory, "compile profile output directory")
        val now = support.now()
        val entity = new(LlvmCompileProfile::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            name = request.name
            targetPlatform = request.targetPlatform
            outputDirectory = request.outputDirectory
            optPath = request.optPath
            optArgsJson = encodeStringList(request.optArgs)
            llcPath = request.llcPath
            llcArgsJson = encodeStringList(request.llcArgs)
            clangPath = request.clangPath
            clangArgsJson = encodeStringList(request.clangArgs)
            environmentJson = encodeStringMap(request.environment)
            orderIndex = support.nextOrder(support.listCompileProfiles(request.moduleId))
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: LlvmSearchRequest): List<LlvmCompileProfileDto> {
        return support.listCompileProfiles(search.moduleId)
            .map { it.toDto() }
            .filter { search.matches(moduleId = it.moduleId, symbol = it.name, extras = listOf(it.targetPlatform, it.outputDirectory)) }
    }

    override suspend fun get(id: String): LlvmCompileProfileDto = support.compileProfileOrThrow(id).toDto()

    override suspend fun update(id: String, request: UpdateLlvmCompileProfileRequest): LlvmCompileProfileDto {
        val existing = support.compileProfileOrThrow(id)
        val entity = new(LlvmCompileProfile::class).by {
            this.id = id
            module = support.moduleRef(existing.moduleId)
            name = request.name
            targetPlatform = request.targetPlatform
            outputDirectory = request.outputDirectory
            optPath = request.optPath
            optArgsJson = encodeStringList(request.optArgs)
            llcPath = request.llcPath
            llcArgsJson = encodeStringList(request.llcArgs)
            clangPath = request.clangPath
            clangArgsJson = encodeStringList(request.clangArgs)
            environmentJson = encodeStringMap(request.environment)
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto {
        val blockers = support.listCompileJobs().filter { it.profileId == id }.map { "compile job '${it.id}' uses this profile" }
        return LlvmDeleteCheckResultDto(id, "compile-profile", blockers.isEmpty(), blockers, if (blockers.isEmpty()) null else "Delete blocked by ${blockers.size} jobs")
    }

    override suspend fun validate(id: String): List<LlvmValidationIssueDto> {
        val profile = support.compileProfileOrThrow(id)
        val issues = mutableListOf<LlvmValidationIssueDto>()
        if (profile.outputDirectory.isBlank()) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.ERROR, "compile-profile:${id}", "output directory is required")
        }
        listOf(profile.optPath, profile.llcPath, profile.clangPath).filterNotNull().forEach { executable ->
            if (!Paths.get(executable).exists()) {
                issues += LlvmValidationIssueDto(LlvmValidationSeverity.WARNING, "compile-profile:${id}", "tool '$executable' does not exist on disk")
            }
        }
        if (profile.optPath.isNullOrBlank() && profile.llcPath.isNullOrBlank() && profile.clangPath.isNullOrBlank()) {
            issues += LlvmValidationIssueDto(LlvmValidationSeverity.WARNING, "compile-profile:${id}", "profile exports .ll only because no tool executables are configured")
        }
        return issues
    }

    override suspend fun delete(id: String) {
        val check = deleteCheck(id)
        if (!check.deletable) throw PlaygroundValidationException(check.message ?: "LLVM compile profile cannot be deleted")
        support.sqlClient.deleteById(LlvmCompileProfile::class, id)
    }
}

@Single
class LlvmCompileJobServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val exportService: LlvmLlExportService,
) : LlvmCompileJobService {
    override suspend fun create(request: CreateLlvmCompileJobRequest): LlvmCompileJobDto {
        support.moduleOrThrow(request.moduleId)
        val profile = support.compileProfileOrThrow(request.profileId)
        val now = support.now()
        val entity = new(LlvmCompileJob::class).by {
            id = support.newId()
            module = support.moduleRef(request.moduleId)
            this.profile = support.compileProfileRef(request.profileId)
            status = LlvmCompileJobStatus.PENDING.name
            outputDirectory = profile.outputDirectory
            exportPath = null
            stdoutText = null
            stderrText = null
            exitCode = null
            finishedAt = null
            createdAt = now
            updatedAt = now
        }
        val created = support.sqlClient.save(entity).modifiedEntity.toDto()
        if (request.runNow) {
            execute(created.id)
            return get(created.id)
        }
        return created
    }

    override suspend fun list(search: LlvmSearchRequest): List<LlvmCompileJobDto> {
        return support.listCompileJobs(search.moduleId)
            .map { it.toDto() }
            .filter { search.matches(moduleId = it.moduleId, symbol = it.id, extras = listOf(it.outputDirectory, it.status.name)) }
    }

    override suspend fun get(id: String): LlvmCompileJobDto = support.compileJobOrThrow(id).toDto()

    override suspend fun execute(id: String): LlvmCompileExecutionResultDto {
        val job = support.compileJobOrThrow(id)
        val profile = support.compileProfileOrThrow(job.profileId)
        val profileEnvironment = decodeStringMap(profile.environmentJson)
        val module = support.moduleOrThrow(job.moduleId)
        val outputDir = Paths.get(profile.outputDirectory)
        outputDir.createDirectories()
        val exportPath = outputDir.resolve("${module.name}.ll")
        val steps = mutableListOf<LlvmCompileExecutionStepDto>()
        var status = LlvmCompileJobStatus.RUNNING
        var stdout = ""
        var stderr = ""
        var exitCode: Int? = null
        updateJob(job, status = status, outputDirectory = outputDir.toString(), exportPath = exportPath.toString(), stdout = stdout, stderr = stderr, exitCode = exitCode, finishedAt = null)
        val exported = exportService.exportModule(job.moduleId, exportPath.toString())
        saveArtifact(job.id, LlvmCompileArtifactKind.LLVM_IR, exportPath)
        var currentInput = exportPath

        fun runStep(
            executable: String?,
            args: List<String>,
            output: Path?,
        ): Int? {
            if (executable.isNullOrBlank()) return null
            val command = buildList {
                add(executable)
                addAll(args)
                if (output != null) {
                    add("-o")
                    add(output.toString())
                }
                add(currentInput.toString())
            }
            val process = ProcessBuilder(command)
                .directory(outputDir.toFile())
                .apply { environment().putAll(profileEnvironment) }
                .start()
            val stdoutText = process.inputStream.bufferedReader().readText()
            val stderrText = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            steps += LlvmCompileExecutionStepDto(command = command, exitCode = code, stdoutText = stdoutText, stderrText = stderrText)
            stdout += stdoutText
            stderr += stderrText
            exitCode = code
            return code
        }

        val optOutput = if (!profile.optPath.isNullOrBlank()) outputDir.resolve("${module.name}.opt.ll") else null
        runStep(profile.optPath, decodeStringList(profile.optArgsJson) + listOf("-S"), optOutput)?.let { code ->
            if (code != 0) status = LlvmCompileJobStatus.FAILED else if (optOutput != null) {
                currentInput = optOutput
                saveArtifact(job.id, LlvmCompileArtifactKind.OPT_IR, optOutput)
            }
        }

        if (status != LlvmCompileJobStatus.FAILED) {
            val llcOutput = if (!profile.llcPath.isNullOrBlank()) outputDir.resolve("${module.name}.o") else null
            runStep(profile.llcPath, decodeStringList(profile.llcArgsJson) + listOf("-filetype=obj"), llcOutput)?.let { code ->
                if (code != 0) status = LlvmCompileJobStatus.FAILED else if (llcOutput != null) {
                    currentInput = llcOutput
                    saveArtifact(job.id, LlvmCompileArtifactKind.OBJECT, llcOutput)
                }
            }
        }

        val clangPath = profile.clangPath
        if (status != LlvmCompileJobStatus.FAILED && !clangPath.isNullOrBlank()) {
            val binaryOutput = outputDir.resolve(module.name)
            val command = buildList {
                add(clangPath)
                addAll(decodeStringList(profile.clangArgsJson))
                add(currentInput.toString())
                add("-o")
                add(binaryOutput.toString())
            }
            val process = ProcessBuilder(command)
                .directory(outputDir.toFile())
                .apply { environment().putAll(profileEnvironment) }
                .start()
            val stdoutText = process.inputStream.bufferedReader().readText()
            val stderrText = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            steps += LlvmCompileExecutionStepDto(command = command, exitCode = code, stdoutText = stdoutText, stderrText = stderrText)
            stdout += stdoutText
            stderr += stderrText
            exitCode = code
            if (code != 0) {
                status = LlvmCompileJobStatus.FAILED
            } else {
                saveArtifact(job.id, LlvmCompileArtifactKind.BINARY, binaryOutput)
            }
        }

        if (status != LlvmCompileJobStatus.FAILED) {
            status = LlvmCompileJobStatus.SUCCEEDED
        }
        val finished = support.now()
        updateJob(job, status = status, outputDirectory = outputDir.toString(), exportPath = exported.outputPath, stdout = stdout, stderr = stderr, exitCode = exitCode, finishedAt = finished)
        return LlvmCompileExecutionResultDto(
            job = support.compileJobOrThrow(id).toDto(),
            artifacts = support.listCompileArtifacts(id).map { it.toDto() },
            steps = steps,
        )
    }

    override suspend fun delete(id: String) {
        support.inTransaction {
            support.listCompileArtifacts(id).forEach { support.sqlClient.deleteById(LlvmCompileArtifact::class, it.id) }
            support.sqlClient.deleteById(LlvmCompileJob::class, id)
        }
    }

    override suspend fun listArtifacts(jobId: String): List<LlvmCompileArtifactDto> {
        return support.listCompileArtifacts(jobId).map { it.toDto() }
    }

    private fun updateJob(
        existing: LlvmCompileJob,
        status: LlvmCompileJobStatus,
        outputDirectory: String,
        exportPath: String?,
        stdout: String?,
        stderr: String?,
        exitCode: Int?,
        finishedAt: java.time.LocalDateTime?,
    ) {
        val entity = new(LlvmCompileJob::class).by {
            id = existing.id
            module = support.moduleRef(existing.moduleId)
            this.profile = support.compileProfileRef(existing.profileId)
            this.status = status.name
            this.outputDirectory = outputDirectory
            this.exportPath = exportPath
            stdoutText = stdout
            stderrText = stderr
            this.exitCode = exitCode
            this.finishedAt = finishedAt
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        support.sqlClient.save(entity)
    }

    private fun saveArtifact(jobId: String, kind: LlvmCompileArtifactKind, path: Path): LlvmCompileArtifactDto {
        val now = support.now()
        val entity = new(LlvmCompileArtifact::class).by {
            id = support.newId()
            job = support.compileJobRef(jobId)
            this.kind = kind.name
            filePath = path.toString()
            sizeBytes = if (path.exists()) Files.size(path) else 0
            createdAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }
}
