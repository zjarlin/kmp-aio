package site.addzero.fluentdemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single

enum class FluentDemoLanguage {
    ZhCn,
    EnUs,
    ;

    fun toggled(): FluentDemoLanguage =
        when (this) {
            ZhCn -> EnUs
            EnUs -> ZhCn
        }
}

enum class FluentDemoPage {
    Overview,
    Forms,
    Commands,
    Dialogs,
    Settings,
}

@Single
class FluentDemoState {
    var selectedPage by mutableStateOf(FluentDemoPage.Overview)
        private set

    var darkMode by mutableStateOf(false)
        private set

    var navigationCompact by mutableStateOf(false)
        private set

    var language by mutableStateOf(FluentDemoLanguage.ZhCn)
        private set

    var noticeVisible by mutableStateOf(true)
        private set

    var dialogVisible by mutableStateOf(false)
        private set

    var workspaceName by mutableStateOf("Fluent Starter")
        private set

    var searchText by mutableStateOf("")
        private set

    var notificationsEnabled by mutableStateOf(true)
        private set

    var autoSyncEnabled by mutableStateOf(true)
        private set

    var lastAction by mutableStateOf("ready")
        private set

    fun selectPage(page: FluentDemoPage) {
        selectedPage = page
        lastAction = page.name
    }

    fun toggleDarkMode() {
        updateDarkMode(!darkMode)
    }

    fun updateDarkMode(value: Boolean) {
        darkMode = value
        lastAction = if (value) "dark" else "light"
    }

    fun toggleNavigationCompact() {
        updateNavigationCompact(!navigationCompact)
    }

    fun updateNavigationCompact(value: Boolean) {
        navigationCompact = value
        lastAction = if (value) "compact-nav" else "full-nav"
    }

    fun toggleLanguage() {
        language = language.toggled()
        lastAction = language.name
    }

    fun showNotice() {
        noticeVisible = true
        lastAction = "notice"
    }

    fun dismissNotice() {
        noticeVisible = false
        lastAction = "notice-dismissed"
    }

    fun openDialog() {
        dialogVisible = true
        lastAction = "dialog-open"
    }

    fun closeDialog() {
        dialogVisible = false
        lastAction = "dialog-close"
    }

    fun updateWorkspaceName(value: String) {
        workspaceName = value
        lastAction = "workspace-name"
    }

    fun updateSearchText(value: String) {
        searchText = value
        lastAction = "search"
    }

    fun updateNotificationsEnabled(value: Boolean) {
        notificationsEnabled = value
        lastAction = if (value) "notifications-on" else "notifications-off"
    }

    fun updateAutoSyncEnabled(value: Boolean) {
        autoSyncEnabled = value
        lastAction = if (value) "autosync-on" else "autosync-off"
    }
}
