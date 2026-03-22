package site.addzero.remotecompose

import site.addzero.remotecompose.client.RemoteComposeLocalePreferences
import site.addzero.remotecompose.shared.RemoteComposeLocale
import java.util.prefs.Preferences

private const val LOCALE_PREFERENCE_KEY = "site.addzero.remotecompose.locale"

class JvmRemoteComposeLocalePreferences : RemoteComposeLocalePreferences {
    private val preferences = Preferences.userNodeForPackage(JvmRemoteComposeLocalePreferences::class.java)

    override fun read(): RemoteComposeLocale {
        return RemoteComposeLocale.fromCode(
            preferences.get(LOCALE_PREFERENCE_KEY, RemoteComposeLocale.ZH_CN.code)
        )
    }

    override fun write(locale: RemoteComposeLocale) {
        preferences.put(LOCALE_PREFERENCE_KEY, locale.code)
    }
}
