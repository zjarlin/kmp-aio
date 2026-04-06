package site.addzero.cupertinodemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import org.koin.core.annotation.Single

enum class CupertinoDemoPage(
    val title: String,
    val caption: String,
) {
    Overview("Workbench", "顶部栏、侧边栏、内容工作区和状态区的纯 Cupertino 壳层"),
    Forms("Forms", "搜索、字段、设置项这些高频输入场景"),
    Controls("Controls", "按钮、分段控件、滑杆和图标交互"),
    Adaptive("Adaptive API", "Adaptive 组件固定以 Cupertino 外观输出，不再暴露 M3"),
}

@Single
class CupertinoDemoState {
    var selectedPage by mutableStateOf(CupertinoDemoPage.Overview)
        private set

    var darkMode by mutableStateOf(false)
        private set

    var sidebarCollapsed by mutableStateOf(false)
        private set

    var sidebarQuery by mutableStateOf("")
        private set

    var showAlert by mutableStateOf(false)
        private set

    var projectName by mutableStateOf("Compose Cupertino Demo")
        private set

    var searchText by mutableStateOf("")
        private set

    var notificationsEnabled by mutableStateOf(true)
        private set

    var favoriteEnabled by mutableStateOf(false)
        private set

    var selectedCoreSegment by mutableIntStateOf(0)
        private set

    var coreSliderValue by mutableFloatStateOf(0.42f)
        private set

    var adaptiveSwitchEnabled by mutableStateOf(true)
        private set

    var adaptiveCheckboxEnabled by mutableStateOf(true)
        private set

    var adaptiveTriState by mutableStateOf(ToggleableState.Indeterminate)
        private set

    var adaptiveSliderValue by mutableFloatStateOf(0.68f)
        private set

    var adaptiveIconSelected by mutableStateOf(false)
        private set

    fun selectPage(page: CupertinoDemoPage) {
        selectedPage = page
    }

    fun toggleDarkMode() {
        darkMode = !darkMode
    }

    fun toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed
    }

    fun updateSidebarQuery(value: String) {
        sidebarQuery = value
    }

    fun showAlert() {
        showAlert = true
    }

    fun dismissAlert() {
        showAlert = false
    }

    fun updateProjectName(value: String) {
        projectName = value
    }

    fun updateSearchText(value: String) {
        searchText = value
    }

    fun updateNotificationsEnabled(value: Boolean) {
        notificationsEnabled = value
    }

    fun updateFavoriteEnabled(value: Boolean) {
        favoriteEnabled = value
    }

    fun updateSelectedCoreSegment(index: Int) {
        selectedCoreSegment = index
    }

    fun updateCoreSliderValue(value: Float) {
        coreSliderValue = value
    }

    fun updateAdaptiveSwitchEnabled(value: Boolean) {
        adaptiveSwitchEnabled = value
    }

    fun updateAdaptiveCheckboxEnabled(value: Boolean) {
        adaptiveCheckboxEnabled = value
    }

    fun cycleAdaptiveTriState() {
        adaptiveTriState = when (adaptiveTriState) {
            ToggleableState.Off -> ToggleableState.Indeterminate
            ToggleableState.Indeterminate -> ToggleableState.On
            ToggleableState.On -> ToggleableState.Off
        }
    }

    fun updateAdaptiveSliderValue(value: Float) {
        adaptiveSliderValue = value
    }

    fun updateAdaptiveIconSelected(value: Boolean) {
        adaptiveIconSelected = value
    }
}
