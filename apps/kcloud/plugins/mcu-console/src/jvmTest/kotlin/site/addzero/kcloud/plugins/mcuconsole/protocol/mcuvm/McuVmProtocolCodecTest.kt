package site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest

class McuVmProtocolCodecTest {
    private val codec = McuVmProtocolCodec(
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    )

    @Test
    fun `encodes execute frame with newline`() {
        val encoded = codec.encode(
            codec.buildExecuteFrame(
                requestId = "req-1",
                request = McuScriptExecuteRequest(script = "gpio_set(1,true);"),
            ),
        )

        assertTrue(encoded.endsWith("\n"))
        assertTrue(encoded.contains("\"command\":\"vm.execute\""))
    }

    @Test
    fun `extracts frames and remainder`() {
        val decoded = codec.extractFrames(
            """{"requestId":"a","type":"ack","success":true}""" + "\n" +
                """{"requestId":"b","type":"log","success":true}""" + "\npart",
        )

        assertEquals(2, decoded.frames.size)
        assertEquals("part", decoded.remainder)
    }

    @Test
    fun `decodes incoming frame`() {
        val frame = codec.decodeOrNull(
            """{"requestId":"abc","type":"status","success":true,"payload":{"state":"RUNNING"}}""",
        )

        assertNotNull(frame)
        assertEquals("abc", frame.requestId)
        assertEquals("status", frame.type)
    }
}
