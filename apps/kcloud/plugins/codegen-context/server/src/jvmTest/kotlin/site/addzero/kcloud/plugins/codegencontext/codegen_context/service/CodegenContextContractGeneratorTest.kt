package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CodegenContextContractGeneratorTest {

    @Test
    fun shouldRenderStableReadAndWriteGoldenFiles() {
        CodegenContextTestFixture().use { fixture ->
            val workspaceRoot = createGeneratorWorkspace()
            try {
                val rendered = fixture.generator.render(
                    context =
                        baseContextRequest(
                            protocolTemplateId = 1L,
                            code = "CTX_GOLDEN",
                        ).copy(id = 99L),
                    workspaceRoot = workspaceRoot,
                )

                assertEquals(readGolden("device-api.golden.kt"), rendered.deviceApi)
                assertEquals(readGolden("device-write-api.golden.kt"), rendered.deviceWriteApi)
                assertEquals(
                    readGolden("board-snapshot-registers.golden.kt"),
                    rendered.registerDtos.getValue("BoardSnapshotRegisters.kt"),
                )
                assertContains(rendered.metadataPayload, "\"interfaceSimpleName\":\"DeviceApi\"")
                assertContains(rendered.metadataPayload, "\"interfaceSimpleName\":\"DeviceWriteApi\"")
                assertContains(rendered.metadataPayload, "\"transport\":\"rtu\"")
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }

    @Test
    fun shouldNotRenderApiModelBridgeForRegisters() {
        CodegenContextTestFixture().use { fixture ->
            val workspaceRoot = createGeneratorWorkspace()
            try {
                val rendered = fixture.generator.render(
                    context =
                        baseContextRequest(
                            protocolTemplateId = 1L,
                            code = "CTX_MODEL_DOC",
                        ).copy(id = 100L),
                    workspaceRoot = workspaceRoot,
                )

                val registerDto = rendered.registerDtos.getValue("BoardSnapshotRegisters.kt")
                assertContains(registerDto, " * 读取板卡快照寄存器。")
                assertFalse(registerDto.contains("fun toApiModel()"))
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}
