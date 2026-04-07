package site.addzero.kcloud.plugins.hostconfig.routes.common

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.common.ApiErrorResponse
import site.addzero.kcloud.plugins.hostconfig.routes.common.ApiException
import site.addzero.starter.statuspages.spi.StatusPagesSpi

/**
 * Host-config-specific Ktor error mapping.
 *
 * This plugin runs inside Ktor, so Spring's `@RestControllerAdvice` is not part
 * of the request pipeline. We contribute StatusPages handlers instead.
 */
@Single
class HostConfigStatusPagesSpi : StatusPagesSpi {
    override val order: Int = 100

    override fun StatusPagesConfig.configure(application: Application) {
        exception<ApiException> { call, cause ->
            val status = HttpStatusCode.fromValue(cause.status)
            call.respond(
                status,
                ApiErrorResponse(
                    code = cause.status,
                    msg = cause.message,
                    data = cause.payload,
                ),
            )
            call.application.environment.log.warn(
                "Host-config API ${cause.status}: ${cause.message}",
                cause,
            )
        }
    }
}
