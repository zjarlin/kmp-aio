package site.addzero.kcloud.plugins.hostconfig.screen

import java.awt.EventQueue
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class HostConfigProjectSqliteFilePicker {
    actual fun pickSqliteFile(
        onPicked: (String?) -> Unit,
    ) {
        EventQueue.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "选择工程 sqlite 文件"
                fileSelectionMode = JFileChooser.FILES_ONLY
                fileFilter = FileNameExtensionFilter(
                    "SQLite 工程文件 (*.sqlite, *.db, *.sqlite3)",
                    "sqlite",
                    "db",
                    "sqlite3",
                )
            }
            val selectedPath = if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.absolutePath
            } else {
                null
            }
            onPicked(selectedPath)
        }
    }
}
