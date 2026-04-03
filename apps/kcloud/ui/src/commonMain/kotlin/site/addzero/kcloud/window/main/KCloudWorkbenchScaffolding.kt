package site.addzero.kcloud.window.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.adminWorkbenchConfig
import site.addzero.appsidebar.adminWorkbenchPageConfig
import site.addzero.appsidebar.adminWorkbenchSlots
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.theme.currentKCloudUiMetrics
import site.addzero.kcloud.window.spi.KCloudBrandSlotSpi
import site.addzero.kcloud.window.spi.KCloudUserSlotSpi
import site.addzero.kcloud.window.spi.KCloudWorkbenchScaffoldingSpi
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Single(
    binds = [
        KCloudWorkbenchScaffoldingSpi::class,
    ],
)
class DefaultKCloudWorkbenchScaffolding(
    private val shellState: KCloudShellState,
    private val sidebarRenderer: SidebarRender,
    private val headerRenderer: HeaderRender,
    private val contentRenderer: ContentRender,
    private val brandSlot: KCloudBrandSlotSpi,
    private val userSlot: KCloudUserSlotSpi,
) : KCloudWorkbenchScaffoldingSpi {
    @Composable
    override fun Render(
        darkTheme: Boolean,
        onThemeToggle: () -> Unit,
    ) {
        val uiMetrics = currentKCloudUiMetrics()
        val sidebarVisible = shellState.sidebarVisible

        AdminWorkbenchScaffold(
            sidebar = {
                // [左翼] 页面左侧整条导航带，像建筑的左翼走廊。
                sidebarRenderer.Render(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            content = {
                // [中庭] 页面中间最大的一块内容区，像建筑中庭，业务页面主要在这里展开。
                contentRenderer.Render(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            page = adminWorkbenchPageConfig(pageTitle = "KCloud"),
            modifier = Modifier.fillMaxSize(),
            config = adminWorkbenchConfig(
                brandLabel = "OKMY DICS",
                welcomeLabel = "",
                defaultSidebarRatio = if (sidebarVisible) uiMetrics.sidebarRatio else 0f,
                minSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMinWidth else 0.dp,
                maxSidebarWidth = if (sidebarVisible) uiMetrics.sidebarMaxWidth else 0.dp,
                contentPadding = PaddingValues(0.dp),
                detailPadding = PaddingValues(0.dp),
                isDarkTheme = darkTheme,
            ),
            slots = adminWorkbenchSlots(
                brandContent = {
                    // [旗帜位 + 航标位] 左上角先放品牌门头，再接顶部场景切换。
                    with(brandSlot) {
                        Render()
                    }
                    headerRenderer.Render(
                        modifier = Modifier.weight(1f),
                    )
                },
                showContentHeader = false,
                userContent = {
                    // [驾驶舱右手边] 顶栏最右边的全局动作区。
                    with(userSlot) {
                        Render(
                            darkTheme = darkTheme,
                            onThemeToggle = onThemeToggle,
                        )
                    }
                },
            ),
        )
    }
}
