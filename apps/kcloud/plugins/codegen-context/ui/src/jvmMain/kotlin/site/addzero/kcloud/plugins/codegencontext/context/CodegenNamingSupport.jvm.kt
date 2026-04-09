package site.addzero.kcloud.plugins.codegencontext.context

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType

internal actual object CodegenIdentifierTransliterator {
    actual fun romanize(source: String): String {
        if (source.isBlank()) {
            return ""
        }
        val format =
            HanyuPinyinOutputFormat().apply {
                caseType = HanyuPinyinCaseType.LOWERCASE
                toneType = HanyuPinyinToneType.WITHOUT_TONE
            }
        val builder = StringBuilder()
        source.forEach { char ->
            if (char.isWhitespace()) {
                builder.append(' ')
                return@forEach
            }
            if (char.code <= 127) {
                builder.append(char)
                return@forEach
            }
            val pinyin =
                runCatching {
                    PinyinHelper.toHanyuPinyinStringArray(char, format)?.firstOrNull()
                }.getOrNull()
            if (pinyin.isNullOrBlank()) {
                builder.append(char)
            } else {
                builder.append(pinyin)
                builder.append(' ')
            }
        }
        return builder.toString()
    }
}
