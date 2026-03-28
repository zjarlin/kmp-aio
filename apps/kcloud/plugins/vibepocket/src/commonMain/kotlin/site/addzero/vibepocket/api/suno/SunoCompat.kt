package site.addzero.vibepocket.api.suno

typealias SunoApiClient = site.addzero.kcloud.api.suno.SunoApiClient
typealias SunoBoostStyleData = site.addzero.kcloud.api.suno.SunoBoostStyleData
typealias SunoBoostStyleRequest = site.addzero.kcloud.api.suno.SunoBoostStyleRequest
typealias SunoExtendRequest = site.addzero.kcloud.api.suno.SunoExtendRequest
typealias SunoGeneratePersonaRequest = site.addzero.kcloud.api.suno.SunoGeneratePersonaRequest
typealias SunoGenerateRequest = site.addzero.kcloud.api.suno.SunoGenerateRequest
typealias SunoLyricsRequest = site.addzero.kcloud.api.suno.SunoLyricsRequest
typealias SunoLyricItem = site.addzero.kcloud.api.suno.SunoLyricItem
typealias SunoMusicCoverRequest = site.addzero.kcloud.api.suno.SunoMusicCoverRequest
typealias SunoPersonaData = site.addzero.kcloud.api.suno.SunoPersonaData
typealias SunoReplaceSectionRequest = site.addzero.kcloud.api.suno.SunoReplaceSectionRequest
typealias SunoTaskDetail = site.addzero.kcloud.api.suno.SunoTaskDetail
typealias SunoTrack = site.addzero.kcloud.api.suno.SunoTrack
typealias SunoUploadedFileData = site.addzero.kcloud.api.suno.SunoUploadedFileData
typealias SunoUploadCoverRequest = site.addzero.kcloud.api.suno.SunoUploadCoverRequest
typealias SunoVocalRemovalRequest = site.addzero.kcloud.api.suno.SunoVocalRemovalRequest
typealias SunoWavRequest = site.addzero.kcloud.api.suno.SunoWavRequest

val SUNO_MODELS: List<String>
    get() = site.addzero.kcloud.api.suno.SUNO_MODELS

val VOCAL_GENDERS: List<Pair<String, String>>
    get() = site.addzero.kcloud.api.suno.VOCAL_GENDERS
