package site.addzero.kcloud.plugins.mcuconsole.modbus

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse

@Single
class McuModbusRemoteService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    private val api: McuModbusBackendApi = Ktorfit.Builder()
        .baseUrl(DEFAULT_BASE_URL)
        .httpClient(httpClient)
        .build()
        .createMcuModbusBackendApi()

    suspend fun getDeviceInfo(): McuModbusDeviceInfoResponse {
        return api.getDeviceInfo()
    }

    suspend fun getPowerLights(): List<Boolean> {
        return (1..24).map { index ->
            api.getPowerLights()["light$index"]
                ?.jsonPrimitive
                ?.booleanOrNull
                ?: false
        }
    }

    suspend fun writeIndicatorLights(
        faultLightOn: Boolean,
        runLightOn: Boolean,
    ) {
        api.writeIndicatorLights(
            McuModbusIndicatorLightsRequest(
                faultLightOn = faultLightOn,
                runLightOn = runLightOn,
            )
        )
    }

    companion object {
        private const val DEFAULT_BASE_URL = "http://localhost:18080/"
    }
}

internal interface McuModbusBackendApi {
    @GET("/api/mcu/modbus/device-info")
    suspend fun getDeviceInfo(): McuModbusDeviceInfoResponse

    @GET("/api/mcu/modbus/power-lights")
    suspend fun getPowerLights(): JsonObject

    @POST("/api/mcu/modbus/indicator-lights")
    @Headers("Content-Type: application/json")
    suspend fun writeIndicatorLights(
        @Body request: McuModbusIndicatorLightsRequest,
    ): JsonObject
}

@Serializable
internal data class McuModbusIndicatorLightsRequest(
    val faultLightOn: Boolean,
    val runLightOn: Boolean,
)
