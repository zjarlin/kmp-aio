package site.addzero.kcpi18ndemo

import site.addzero.util.I8nutil

actual object DemoI18nLocaleBridge {
    actual fun applyLocale(locale: String) {
        I8nutil.setLocale(locale)
    }
}
