package site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.common

open class ApiException(
    val status: Int,
    override val message: String,
    val payload: Any? = null,
) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(404, message)

class ConflictException(message: String) : ApiException(409, message)

class BusinessValidationException(
    message: String,
    payload: Any? = null,
) : ApiException(422, message, payload)
