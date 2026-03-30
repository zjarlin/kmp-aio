package site.addzero.kcloud.plugins.mcuconsole.api.external

expect object McuConsoleApiClient {
    fun configureBaseUrl(value: String)
    val sessionApi: McuSessionApi
    val settingsApi: McuSettingsApi
    val scriptApi: McuScriptApi
    val flashApi: McuFlashApi
    val runtimeApi: McuRuntimeApi
    val modbusApi: McuModbusApi
    val transportApi: McuTransportApi
}
