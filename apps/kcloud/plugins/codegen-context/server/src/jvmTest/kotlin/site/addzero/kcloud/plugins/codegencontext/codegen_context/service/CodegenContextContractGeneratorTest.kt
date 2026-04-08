package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

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
                assertContains(rendered.metadataPayload, "\"interfaceSimpleName\": \"DeviceApi\"")
                assertContains(rendered.metadataPayload, "\"interfaceSimpleName\": \"DeviceWriteApi\"")
                assertContains(rendered.metadataPayload, "\"transport\": \"rtu\"")
            } finally {
                deleteWorkspace(workspaceRoot)
            }
        }
    }
}
