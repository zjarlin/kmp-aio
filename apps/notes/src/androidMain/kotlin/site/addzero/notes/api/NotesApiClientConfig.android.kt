package site.addzero.notes.api

actual fun platformDefaultApiBaseUrl(): String {
    return configuredApiBaseUrl("http://10.0.2.2:18080/")
}
