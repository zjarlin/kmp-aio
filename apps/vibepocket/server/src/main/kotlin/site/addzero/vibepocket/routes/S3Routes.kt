package site.addzero.vibepocket.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.*
import site.addzero.vibepocket.s3.S3Service
import java.io.InputStream
import java.util.*

/**
 * S3 存储桶相关 API
 */
@Bean
fun Route.s3Routes() {
    val s3Service by inject<S3Service>()

    route("/api/s3") {
        /**
         * 上传文件
         */
        post("/upload") {
            val multipart = call.receiveMultipart()
            var fileKey: String? = null
            var fileName: String? = null
            var contentType: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = part.originalFileName ?: "file"
                    contentType = part.contentType?.toString()
                    fileKey = "${UUID.randomUUID()}_$fileName"
                    
                    val inputStream: InputStream = part.streamProvider()
                    val contentLength = part.headers[HttpHeaders.ContentLength]?.toLong()
                    
                    if (contentLength != null) {
                        s3Service.upload(fileKey!!, inputStream, contentLength, contentType)
                    } else {
                        // 如果没有 content-length，先读取到字节数组（适用于小文件）
                        s3Service.upload(fileKey!!, inputStream.readBytes(), contentType)
                    }
                }
                part.dispose()
            }

            if (fileKey != null) {
                call.respond(S3UploadResponse(
                    key = fileKey!!,
                    url = s3Service.getUrl(fileKey!!)
                ))
            } else {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "No file uploaded"))
            }
        }

        /**
         * 获取文件 URL
         */
        get("/{key}") {
            val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Key is required"))
            call.respond(S3UrlResponse(url = s3Service.getUrl(key)))
        }

        /**
         * 删除文件
         */
        delete("/{key}") {
            val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Key is required"))
            s3Service.delete(key)
            call.respond(OkResponse())
        }

        /**
         * 列出文件 (可选前缀)
         */
        get("/list") {
            val prefix = call.request.queryParameters["prefix"]
            val objects = s3Service.list(prefix)
            call.respond(objects.map { 
                S3ObjectDto(
                    key = it.key(),
                    size = it.size(),
                    lastModified = it.lastModified().toString()
                )
            })
        }
    }
}
