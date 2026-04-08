package site.addzero.kcloud.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class JvmUiPersistenceKoinModule {
    @Single
    fun settings(): Settings {
        return PreferencesSettings.Factory()
            .create("site.addzero.kcloud.ui")
    }
}
