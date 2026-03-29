package site.addzero.kcloud.server

import io.ktor.server.config.*
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.plugins.rbac.UserProfileService
import site.addzero.kcloud.plugins.system.aichat.AiChatService
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseService
import site.addzero.kcloud.system.api.UserProfileUpdateRequest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KCloudSystemFeatureServiceTest {
    @Test
    fun userProfileServiceSupportsBootstrapAndUpsert() = withServerKoin { koin ->
        val service = koin.get<UserProfileService>()

        val initial = service.readCurrentProfile()
        val saved = service.saveCurrentProfile(
            UserProfileUpdateRequest(
                displayName = "张小云",
                email = "cloud@example.com",
                avatarLabel = "ZX",
                locale = "zh-CN",
                timeZone = "Asia/Shanghai",
            ),
        )
        val reloaded = service.readCurrentProfile()

        assertEquals("desktop-user", initial.accountKey)
        assertEquals(saved.id, reloaded.id)
        assertEquals("张小云", reloaded.displayName)
        assertEquals("cloud@example.com", reloaded.email)
        assertEquals("ZX", reloaded.avatarLabel)
    }

    @Test
    fun aiChatServiceSupportsSessionAndMessageCrud() = withServerKoin { koin ->
        val service = koin.get<AiChatService>()

        val created = service.createSession("测试会话")
        val conversation = service.sendMessage(created.id, "今天先记一个占位消息")
        val listed = service.listSessions()
        val deleted = service.deleteSession(created.id)

        assertEquals(created.id, conversation.session.id)
        assertEquals(2, conversation.messages.size)
        assertEquals("user", conversation.messages.first().role)
        assertEquals("assistant", conversation.messages.last().role)
        assertTrue(conversation.messages.last().content.contains("模型提供方尚未接通"))
        assertTrue(listed.any { it.id == created.id })
        assertTrue(deleted.ok)
        assertFalse(service.listSessions().any { it.id == created.id })
    }

    @Test
    fun knowledgeBaseServiceSupportsSpaceAndDocumentCrud() = withServerKoin { koin ->
        val service = koin.get<KnowledgeBaseService>()

        val createdSpace = service.createSpace("产品知识", "第一批资料")
        val updatedSpace = service.updateSpace(createdSpace.id, "产品知识库", "更新描述")
        val createdDocument = service.createDocument(createdSpace.id, "接入说明", "先写文字版本")
        val updatedDocument = service.updateDocument(createdDocument.id, "接入说明 v2", "补充细节")
        val documents = service.listDocuments(createdSpace.id)

        assertEquals("产品知识库", updatedSpace.name)
        assertEquals("接入说明 v2", updatedDocument.title)
        assertEquals(1, documents.size)

        assertTrue(service.deleteDocument(createdDocument.id).ok)
        assertTrue(service.listDocuments(createdSpace.id).isEmpty())
        assertTrue(service.deleteSpace(createdSpace.id).ok)
        assertTrue(service.listSpaces().none { it.id == createdSpace.id })
    }
}

private inline fun withServerKoin(
    block: (Koin) -> Unit,
) {
    val tempDatabase = Files.createTempFile("kcloud-system-service-test-", ".db").toFile()
    val previousEmbeddedFlag = System.getProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
    System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    val config = MapApplicationConfig(
        "datasources.sqlite.enabled" to "true",
        "datasources.sqlite.url" to "jdbc:sqlite:${tempDatabase.absolutePath}",
        "datasources.sqlite.driver" to "org.sqlite.JDBC",
    )
    val application = koinApplication {
        withConfiguration<KCloudServerStarterKoinApplication>()
        properties(
            mapOf(
                KCLOUD_APPLICATION_CONFIG_PROPERTY to config,
                VIBEPOCKET_APPLICATION_CONFIG_PROPERTY to config,
            ),
        )
    }

    try {
        block(application.koin)
    } finally {
        application.close()
        if (previousEmbeddedFlag == null) {
            System.clearProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
        } else {
            System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, previousEmbeddedFlag)
        }
        tempDatabase.delete()
    }
}

private const val KCLOUD_APPLICATION_CONFIG_PROPERTY = "kcloud.applicationConfig"
private const val VIBEPOCKET_APPLICATION_CONFIG_PROPERTY = "vibepocket.applicationConfig"
private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
