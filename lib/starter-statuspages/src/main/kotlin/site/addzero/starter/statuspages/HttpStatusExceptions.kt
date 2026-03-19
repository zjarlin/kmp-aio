package site.addzero.starter.statuspages

import io.ktor.http.HttpStatusCode

open class HttpStatusException(
    val status: HttpStatusCode,
    override val message: String,
) : RuntimeException(message)

class ServiceUnavailableHttpException(
    message: String,
) : HttpStatusException(HttpStatusCode.ServiceUnavailable, message)

class BadGatewayHttpException(
    message: String,
) : HttpStatusException(HttpStatusCode.BadGateway, message)
