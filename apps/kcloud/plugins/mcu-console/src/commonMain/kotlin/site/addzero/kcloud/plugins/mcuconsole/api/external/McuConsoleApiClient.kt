package site.addzero.kcloud.plugins.mcuconsole.api.external

expect object McuConsoleApiClient {
    fun configureBaseUrl(value: String)
    val api: McuConsoleApi
}
