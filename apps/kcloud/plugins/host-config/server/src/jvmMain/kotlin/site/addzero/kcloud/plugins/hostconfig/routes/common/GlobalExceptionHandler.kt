package site.addzero.kcloud.plugins.hostconfig.api.common

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(ex.status)
            .body(ApiErrorResponse(code = ex.status, msg = ex.message, data = ex.payload))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val fields = ex.bindingResult
            .allErrors
            .mapNotNull { error ->
                val fieldError = error as? FieldError ?: return@mapNotNull null
                mapOf(
                    "field" to fieldError.field,
                    "error" to (fieldError.defaultMessage ?: "Invalid value"),
                )
            }
        return ResponseEntity
            .status(400)
            .body(ApiErrorResponse(code = 400, msg = "Invalid request body", data = mapOf("fields" to fields)))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(409)
            .body(ApiErrorResponse(code = 409, msg = "Resource conflict"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(code = 500, msg = "Internal server error"))
    }
}
