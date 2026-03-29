package site.addzero.kcpi18ndemo

import site.addzero.util.I8nutil
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KcpI18nDemoJvmTest {

    @BeforeTest
    fun resetLocale() {
        I8nutil.clearLocale()
    }

    @Test
    fun `i18n plugin rewrites compose module literals and switches locale at runtime`() {
        val state = DemoTextState()

        I8nutil.setLocale("zh")
        assertEquals("你好，KCP", state.titleText())
        assertEquals("Compose 模块已经接入国际化编译插件。", state.bodyText())
        assertEquals("点我切换计数", state.buttonText())
        assertEquals("当前还没有点击按钮。", state.statusText())

        I8nutil.setLocale("en")
        assertEquals(expectedTitle(), state.titleText())
        assertEquals(expectedBody(), state.bodyText())
        assertEquals(expectedButton(), state.buttonText())
        assertEquals(expectedIdleStatus(), state.statusText())

        I8nutil.setLocale("ja")
        assertEquals(expectedJapaneseTitle(), state.titleText())
        assertEquals(expectedJapaneseBody(), state.bodyText())
        assertEquals(expectedJapaneseButton(), state.buttonText())
        assertEquals(expectedJapaneseIdleStatus(), state.statusText())

        state.recordClick()

        I8nutil.setLocale("en")
        assertEquals(expectedClickedStatus(), state.statusText())

        I8nutil.setLocale("ja")
        assertEquals(expectedJapaneseClickedStatus(), state.statusText())
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

    private fun expectedJapaneseTitle(): String = charArrayOf(
        'こ', 'ん', 'に', 'ち', 'は', '、', 'K', 'C', 'P',
    ).concatToString()

    private fun expectedJapaneseBody(): String = charArrayOf(
        'C', 'o', 'm', 'p', 'o', 's', 'e', ' ', 'モ', 'ジ', 'ュ', 'ー', 'ル', 'は', '国', '際',
        '化', 'コ', 'ン', 'パ', 'イ', 'ラ', 'プ', 'ラ', 'グ', 'イ', 'ン', 'を', '使', '用', '中', 'で', 'す', '。',
    ).concatToString()

    private fun expectedJapaneseButton(): String = charArrayOf(
        'ク', 'リ', 'ッ', 'ク', '回', '数', 'を', '切', 'り', '替', 'え', 'る',
    ).concatToString()

    private fun expectedJapaneseIdleStatus(): String = charArrayOf(
        'ま', 'だ', 'ボ', 'タ', 'ン', 'は', '押', 'さ', 'れ', 'て', 'い', 'ま', 'せ', 'ん', '。',
    ).concatToString()

    private fun expectedJapaneseClickedStatus(): String = charArrayOf(
        'ク', 'リ', 'ッ', 'ク', '済', 'み', ' ', '1', '回', '。',
    ).concatToString()
}
