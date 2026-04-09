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
/**
 * 提供mcu烧录远程相关服务。
 *
 * @property httpClient http客户端。
 */
class McuFlashRemoteService(
    private val httpClient: HttpClient,
) {
    /**
     * 列出配置档。
     */
    suspend fun listProfiles(): List<McuFlashProfileSummary> {
        return httpClient.get("/api/mcu/flash/profiles")
            .body<McuFlashProfilesResponse>()
            .items
    }

    /**
     * 列出探针。
     */
    suspend fun listProbes(): List<McuFlashProbeSummary> {
        return httpClient.get("/api/mcu/flash/probes")
            .body<McuFlashProbesResponse>()
            .items
    }

    /**
     * 启动烧录任务。
     *
     * @param request 请求参数。
     */
    suspend fun startFlash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse {
        return httpClient.post("/api/mcu/flash/start") {
            setBody(request)
        }.body()
    }

    /**
     * 获取状态。
     */
    suspend fun getStatus(): McuFlashStatusResponse {
        return httpClient.get("/api/mcu/flash/status").body()
    }

    /**
     * 处理重置。
     *
     * @param request 请求参数。
     */
    suspend fun reset(
        request: McuFlashResetRequest,
    ): McuFlashStatusResponse {
        return httpClient.post("/api/mcu/flash/reset") {
            setBody(request)
        }.body()
    }
}
