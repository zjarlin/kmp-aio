package site.addzero.notes.api

actual fun platformDefaultApiBaseUrl(): String {
    return configuredApiBaseUrl("http://127.0.0.1:18080/")
}
