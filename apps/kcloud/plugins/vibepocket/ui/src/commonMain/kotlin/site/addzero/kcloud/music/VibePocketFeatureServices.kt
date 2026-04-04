package site.addzero.kcloud.music

import org.koin.core.annotation.Single
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.api.suno.SunoTrack
import site.addzero.kcloud.vibepocket.model.ConfigRuntimeInfo
import site.addzero.kcloud.vibepocket.model.FavoriteItem
import site.addzero.kcloud.vibepocket.model.FavoriteRequest
import site.addzero.kcloud.vibepocket.model.MusicHistoryItem
import site.addzero.kcloud.vibepocket.model.SunoTaskResourceItem

data class CreativeAssetsRefreshResult(
    val detail: SunoTaskDetail,
    val archivedItem: SunoTaskResourceItem?,
    val archiveStatus: String,
)

interface CreativeAssetsService {
    suspend fun loadTaskResources(
        onPartialRefresh: (List<SunoTaskResourceItem>) -> Unit,
    ): List<SunoTaskResourceItem>

    suspend fun refreshTaskSnapshotById(
        taskId: String,
        fallbackType: String,
        requestJson: String?,
    ): CreativeAssetsRefreshResult

    fun errorMessage(error: Throwable): String
}

@Single(
    binds = [
        CreativeAssetsService::class,
    ],
)
class DefaultCreativeAssetsService : CreativeAssetsService {
    override suspend fun loadTaskResources(
        onPartialRefresh: (List<SunoTaskResourceItem>) -> Unit,
    ): List<SunoTaskResourceItem> {
        val loaded = ServerApiClient.sunoTaskResourceApi.list()
            .map { response -> response.toTaskResourceItem() }
        val runtimeConfig = SunoWorkflowService.loadConfig()
        return if (runtimeConfig.hasToken) {
            loaded.reconcileTaskResourcesWithSuno(onPartialRefresh)
        } else {
            loaded
        }
    }

    override suspend fun refreshTaskSnapshotById(
        taskId: String,
        fallbackType: String,
        requestJson: String?,
    ): CreativeAssetsRefreshResult {
        val refreshedSnapshot = refreshSunoTaskSnapshotById(
            taskId = taskId,
            fallbackType = fallbackType,
            requestJson = requestJson,
        )
        return CreativeAssetsRefreshResult(
            detail = refreshedSnapshot.detail,
            archivedItem = refreshedSnapshot.archivedItem,
            archiveStatus = refreshedSnapshot.archiveStatus,
        )
    }

    override fun errorMessage(error: Throwable): String {
        return SunoWorkflowService.errorMessage(error)
    }
}

interface MusicHistoryService {
    suspend fun getHistory(): List<MusicHistoryItem>

    suspend fun getFavorites(): List<FavoriteItem>

    suspend fun addFavorite(
        trackId: String,
        track: SunoTrack,
        taskId: String,
    )

    suspend fun removeFavorite(
        trackId: String,
    )

    suspend fun getCreditsOrNull(): Int?
}

@Single(
    binds = [
        MusicHistoryService::class,
    ],
)
class DefaultMusicHistoryService : MusicHistoryService {
    override suspend fun getHistory(): List<MusicHistoryItem> {
        return ServerApiClient.historyApi.getHistory()
    }

    override suspend fun getFavorites(): List<FavoriteItem> {
        return ServerApiClient.favoriteApi.getFavorites()
    }

    override suspend fun addFavorite(
        trackId: String,
        track: SunoTrack,
        taskId: String,
    ) {
        ServerApiClient.favoriteApi.addFavorite(
            FavoriteRequest(
                trackId = trackId,
                taskId = taskId,
                audioUrl = track.audioUrl,
                title = track.title,
                tags = track.tags,
                imageUrl = track.imageUrl,
                duration = track.duration,
            ),
        )
    }

    override suspend fun removeFavorite(
        trackId: String,
    ) {
        ServerApiClient.favoriteApi.removeFavorite(trackId)
    }

    override suspend fun getCreditsOrNull(): Int? {
        return SunoWorkflowService.getCreditsOrNull()
    }
}

interface MusicStudioService {
    suspend fun loadConfig(): SunoRuntimeConfig

    suspend fun getCreditsOrNull(): Int?
}

@Single(
    binds = [
        MusicStudioService::class,
    ],
)
class DefaultMusicStudioService : MusicStudioService {
    override suspend fun loadConfig(): SunoRuntimeConfig {
        return SunoWorkflowService.loadConfig()
    }

    override suspend fun getCreditsOrNull(): Int? {
        return SunoWorkflowService.getCreditsOrNull()
    }
}

interface SettingsService {
    suspend fun loadConfig(): SunoRuntimeConfig

    suspend fun saveConfig(
        apiToken: String,
        baseUrl: String,
        callbackUrl: String,
    )

    suspend fun getRuntimeInfo(): ConfigRuntimeInfo?
}

@Single(
    binds = [
        SettingsService::class,
    ],
)
class DefaultSettingsService : SettingsService {
    override suspend fun loadConfig(): SunoRuntimeConfig {
        return SunoWorkflowService.loadConfig()
    }

    override suspend fun saveConfig(
        apiToken: String,
        baseUrl: String,
        callbackUrl: String,
    ) {
        SunoWorkflowService.saveConfig(
            apiToken = apiToken,
            baseUrl = baseUrl,
            callbackUrl = callbackUrl,
        )
    }

    override suspend fun getRuntimeInfo(): ConfigRuntimeInfo? {
        return runCatching { ServerApiClient.configApi.getRuntimeInfo() }.getOrNull()
    }
}

interface WelcomeSetupService {
    suspend fun completeSetup(
        apiToken: String,
        baseUrl: String,
        callbackUrl: String,
    )
}

@Single(
    binds = [
        WelcomeSetupService::class,
    ],
)
class DefaultWelcomeSetupService : WelcomeSetupService {
    override suspend fun completeSetup(
        apiToken: String,
        baseUrl: String,
        callbackUrl: String,
    ) {
        persistSunoRuntimeConfig(
            apiToken = apiToken,
            baseUrl = baseUrl,
            callbackUrl = callbackUrl,
        )
    }
}
