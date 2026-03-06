package site.addzero.notes.api

actual fun platformDefaultApiBaseUrl(): String {
    val fromEnv = System.getenv("NOTES_API_BASE_URL")?.trim().orEmpty()
    if (fromEnv.isNotBlank()) {
        return fromEnv
    }
    return "http://127.0.0.1:18080/"
}
