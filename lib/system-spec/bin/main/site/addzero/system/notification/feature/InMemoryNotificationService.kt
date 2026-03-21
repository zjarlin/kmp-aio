package site.addzero.system.notification.feature

import site.addzero.system.common.dto.PageResult
import site.addzero.system.common.exception.DuplicateResourceException
import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.notification.dto.*
import site.addzero.system.notification.spi.*
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 基于内存的通知服务默认实现
 */
open class InMemoryNotificationService : NotificationSpi {

    protected val notificationStore = ConcurrentHashMap<String, NotificationDTO>()
    protected val idGenerator = AtomicLong(1)

    override fun send(request: NotificationSendRequest): String {
        val id = generateId()
        val notification = NotificationDTO(
            id = id,
            userId = request.recipient.userId,
            title = request.title,
            content = request.content,
            channel = request.channel,
            priority = request.priority,
            status = NotificationStatus.UNREAD,
            senderId = request.senderId,
            senderName = request.senderName,
            actionUrl = request.actionUrl,
            actionType = request.actionType,
            actionId = request.actionId,
            extraData = request.extraData,
            sentAt = request.scheduledAt ?: Instant.now(),
            readAt = null
        )

        notificationStore[id] = notification
        return id
    }

    override fun sendBatch(requests: List<NotificationSendRequest>): List<String> {
        return requests.map { send(it) }
    }

    override fun sendWithTemplate(templateCode: String, params: Map<String, Any?>, recipient: Recipient): String {
        // 简化实现：直接使用模板码作为标题，参数拼接作为内容
        return send(
            NotificationSendRequest(
                recipient = recipient,
                title = "[$templateCode]",
                content = params.toString()
            )
        )
    }

    override fun getById(id: String): NotificationDTO? = notificationStore[id]

    override fun getByUserId(userId: String, status: NotificationStatus?): List<NotificationDTO> {
        return notificationStore.values
            .filter { it.userId == userId }
            .filter { status == null || it.status == status }
            .sortedByDescending { it.sentAt }
    }

    override fun page(query: NotificationQuery): PageResult<NotificationDTO> {
        val filtered = notificationStore.values.filter { notif ->
            (query.userId == null || notif.userId == query.userId) &&
                    (query.channel == null || notif.channel == query.channel) &&
                    (query.status == null || notif.status == query.status) &&
                    (query.priority == null || notif.priority == query.priority) &&
                    (query.startTime == null || notif.sentAt >= query.startTime) &&
                    (query.endTime == null || notif.sentAt <= query.endTime)
        }.sortedByDescending { it.sentAt }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun markAsRead(id: String) {
        val notification = notificationStore[id]
            ?: throw ResourceNotFoundException("Notification", id)
        notificationStore[id] = notification.copy(
            status = NotificationStatus.READ,
            readAt = Instant.now()
        )
    }

    override fun markBatchAsRead(ids: List<String>) {
        ids.forEach { markAsRead(it) }
    }

    override fun delete(id: String) {
        notificationStore.remove(id)
            ?: throw ResourceNotFoundException("Notification", id)
    }

    override fun getUnreadCount(userId: String): Int {
        return notificationStore.values.count {
            it.userId == userId && it.status == NotificationStatus.UNREAD
        }
    }

    override fun broadcast(request: BroadcastRequest): String {
        // 简化实现：返回一个特殊的广播ID
        val id = "BC${System.currentTimeMillis()}"
        // 实际实现中会根据targetUserIds或targetRoles查询用户列表并批量发送
        return id
    }

    private fun generateId(): String {
        return "NT${idGenerator.getAndIncrement()}"
    }
}

/**
 * 基于内存的消息模板服务默认实现
 */
open class InMemoryTemplateService : MessageTemplateSpi {

    protected val templateStore = ConcurrentHashMap<String, MessageTemplateDTO>()
    protected val idGenerator = AtomicLong(1)

    override fun create(request: TemplateCreateRequest): MessageTemplateDTO {
        if (templateStore.containsKey(request.templateCode)) {
            throw DuplicateResourceException("MessageTemplate", "templateCode")
        }

        val template = MessageTemplateDTO(
            id = idGenerator.getAndIncrement().toString(),
            templateCode = request.templateCode,
            templateName = request.templateName,
            channel = request.channel,
            titleTemplate = request.titleTemplate,
            contentTemplate = request.contentTemplate,
            description = request.description,
            isEnabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        templateStore[template.templateCode] = template
        return template
    }

    override fun update(templateCode: String, request: TemplateUpdateRequest): MessageTemplateDTO {
        val existing = templateStore[templateCode]
            ?: throw ResourceNotFoundException("MessageTemplate", templateCode)

        val updated = existing.copy(
            templateName = request.templateName ?: existing.templateName,
            titleTemplate = request.titleTemplate ?: existing.titleTemplate,
            contentTemplate = request.contentTemplate ?: existing.contentTemplate,
            description = request.description ?: existing.description,
            isEnabled = request.isEnabled ?: existing.isEnabled,
            updatedAt = Instant.now()
        )

        templateStore[templateCode] = updated
        return updated
    }

    override fun getByCode(templateCode: String): MessageTemplateDTO? = templateStore[templateCode]

    override fun delete(templateCode: String) {
        templateStore.remove(templateCode)
            ?: throw ResourceNotFoundException("MessageTemplate", templateCode)
    }

    override fun render(templateCode: String, params: Map<String, Any?>): RenderedMessage {
        val template = getByCode(templateCode)
            ?: throw ResourceNotFoundException("MessageTemplate", templateCode)

        var title = template.titleTemplate
        var content = template.contentTemplate

        // 简单替换占位符 ${key}
        params.forEach { (key, value) ->
            title = title.replace("\${$key}", value?.toString() ?: "")
            content = content.replace("\${$key}", value?.toString() ?: "")
        }

        return RenderedMessage(
            title = title,
            content = content,
            channel = template.channel
        )
    }
}
