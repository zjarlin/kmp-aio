package site.addzero.kcpi18ndemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single

@Single
class DemoTextState {
    private var clickedCount by mutableStateOf(0)

    fun titleText(): String = "你好，KCP"

    fun bodyText(): String = "Compose 模块已经接入国际化编译插件。"

    fun buttonText(): String = "点我切换计数"

    fun statusText(): String = if (clickedCount == 0) {
        "当前还没有点击按钮。"
    } else {
        "按钮已经点击 $clickedCount 次。"
    }

    fun recordClick() {
        clickedCount += 1
        println(statusText())
    }
}
