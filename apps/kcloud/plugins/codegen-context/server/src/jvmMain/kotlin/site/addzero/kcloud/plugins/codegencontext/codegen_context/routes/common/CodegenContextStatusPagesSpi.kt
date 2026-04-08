package site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.codegencontext.api.common.ApiErrorResponse
import site.addzero.starter.statuspages.spi.StatusPagesSpi

@Single
class CodegenContextStatusPagesSpi : StatusPagesSpi {
    override val order: Int = 110

    override fun StatusPagesConfig.configure(application: Application) {
        exception<ApiException> { call, cause ->
            val status = HttpStatusCode.fromValue(cause.status)
            call.respond(
                status,
                ApiErrorResponse(
                    code = cause.status,
                    msg = cause.message,
                    data = cause.payload?.toString(),
                ),
            )
            call.application.environment.log.warn(
                "Codegen-context API ${cause.status}: ${cause.message}",
                cause,
            )
        }
    }
}
