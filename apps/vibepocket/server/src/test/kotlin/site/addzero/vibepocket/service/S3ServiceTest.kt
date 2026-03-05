package site.addzero.vibepocket.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*
import site.addzero.vibepocket.s3.S3Config
import site.addzero.vibepocket.s3.S3Service
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.ByteArrayInputStream

class S3ServiceTest : FunSpec({
    val s3Client = mock<S3Client>()
    val config = S3Config(
        endpoint = "https://s3.cstcloud.cn",
        bucket = "test-bucket"
    )
    val s3Service = S3Service(s3Client, config)

    test("upload bytes should call putObject") {
        val key = "test.txt"
        val bytes = "hello".toByteArray()
        val response = PutObjectResponse.builder().build()
        
        whenever(s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>())).thenReturn(response)

        s3Service.upload(key, bytes)

        verify(s3Client).putObject(
            argThat<PutObjectRequest> { bucket() == "test-bucket" && key() == key },
            any<RequestBody>()
        )
    }

    test("delete should call deleteObject") {
        val key = "test.txt"
        val response = DeleteObjectResponse.builder().build()

        whenever(s3Client.deleteObject(any<DeleteObjectRequest>())).thenReturn(response)

        s3Service.delete(key)

        verify(s3Client).deleteObject(argThat<DeleteObjectRequest> { bucket() == "test-bucket" && key() == key })
    }

    test("getUrl should return correctly formatted url") {
        val key = "test.txt"
        s3Service.getUrl(key) shouldBe "https://s3.cstcloud.cn/test-bucket/test.txt"
    }

    test("list should call listObjectsV2") {
        val prefix = "data/"
        val s3Object = S3Object.builder().key("data/1.txt").build()
        val response = ListObjectsV2Response.builder()
            .contents(listOf(s3Object))
            .build()

        whenever(s3Client.listObjectsV2(any<ListObjectsV2Request>())).thenReturn(response)

        val result = s3Service.list(prefix)

        result.size shouldBe 1
        result[0].key() shouldBe "data/1.txt"
        verify(s3Client).listObjectsV2(argThat<ListObjectsV2Request> { bucket() == "test-bucket" && prefix() == prefix })
    }
})
