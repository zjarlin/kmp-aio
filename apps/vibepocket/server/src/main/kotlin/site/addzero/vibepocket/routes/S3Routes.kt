package site.addzero.vibepocket.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.cio.toByteArray
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.springktor.runtime.SpringRouteResult
import site.addzero.springktor.runtime.springBadRequest
import site.addzero.springktor.runtime.springOk
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.OkResponse
import site.addzero.vibepocket.dto.S3ObjectDto
import site.addzero.vibepocket.dto.S3UploadResponse
import site.addzero.vibepocket.dto.S3UrlResponse
import site.addzero.vibepocket.s3.S3Service
import java.util.*

/**
 * S3 存储桶相关 API
 */
@PostMapping("/api/s3/upload")
suspend fun uploadToS3(call: ApplicationCall): SpringRouteResult<Any> {
    val s3Service = s3Service()
    val multipart = call.receiveMultipart()
    var fileKey: String? = null
    var fileName: String? = null
    var contentType: String? = null

    multipart.forEachPart { part ->
        if (part is PartData.FileItem) {
            fileName = part.originalFileName ?: "file"
            contentType = part.contentType?.toString()
            val generatedKey = "${UUID.randomUUID()}_$fileName"
            fileKey = generatedKey

            val contentLength = part.headers[HttpHeaders.ContentLength]?.toLong()
            val bytes = part.provider().toByteArray()

            if (contentLength != null) {
                s3Service.upload(generatedKey, bytes.inputStream(), contentLength, contentType)
            } else {
                s3Service.upload(generatedKey, bytes, contentType)
            }
        }
        part.dispose()
    }

    val uploadedKey = fileKey
    if (uploadedKey != null) {
        return springOk(
            S3UploadResponse(
                key = uploadedKey,
                url = s3Service.getUrl(uploadedKey),
            ),
        )
    }

    return springBadRequest(ErrorResponse(400, "No file uploaded"))
}

@GetMapping("/api/s3/{key}")
suspend fun readS3FileUrl(
    @PathVariable key: String,
): S3UrlResponse {
    return S3UrlResponse(url = s3Service().getUrl(key))
}

@DeleteMapping("/api/s3/{key}")
suspend fun deleteS3File(
    @PathVariable key: String,
): OkResponse {
    s3Service().delete(key)
    return OkResponse()
}

@GetMapping("/api/s3/list")
suspend fun listS3Files(
    @RequestParam("prefix") prefix: String?,
): List<S3ObjectDto> {
    return s3Service().list(prefix).map {
        S3ObjectDto(
            key = it.key(),
            size = it.size(),
            lastModified = it.lastModified().toString(),
        )
    }
}

private fun s3Service(): S3Service {
    return KoinPlatform.getKoin().get()
}
