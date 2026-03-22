package site.addzero.workbenchshell

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.WorkbenchScaffoldState
import site.addzero.appsidebar.rememberWorkbenchScaffoldState
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

@Composable
fun RenderSidebar(
    modifier: Modifier = Modifier,
    renderer: WorkbenchSidebarRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

@Composable
fun RenderWorkbenchHeader(
    modifier: Modifier = Modifier,
    renderer: WorkbenchHeaderRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

@Composable
fun RenderWorkbenchContent(
    modifier: Modifier = Modifier,
    renderer: WorkbenchContentRenderer = koinInject(),
) {
    renderer.Render(modifier)
}

@Composable
fun RenderWorkbenchScaffold(
    modifier: Modifier = Modifier,
    contentHeaderScrollable: Boolean = true,
    detail: (@Composable BoxScope.() -> Unit)? = null,
    defaultSidebarRatio: Float = 0.22f,
    state: WorkbenchScaffoldState = rememberWorkbenchScaffoldState(defaultSidebarRatio),
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    detailWidth: Dp = 320.dp,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    detailPadding: PaddingValues = PaddingValues(0.dp),
    sidebarRenderer: WorkbenchSidebarRenderer = koinInject(),
    headerRenderer: WorkbenchHeaderRenderer = koinInject(),
    contentRenderer: WorkbenchContentRenderer = koinInject(),
) {
    WorkbenchScaffold(
        modifier = modifier,
        contentHeaderScrollable = contentHeaderScrollable,
        detail = detail,
        defaultSidebarRatio = defaultSidebarRatio,
        state = state,
        minSidebarWidth = minSidebarWidth,
        maxSidebarWidth = maxSidebarWidth,
        detailWidth = detailWidth,
        outerPadding = outerPadding,
        contentPadding = contentPadding,
        detailPadding = detailPadding,
        sidebar = {
            sidebarRenderer.Render(
                modifier = Modifier.fillMaxSize(),
            )
        },
        contentHeader = {
            headerRenderer.Render(
                modifier = Modifier.fillMaxWidth(),
            )
        },
        content = {
            contentRenderer.Render(
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}
