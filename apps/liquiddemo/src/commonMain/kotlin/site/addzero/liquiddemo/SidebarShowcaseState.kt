package site.addzero.liquiddemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebarScaffoldShell

@Single
class SidebarShowcaseState(
    private val catalog: SidebarShowcaseCatalog,
) {
    val scenes: List<SidebarShowcaseSceneDefinition> = catalog.scenes

    init {
        require(scenes.isNotEmpty()) {
            "liquiddemo 至少需要一个场景定义。"
        }
    }

    var selectedSceneId by mutableStateOf(catalog.defaultSceneId)
        private set

    var selectedLeafId by mutableStateOf(initialLeafIdFor(catalog.defaultSceneId))
        private set

    var detailVisible by mutableStateOf(true)
        private set

    var isDarkTheme by mutableStateOf(scenes.firstOrNull()?.config?.isDarkTheme ?: true)
        private set

    var isChinese by mutableStateOf(true)
        private set

    val activeScene: SidebarShowcaseSceneDefinition
        get() = catalog.findScene(selectedSceneId) ?: scenes.first()

    val currentShell: AppSidebarScaffoldShell
        get() = activeScene.config.shell

    val sceneLeaves: List<SidebarShowcaseLeaf>
        get() = activeScene.leaves

    val activeLeafNode: SidebarShowcaseLeaf
        get() = resolveActiveLeafNode()

    val breadcrumb: List<String>
        get() = catalog.breadcrumbNamesFor(activeLeafNode.id)

    val languageToggleLabel: String
        get() = if (isChinese) "中文" else "EN"

    val githubLabel: String
        get() = "github.com/addzero"

    fun selectScene(sceneId: String) {
        val scene = catalog.findScene(sceneId) ?: return
        selectedSceneId = scene.id
        selectedLeafId = initialLeafIdFor(scene.id)
    }

    fun selectLeaf(leafId: String) {
        val leaf = catalog.findLeaf(leafId) ?: return
        val scene = catalog.findSceneForLeaf(leaf.id) ?: return
        selectedLeafId = leaf.id
        selectedSceneId = scene.id
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
        println("notifications=${activeScene.config.notificationCount}")
    }

    fun openUserProfile() {
        println("user=${activeScene.config.userLabel}")
    }

    private fun initialLeafIdFor(sceneId: String): String {
        val scene = catalog.findScene(sceneId) ?: return catalog.defaultLeafId
        val preferredLeafId = scene.config.initialLeafId
        if (preferredLeafId.isNotBlank() && scene.leaves.any { leaf -> leaf.id == preferredLeafId }) {
            return preferredLeafId
        }
        return scene.leaves.firstOrNull()?.id ?: catalog.defaultLeafId
    }

    private fun resolveActiveLeafNode(): SidebarShowcaseLeaf {
        val selectedLeaf = catalog.findLeaf(selectedLeafId)
        val selectedScene = selectedLeaf?.let { leaf -> catalog.findSceneForLeaf(leaf.id) }
        if (selectedLeaf != null && selectedScene?.id == selectedSceneId) {
            return selectedLeaf
        }
        return catalog.findLeaf(initialLeafIdFor(selectedSceneId))
            ?: activeScene.leaves.first()
    }
}
