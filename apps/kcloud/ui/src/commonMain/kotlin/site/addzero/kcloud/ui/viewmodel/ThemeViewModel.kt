package site.addzero.kcloud.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

class ThemeViewModel : ViewModel() {
    var themeMode by mutableStateOf(ThemeMode.DARK)
}
