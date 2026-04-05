package site.addzero.configcenter

fun ConfigCenterBeanFactory.Companion.jdbc(
    settings: ConfigCenterJdbcSettings,
): ConfigCenterBeanFactory {
    return ConfigCenterBeanFactory(
        configCenterValueService = JdbcConfigCenterValueService(settings),
    )
}

fun ConfigCenterBeanFactory.Companion.env(
    settings: ConfigCenterJdbcSettings,
    namespace: String,
    active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
): ConfigCenterEnv {
    return jdbc(settings).env(
        namespace = namespace,
        active = active,
    )
}

fun ConfigCenterBeanFactory.Companion.env(
    url: String,
    namespace: String,
    active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    username: String? = null,
    password: String? = null,
    driver: String? = ConfigCenterJdbcSettings.inferDriver(url),
    autoDdl: Boolean = true,
): ConfigCenterEnv {
    return env(
        settings = ConfigCenterJdbcSettings(
            url = url,
            username = username,
            password = password,
            driver = driver,
            autoDdl = autoDdl,
        ),
        namespace = namespace,
        active = active,
    )
}
