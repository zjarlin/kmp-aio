package site.addzero.vibepocket

import org.koin.dsl.module
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel

val vibePocketPluginModule = module {
    factory { MusicStudioViewModel() }
    factory { CreativeAssetsViewModel() }
    factory { SettingsViewModel() }
}
