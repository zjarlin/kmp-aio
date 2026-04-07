package site.addzero.kcloud.plugins.mcuconsole.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbeSummary
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse

@Single
class McuFlashRemoteService(
    private val httpClient: HttpClient,
) {
    suspend fun listProfiles(): List<McuFlashProfileSummary> {
        return httpClient.get("/api/mcu/flash/profiles")
            .body<McuFlashProfilesResponse>()
            .items
    }

    suspend fun listProbes(): List<McuFlashProbeSummary> {
        return httpClient.get("/api/mcu/flash/probes")
            .body<McuFlashProbesResponse>()
            .items
    }

    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return httpClient.post("/api/mcu/flash/start") {
            setBody(request)
        }.body()
    }

    suspend fun getStatus(): McuFlashStatusResponse {
        return httpClient.get("/api/mcu/flash/status").body()
    }

    suspend fun reset(
        request: McuFlashResetRequest,
    ): McuFlashStatusResponse {
        return httpClient.post("/api/mcu/flash/reset") {
            setBody(request)
        }.body()
    }
}
