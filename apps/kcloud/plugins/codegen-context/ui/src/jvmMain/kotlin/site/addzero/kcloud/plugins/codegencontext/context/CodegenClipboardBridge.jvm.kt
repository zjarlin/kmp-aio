package site.addzero.kcloud.plugins.codegencontext.context

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

internal actual object CodegenClipboardBridge {
    /**
     * 读取text。
     */
    actual fun readText(): String? {
        return runCatching {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                clipboard.getData(DataFlavor.stringFlavor) as? String
            } else {
                null
            }
        }.getOrNull()
    }
}
