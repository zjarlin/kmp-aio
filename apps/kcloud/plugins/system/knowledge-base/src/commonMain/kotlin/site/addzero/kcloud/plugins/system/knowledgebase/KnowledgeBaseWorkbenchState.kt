package site.addzero.kcloud.plugins.system.knowledgebase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.KnowledgeDocumentDto
import site.addzero.kcloud.system.api.KnowledgeSpaceDto

@Single
class KnowledgeBaseWorkbenchState(
    private val remoteService: KnowledgeBaseRemoteService,
) {
    val spaces = mutableStateListOf<KnowledgeSpaceDto>()
    val documents = mutableStateListOf<KnowledgeDocumentDto>()

    var selectedSpaceId by mutableStateOf<Long?>(null)
        private set
    var selectedDocumentId by mutableStateOf<Long?>(null)
        private set

    var spaceName by mutableStateOf("")
    var spaceDescription by mutableStateOf("")
    var documentTitle by mutableStateOf("")
    var documentContent by mutableStateOf("")
    var statusMessage by mutableStateOf("")
        private set
    var isBusy by mutableStateOf(false)
        private set

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refreshSpaces()
        loaded = true
    }

    suspend fun refreshSpaces() {
        runBusy("已刷新知识空间") {
            val loadedSpaces = remoteService.listSpaces()
            spaces.replaceAll(loadedSpaces)
            val nextSpaceId = selectedSpaceId?.takeIf { currentId ->
                loadedSpaces.any { space -> space.id == currentId }
            } ?: loadedSpaces.firstOrNull()?.id
            if (nextSpaceId == null) {
                clearSelection()
            } else {
                selectSpace(nextSpaceId)
            }
        }
    }

    suspend fun createSpace() {
        require(spaceName.isNotBlank()) {
            "空间名称不能为空"
        }
        runBusy("已创建知识空间") {
            val created = remoteService.createSpace(spaceName.trim(), spaceDescription)
            spaces.add(0, created)
            selectSpace(created.id)
        }
    }

    suspend fun saveSpace() {
        val spaceId = selectedSpaceId ?: return createSpace()
        require(spaceName.isNotBlank()) {
            "空间名称不能为空"
        }
        runBusy("已保存知识空间") {
            val updated = remoteService.updateSpace(spaceId, spaceName.trim(), spaceDescription)
            upsertSpace(updated)
        }
    }

    suspend fun deleteSelectedSpace() {
        val spaceId = selectedSpaceId ?: return
        runBusy("已删除知识空间") {
            remoteService.deleteSpace(spaceId)
            selectedSpaceId = null
            refreshSpaces()
        }
    }

    suspend fun selectSpace(
        spaceId: Long,
    ) {
        selectedSpaceId = spaceId
        val space = spaces.firstOrNull { it.id == spaceId }
        if (space != null) {
            spaceName = space.name
            spaceDescription = space.description.orEmpty()
        }
        val loadedDocuments = remoteService.listDocuments(spaceId)
        documents.replaceAll(loadedDocuments)
        val nextDocument = loadedDocuments.firstOrNull()
        if (nextDocument == null) {
            beginCreateDocument()
        } else {
            selectDocument(nextDocument.id)
        }
    }

    fun beginCreateSpace() {
        selectedSpaceId = null
        spaceName = ""
        spaceDescription = ""
    }

    fun beginCreateDocument() {
        selectedDocumentId = null
        documentTitle = ""
        documentContent = ""
    }

    suspend fun createDocument() {
        val spaceId = selectedSpaceId ?: run {
            statusMessage = "请先选择知识空间"
            return
        }
        require(documentTitle.isNotBlank()) {
            "文档标题不能为空"
        }
        runBusy("已创建文档") {
            val created = remoteService.createDocument(spaceId, documentTitle.trim(), documentContent)
            documents.add(0, created)
            selectDocument(created.id)
        }
    }

    suspend fun saveDocument() {
        val documentId = selectedDocumentId ?: return createDocument()
        require(documentTitle.isNotBlank()) {
            "文档标题不能为空"
        }
        runBusy("已保存文档") {
            val updated = remoteService.updateDocument(documentId, documentTitle.trim(), documentContent)
            upsertDocument(updated)
            selectDocument(updated.id)
        }
    }

    suspend fun deleteSelectedDocument() {
        val documentId = selectedDocumentId ?: return
        runBusy("已删除文档") {
            remoteService.deleteDocument(documentId)
            val currentSpaceId = selectedSpaceId
            if (currentSpaceId != null) {
                selectSpace(currentSpaceId)
            } else {
                beginCreateDocument()
            }
        }
    }

    fun selectDocument(
        documentId: Long,
    ) {
        val document = documents.firstOrNull { it.id == documentId } ?: return
        selectedDocumentId = document.id
        documentTitle = document.title
        documentContent = document.content
    }

    private fun clearSelection() {
        selectedSpaceId = null
        selectedDocumentId = null
        documents.clear()
        spaceName = ""
        spaceDescription = ""
        documentTitle = ""
        documentContent = ""
    }

    private fun upsertSpace(
        space: KnowledgeSpaceDto,
    ) {
        val index = spaces.indexOfFirst { it.id == space.id }
        if (index >= 0) {
            spaces[index] = space
        } else {
            spaces.add(0, space)
        }
    }

    private fun upsertDocument(
        document: KnowledgeDocumentDto,
    ) {
        val index = documents.indexOfFirst { it.id == document.id }
        if (index >= 0) {
            documents[index] = document
        } else {
            documents.add(0, document)
        }
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            statusMessage = successMessage
        }.onFailure { throwable ->
            statusMessage = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}

private fun <T> MutableList<T>.replaceAll(
    newItems: List<T>,
) {
    clear()
    addAll(newItems)
}
