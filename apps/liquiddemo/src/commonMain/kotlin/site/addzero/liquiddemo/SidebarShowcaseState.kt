package site.addzero.liquiddemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.workbenchshell.ScreenNode
import site.addzero.workbenchshell.ScreenTree
import site.addzero.workbenchshell.breadcrumbNamesFor
import site.addzero.workbenchshell.findLeaf
import site.addzero.workbenchshell.visibleLeafNodesUnder

@Single
class SidebarShowcaseState(
    private val screenTree: ScreenTree,
    private val sceneSlots: List<SidebarShowcaseSlot>,
) {
    val scenes: List<ScreenNode> = screenTree.roots
        .filter { node -> node.children.isNotEmpty() }

    private val sceneById: Map<String, ScreenNode> = scenes.associateBy { scene -> scene.id }
    private val slotBySceneId: Map<String, SidebarShowcaseSlot> = sceneSlots.associateBy { slot ->
        slot.config.sceneId
    }

    init {
        require(scenes.isNotEmpty()) {
            "liquiddemo 至少需要一个场景 Screen。"
        }
    }

    var selectedSceneId by mutableStateOf(scenes.first().id)
        private set

    var selectedScreenId by mutableStateOf(initialLeafIdFor(scenes.first().id))
        private set

    var detailVisible by mutableStateOf(true)
        private set

    var isDarkTheme by mutableStateOf(sceneSlots.firstOrNull()?.config?.isDarkTheme ?: true)
        private set

    var isChinese by mutableStateOf(true)
        private set

    val activeSceneNode: ScreenNode
        get() = sceneById[selectedSceneId] ?: scenes.first()

    val activeSlot: SidebarShowcaseSlot
        get() = slotBySceneId[selectedSceneId]
            ?: DefaultSidebarShowcaseSlot(
                sceneId = activeSceneNode.id,
                sceneName = activeSceneNode.name,
            )

    val currentShell: AppSidebarScaffoldShell
        get() = activeSlot.config.shell

    val sceneChildren: List<ScreenNode>
        get() = activeSceneNode.children

    val activeLeafNode: ScreenNode
        get() = resolveActiveLeafNode()

    val breadcrumb: List<String>
        get() = screenTree.breadcrumbNamesFor(activeLeafNode.id)

    val languageToggleLabel: String
        get() = if (isChinese) "中文" else "EN"

    val githubLabel: String
        get() = "github.com/addzero"

    fun selectScene(sceneId: String) {
        val scene = sceneById[sceneId] ?: return
        selectedSceneId = scene.id
        selectedScreenId = initialLeafIdFor(scene.id)
    }

    fun selectScreen(screenId: String) {
        val leaf = screenTree.findLeaf(screenId) ?: return
        selectedScreenId = leaf.id
        selectedSceneId = leaf.ancestorIds.firstOrNull() ?: leaf.id
    }

    fun toggleDetailVisibility() {
        detailVisible = !detailVisible
        println("detailVisible=$detailVisible")
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        println("theme=${if (isDarkTheme) "dark" else "light"}")
    }

    fun toggleLanguage() {
        isChinese = !isChinese
        println("language=${if (isChinese) "zh-CN" else "en-US"}")
    }

    fun openGithub() {
        println("github=https://$githubLabel")
    }

    fun openNotifications() {
        println("notifications=${activeSlot.config.notificationCount}")
    }

    fun openUserProfile() {
        println("user=${activeSlot.config.userLabel}")
    }

    private fun initialLeafIdFor(sceneId: String): String {
        val preferredLeafId = slotBySceneId[sceneId]?.config?.initialLeafId
        val preferredLeaf = preferredLeafId?.let(screenTree::findLeaf)
        if (preferredLeaf != null && preferredLeaf.belongsToScene(sceneId)) {
            return preferredLeaf.id
        }
        return screenTree.visibleLeafNodesUnder(sceneId).firstOrNull()?.id
            ?: screenTree.defaultLeafId
    }

    private fun resolveActiveLeafNode(): ScreenNode {
        val selectedLeaf = screenTree.findLeaf(selectedScreenId)
        if (selectedLeaf != null && selectedLeaf.belongsToScene(selectedSceneId)) {
            return selectedLeaf
        }
        return screenTree.findLeaf(initialLeafIdFor(selectedSceneId))
            ?: screenTree.visibleLeafNodes.first()
    }

    private fun ScreenNode.belongsToScene(sceneId: String): Boolean {
        return ancestorIds.firstOrNull() == sceneId || id == sceneId
    }
}
