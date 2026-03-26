package site.addzero.coding.playground.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class LlvmCompileExecutionStepDto(
    val command: List<String>,
    val exitCode: Int,
    val stdoutText: String,
    val stderrText: String,
)

@Serializable
data class LlvmCompileExecutionResultDto(
    val job: LlvmCompileJobDto,
    val artifacts: List<LlvmCompileArtifactDto> = emptyList(),
    val steps: List<LlvmCompileExecutionStepDto> = emptyList(),
)
