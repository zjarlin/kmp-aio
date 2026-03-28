package site.addzero.kcpi18ndemo

import kotlin.test.Test
import kotlin.test.assertEquals

class KcpI18nDemoJvmTest {

    @Test
    fun `i18n plugin rewrites compose module literals`() {
        val state = DemoTextState()

        assertEquals(expectedTitle(), state.titleText())
        assertEquals(expectedBody(), state.bodyText())
        assertEquals(expectedButton(), state.buttonText())
        assertEquals(expectedIdleStatus(), state.statusText())

        state.recordClick()

        assertEquals(expectedClickedStatus(), state.statusText())
    }

    private fun expectedTitle(): String = charArrayOf(
        'H', 'e', 'l', 'l', 'o', ',', ' ', 'K', 'C', 'P',
    ).concatToString()

    private fun expectedBody(): String = charArrayOf(
        'T', 'h', 'e', ' ', 'C', 'o', 'm', 'p', 'o', 's', 'e', ' ',
        'm', 'o', 'd', 'u', 'l', 'e', ' ', 'i', 's', ' ', 'u', 's', 'i', 'n', 'g', ' ',
        't', 'h', 'e', ' ', 'i', '1', '8', 'n', ' ', 'c', 'o', 'm', 'p', 'i', 'l', 'e', 'r', ' ',
        'p', 'l', 'u', 'g', 'i', 'n', '.',
    ).concatToString()

    private fun expectedButton(): String = charArrayOf(
        'C', 'o', 'u', 'n', 't', ' ', 'C', 'l', 'i', 'c', 'k', 's',
    ).concatToString()

    private fun expectedIdleStatus(): String = charArrayOf(
        'N', 'o', ' ', 'c', 'l', 'i', 'c', 'k', 's', ' ', 'y', 'e', 't', '.',
    ).concatToString()

    private fun expectedClickedStatus(): String = charArrayOf(
        'C', 'l', 'i', 'c', 'k', 'e', 'd', ' ', '1', ' ', 't', 'i', 'm', 'e', '(', 's', ')', '.',
    ).concatToString()
}
