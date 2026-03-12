package com.kcloud.plugins.webdav.server

import com.kcloud.plugin.KCloudLocalPaths
import com.kcloud.plugin.readKCloudJson
import com.kcloud.plugin.writeKCloudJson
import com.kcloud.plugins.webdav.WebDavActionResult
import com.kcloud.plugins.webdav.WebDavConnectionConfig
import com.kcloud.plugins.webdav.WebDavEntry
import com.kcloud.plugins.webdav.WebDavWorkspaceService
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

private const val WEBDAV_PLUGIN_ID = "webdav-plugin"

class WebDavWorkspaceServiceImpl : WebDavWorkspaceService {
    private val settingsFile = File(KCloudLocalPaths.pluginDir(WEBDAV_PLUGIN_ID), "settings.json")
    private val httpClient: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    override fun loadSettings(): WebDavConnectionConfig {
        return readKCloudJson(settingsFile) {
            WebDavConnectionConfig()
        }
    }

    override fun saveSettings(settings: WebDavConnectionConfig): WebDavConnectionConfig {
        writeKCloudJson(settingsFile, settings)
        return settings
    }

    override fun testConnection(settings: WebDavConnectionConfig): WebDavActionResult {
        return runCatching {
            val request = requestBuilder(settings, settings.basePath, "OPTIONS")
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            val success = response.statusCode() in 200..299
            WebDavActionResult(
                success = success,
                message = if (success) "WebDAV 连接成功" else "WebDAV 连接失败：${response.statusCode()}"
            )
        }.getOrElse { throwable ->
            WebDavActionResult(false, throwable.message ?: "WebDAV 连接失败")
        }
    }

    override fun listDirectory(path: String): List<WebDavEntry> {
        val settings = loadSettings()
        return runCatching {
            val body = """
                <?xml version="1.0" encoding="utf-8" ?>
                <d:propfind xmlns:d="DAV:">
                  <d:prop>
                    <d:resourcetype/>
                    <d:getcontentlength/>
                    <d:getlastmodified/>
                  </d:prop>
                </d:propfind>
            """.trimIndent()
            val request = requestBuilder(settings, path.ifBlank { settings.basePath }, "PROPFIND")
                .header("Depth", "1")
                .header("Content-Type", "application/xml; charset=utf-8")
                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body))
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in listOf(207, 200)) {
                return emptyList()
            }

            parsePropFindResponse(response.body(), path.ifBlank { settings.basePath })
        }.getOrDefault(emptyList())
    }

    override fun createDirectory(path: String): WebDavActionResult {
        val settings = loadSettings()
        return runCatching {
            val request = requestBuilder(settings, path, "MKCOL")
                .method("MKCOL", HttpRequest.BodyPublishers.noBody())
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            val success = response.statusCode() in listOf(201, 200, 204, 405)
            WebDavActionResult(success, if (success) "已创建目录 $path" else "创建目录失败：${response.statusCode()}")
        }.getOrElse { throwable ->
            WebDavActionResult(false, throwable.message ?: "创建目录失败")
        }
    }

    override fun deletePath(path: String): WebDavActionResult {
        val settings = loadSettings()
        return runCatching {
            val request = requestBuilder(settings, path, "DELETE")
                .DELETE()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.discarding())
            val success = response.statusCode() in 200..299
            WebDavActionResult(success, if (success) "已删除 $path" else "删除失败：${response.statusCode()}")
        }.getOrElse { throwable ->
            WebDavActionResult(false, throwable.message ?: "删除失败")
        }
    }

    private fun requestBuilder(
        settings: WebDavConnectionConfig,
        path: String,
        method: String
    ): HttpRequest.Builder {
        require(settings.baseUrl.isNotBlank()) { "Base URL 不能为空" }

        val normalizedBase = settings.baseUrl.trimEnd('/')
        val normalizedPath = path.trim().ifBlank { "/" }.let {
            if (it.startsWith("/")) it else "/$it"
        }
        val target = URI.create("$normalizedBase$normalizedPath")
        val auth = Base64.getEncoder().encodeToString("${settings.username}:${settings.password}".toByteArray())

        return HttpRequest.newBuilder(target)
            .header("Authorization", "Basic $auth")
            .header("Accept", "application/xml, application/json, */*")
            .method(method, HttpRequest.BodyPublishers.noBody())
    }

    private fun parsePropFindResponse(
        xml: String,
        currentPath: String
    ): List<WebDavEntry> {
        val builder = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
        }.newDocumentBuilder()
        val document = builder.parse(ByteArrayInputStream(xml.toByteArray()))
        val responses = document.getElementsByTagName("d:response")
        if (responses.length == 0) {
            return emptyList()
        }

        val normalizedCurrent = currentPath.trim().ifBlank { "/" }.trimEnd('/')

        return buildList {
            for (index in 0 until responses.length) {
                val node = responses.item(index)
                val element = node as? Element ?: continue
                val href = textOf(element, "d:href").trim()
                val decodedHref = URLDecoder.decode(href, StandardCharsets.UTF_8)
                val normalizedHref = decodedHref.trimEnd('/')
                if (normalizedHref == normalizedCurrent || normalizedHref.endsWith(normalizedCurrent)) {
                    continue
                }

                val name = normalizedHref.substringAfterLast('/').ifBlank { decodedHref.substringAfterLast('/') }
                val resourceType = element.getElementsByTagName("d:collection").length > 0
                val contentLength = textOf(element, "d:getcontentlength").toLongOrNull() ?: 0L
                val modifiedAt = parseModifiedTime(textOf(element, "d:getlastmodified"))

                add(
                    WebDavEntry(
                        name = name,
                        path = decodedHref,
                        directory = resourceType,
                        size = contentLength,
                        modifiedAt = modifiedAt
                    )
                )
            }
        }
    }

    private fun textOf(element: Element, tagName: String): String {
        return element.getElementsByTagName(tagName)
            .item(0)
            ?.textContent
            .orEmpty()
    }

    private fun parseModifiedTime(value: String): Long {
        return runCatching {
            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.getOrDefault(0L)
    }
}
