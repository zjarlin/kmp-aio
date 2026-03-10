package com.moveoff.storage

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.AbortMultipartUploadRequest
import aws.sdk.kotlin.services.s3.model.CompleteMultipartUploadRequest
import aws.sdk.kotlin.services.s3.model.CompletedPart
import aws.sdk.kotlin.services.s3.model.CreateMultipartUploadRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.HeadBucketRequest
import aws.sdk.kotlin.services.s3.model.HeadObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.UploadPartRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.toFlow
import aws.smithy.kotlin.runtime.net.url.Url
import com.moveoff.sync.api.DownloadResult
import com.moveoff.sync.api.RemoteObject
import com.moveoff.sync.api.StorageClient
import com.moveoff.sync.api.UploadResult
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.RandomAccessFile

/**
 * S3配置
 */
data class S3Config(
    val endpoint: String,           // 如: https://s3.amazonaws.com 或 http://localhost:9000
    val region: String,             // 如: us-east-1
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
    val prefix: String = "",        // 存储前缀（如: "moveoff/"）
    val forcePathStyle: Boolean = false  // MinIO需要设为true
)

/**
 * S3存储客户端实现
 */
class S3StorageClient(
    private val config: S3Config
) : StorageClient {

    private var s3Client: S3Client? = null

    /**
     * 初始化S3客户端
     */
    private fun getClient(): S3Client {
        if (s3Client == null) {
            s3Client = S3Client {
                region = config.region
                endpointUrl = Url.parse(config.endpoint)
                credentialsProvider = StaticCredentialsProvider {
                    accessKeyId = config.accessKey
                    secretAccessKey = config.secretKey
                }
                forcePathStyle = config.forcePathStyle
            }
        }
        return s3Client!!
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val client = getClient()
            client.headBucket(
                HeadBucketRequest {
                    bucket = config.bucket
                }
            )
            true
        } catch (e: Exception) {
            println("S3连接测试失败: ${e.message}")
            false
        }
    }

    override suspend fun listObjects(prefix: String?): List<RemoteObject> {
        val client = getClient()
        val fullPrefix = if (config.prefix.isNotEmpty()) {
            "${config.prefix}${prefix ?: ""}"
        } else {
            prefix ?: ""
        }

        val objects = mutableListOf<RemoteObject>()
        var continuationToken: String? = null

        do {
            val response = client.listObjectsV2(
                ListObjectsV2Request {
                    bucket = config.bucket
                    this.prefix = fullPrefix
                    this.continuationToken = continuationToken
                    maxKeys = 1000
                }
            )

            response.contents?.forEach { obj ->
                objects.add(
                    RemoteObject(
                        key = obj.key?.removePrefix(config.prefix) ?: "",
                        size = obj.size ?: 0,
                        etag = obj.eTag?.trim('"') ?: "",
                        lastModified = obj.lastModified?.epochSeconds ?: 0
                    )
                )
            }

            continuationToken = response.nextContinuationToken
        } while (continuationToken != null)

        return objects
    }

    override suspend fun uploadObject(
        localPath: String,
        remotePath: String,
        progress: (Long, Long) -> Unit
    ): UploadResult {
        val client = getClient()
        val file = File(localPath)

        if (!file.exists()) {
            return UploadResult(success = false, error = "文件不存在: $localPath")
        }

        val fullKey = if (config.prefix.isNotEmpty()) {
            "${config.prefix}$remotePath"
        } else {
            remotePath
        }

        val totalBytes = file.length()

        return try {
            // 小文件直接上传
            if (totalBytes < 5 * 1024 * 1024) { // 5MB
                progress(0, totalBytes)
                val result = client.putObject(
                    PutObjectRequest {
                        bucket = config.bucket
                        key = fullKey
                        contentLength = totalBytes
                        body = file.asByteStream()
                    }
                )
                progress(totalBytes, totalBytes)

                UploadResult(
                    success = true,
                    etag = result.eTag?.trim('"'),
                    versionId = result.versionId
                )
            } else {
                // 大文件分片上传
                multipartUpload(file, fullKey, progress)
            }
        } catch (e: Exception) {
            UploadResult(success = false, error = e.message)
        }
    }

    /**
     * 分片上传
     */
    private suspend fun multipartUpload(
        file: File,
        key: String,
        progress: (Long, Long) -> Unit
    ): UploadResult {
        val client = getClient()
        val partSize = 5 * 1024 * 1024L // 5MB per part
        val totalBytes = file.length()

        // 1. 初始化分片上传
        val createResponse = client.createMultipartUpload(
            CreateMultipartUploadRequest {
                bucket = config.bucket
                this.key = key
            }
        )

        val uploadId = createResponse.uploadId
            ?: return UploadResult(success = false, error = "无法获取uploadId")

        val completedParts = mutableListOf<CompletedPart>()
        var uploadedBytes = 0L

        try {
            // 2. 上传分片
            RandomAccessFile(file, "r").use { raf ->
                var partNumber = 1
                val buffer = ByteArray(partSize.toInt())

                while (uploadedBytes < totalBytes) {
                    val bytesToRead = minOf(partSize, totalBytes - uploadedBytes).toInt()
                    raf.seek(uploadedBytes)
                    val bytesRead = raf.read(buffer, 0, bytesToRead)

                    if (bytesRead <= 0) break

                    val partData = if (bytesRead == buffer.size) {
                        buffer
                    } else {
                        buffer.copyOf(bytesRead)
                    }

                    val uploadResponse = client.uploadPart(
                        UploadPartRequest {
                            bucket = config.bucket
                            this.key = key
                            this.uploadId = uploadId
                            this.partNumber = partNumber
                            contentLength = bytesRead.toLong()
                            body = ByteStream.fromBytes(partData)
                        }
                    )

                    completedParts.add(
                        CompletedPart {
                            this.partNumber = partNumber
                            this.eTag = uploadResponse.eTag
                        }
                    )

                    uploadedBytes += bytesRead
                    progress(uploadedBytes, totalBytes)
                    partNumber++
                }
            }

            // 3. 完成分片上传
            val completeResponse = client.completeMultipartUpload(
                CompleteMultipartUploadRequest {
                    bucket = config.bucket
                    this.key = key
                    this.uploadId = uploadId
                    multipartUpload {
                        parts = completedParts.sortedBy { it.partNumber ?: Int.MAX_VALUE }
                    }
                }
            )

            return UploadResult(
                success = true,
                etag = completeResponse.eTag?.trim('"'),
                versionId = completeResponse.versionId
            )
        } catch (e: Exception) {
            // 发生错误，中止上传
            try {
                client.abortMultipartUpload(
                    AbortMultipartUploadRequest {
                        bucket = config.bucket
                        this.key = key
                        this.uploadId = uploadId
                    }
                )
            } catch (_: Exception) {
            }

            return UploadResult(success = false, error = "分片上传失败: ${e.message}")
        }
    }

    override suspend fun downloadObject(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): DownloadResult {
        val client = getClient()
        val fullKey = if (config.prefix.isNotEmpty()) {
            "${config.prefix}$remotePath"
        } else {
            remotePath
        }

        return try {
            client.getObject(
                GetObjectRequest {
                    bucket = config.bucket
                    key = fullKey
                }
            ) { response ->
                val body = response.body
                    ?: return@getObject DownloadResult(success = false, error = "响应体为空")

                val totalBytes = response.contentLength ?: 0L
                var downloadedBytes = 0L

                val outputFile = File(localPath)
                outputFile.parentFile?.mkdirs()

                outputFile.outputStream().use { output ->
                    body.toFlow().collect { chunk ->
                        output.write(chunk)
                        downloadedBytes += chunk.size.toLong()
                        progress(downloadedBytes, totalBytes)
                    }
                }

                DownloadResult(success = true, bytesDownloaded = downloadedBytes)
            }
        } catch (e: Exception) {
            DownloadResult(success = false, error = e.message)
        }
    }

    override suspend fun deleteObject(remotePath: String): Boolean {
        val client = getClient()
        val fullKey = if (config.prefix.isNotEmpty()) {
            "${config.prefix}$remotePath"
        } else {
            remotePath
        }

        return try {
            client.deleteObject(
                DeleteObjectRequest {
                    bucket = config.bucket
                    key = fullKey
                }
            )
            true
        } catch (e: Exception) {
            println("删除对象失败: ${e.message}")
            false
        }
    }

    override suspend fun getObjectMetadata(remotePath: String): RemoteObject? {
        val client = getClient()
        val fullKey = if (config.prefix.isNotEmpty()) {
            "${config.prefix}$remotePath"
        } else {
            remotePath
        }

        return try {
            val response = client.headObject(
                HeadObjectRequest {
                    bucket = config.bucket
                    key = fullKey
                }
            )

            RemoteObject(
                key = remotePath,
                size = response.contentLength ?: 0,
                etag = response.eTag?.trim('"') ?: "",
                versionId = response.versionId,
                lastModified = response.lastModified?.epochSeconds ?: 0
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 关闭客户端
     */
    fun close() {
        s3Client?.close()
        s3Client = null
    }
}

/**
 * S3存储客户端构建器
 */
class S3StorageClientBuilder {
    private var endpoint: String = ""
    private var region: String = "us-east-1"
    private var bucket: String = ""
    private var accessKey: String = ""
    private var secretKey: String = ""
    private var prefix: String = ""
    private var forcePathStyle: Boolean = false

    fun endpoint(endpoint: String) = apply { this.endpoint = endpoint }
    fun region(region: String) = apply { this.region = region }
    fun bucket(bucket: String) = apply { this.bucket = bucket }
    fun accessKey(accessKey: String) = apply { this.accessKey = accessKey }
    fun secretKey(secretKey: String) = apply { this.secretKey = secretKey }
    fun prefix(prefix: String) = apply { this.prefix = prefix }
    fun forcePathStyle(force: Boolean) = apply { this.forcePathStyle = force }

    fun build(): S3StorageClient {
        require(endpoint.isNotEmpty()) { "Endpoint不能为空" }
        require(bucket.isNotEmpty()) { "Bucket不能为空" }
        require(accessKey.isNotEmpty()) { "AccessKey不能为空" }
        require(secretKey.isNotEmpty()) { "SecretKey不能为空" }

        return S3StorageClient(
            S3Config(
                endpoint = endpoint,
                region = region,
                bucket = bucket,
                accessKey = accessKey,
                secretKey = secretKey,
                prefix = prefix,
                forcePathStyle = forcePathStyle
            )
        )
    }
}

/**
 * 为MinIO优化的构建器
 */
fun createMinIOClient(
    endpoint: String,  // 如: http://localhost:9000
    bucket: String,
    accessKey: String,
    secretKey: String,
    prefix: String = ""
): S3StorageClient {
    return S3StorageClientBuilder()
        .endpoint(endpoint)
        .region("us-east-1")  // MinIO不验证region
        .bucket(bucket)
        .accessKey(accessKey)
        .secretKey(secretKey)
        .prefix(prefix)
        .forcePathStyle(true)  // MinIO需要路径样式
        .build()
}
