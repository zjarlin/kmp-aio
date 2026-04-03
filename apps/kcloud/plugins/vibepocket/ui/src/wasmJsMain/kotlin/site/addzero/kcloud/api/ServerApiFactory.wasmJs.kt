package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildConfigApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createConfigApi()
}

internal actual fun buildFavoriteApi(
    baseUrl: String,
    httpClient: HttpClient,
): FavoriteApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createFavoriteApi()
}

internal actual fun buildPersonaApi(
    baseUrl: String,
    httpClient: HttpClient,
): PersonaApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createPersonaApi()
}

internal actual fun buildHistoryApi(
    baseUrl: String,
    httpClient: HttpClient,
): HistoryApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createHistoryApi()
}

internal actual fun buildMusicSearchApi(
    baseUrl: String,
    httpClient: HttpClient,
): MusicSearchApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createMusicSearchApi()
}

internal actual fun buildSunoTaskResourceApi(
    baseUrl: String,
    httpClient: HttpClient,
): SunoTaskResourceApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createSunoTaskResourceApi()
}
