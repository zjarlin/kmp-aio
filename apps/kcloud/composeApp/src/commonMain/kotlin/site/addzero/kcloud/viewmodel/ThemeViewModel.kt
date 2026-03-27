package site.addzero.kcloud.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}
/**
 *
 * @author ForteScarlet
 */
class ThemeViewModel : ViewModel(){
   var themeMode by mutableStateOf(ThemeMode.DARK)

}
