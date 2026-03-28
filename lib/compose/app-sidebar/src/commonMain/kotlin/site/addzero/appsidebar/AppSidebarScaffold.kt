package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Stable
class AppSidebarScaffoldState internal constructor(
    defaultSidebarRatio: Float,
) {
    var sidebarRatio by mutableFloatStateOf(defaultSidebarRatio)
        private set

    private var sidebarWidthPx by mutableFloatStateOf(Float.NaN)

    fun onContainerMeasured(
        containerWidthPx: Float,
        minSidebarWidthPx: Float,
        maxSidebarWidthPx: Float,
    ) {
        if (containerWidthPx <= 0f) {
            return
        }
        val measuredSidebarWidthPx = if (sidebarWidthPx.isNaN()) {
            containerWidthPx * sidebarRatio
        } else {
            sidebarWidthPx
        }.coerceIn(minSidebarWidthPx, maxSidebarWidthPx)

        sidebarWidthPx = measuredSidebarWidthPx
        sidebarRatio = (measuredSidebarWidthPx / containerWidthPx).coerceIn(0f, 1f)
    }

    fun dragSidebarBy(
        deltaPx: Float,
        containerWidthPx: Float,
        minSidebarWidthPx: Float,
        maxSidebarWidthPx: Float,
    ) {
        if (containerWidthPx <= 0f) {
            return
        }
        val nextWidth = (currentSidebarWidthPx(containerWidthPx) + deltaPx)
            .coerceIn(minSidebarWidthPx, maxSidebarWidthPx)
        sidebarWidthPx = nextWidth
        sidebarRatio = (nextWidth / containerWidthPx).coerceIn(0f, 1f)
    }

    fun currentSidebarWidthPx(
        containerWidthPx: Float,
    ): Float {
        return if (sidebarWidthPx.isNaN()) {
            containerWidthPx * sidebarRatio
        } else {
            sidebarWidthPx
        }
    }

    internal companion object {
        val Saver: Saver<AppSidebarScaffoldState, Float> = Saver(
            save = { state -> state.sidebarRatio },
            restore = { savedRatio -> AppSidebarScaffoldState(savedRatio) },
        )
    }
}

@Composable
fun rememberAppSidebarScaffoldState(
    defaultSidebarRatio: Float = 0.22f,
): AppSidebarScaffoldState = rememberSaveable(
    inputs = arrayOf(defaultSidebarRatio),
    saver = AppSidebarScaffoldState.Saver,
) {
    AppSidebarScaffoldState(
        defaultSidebarRatio = defaultSidebarRatio,
    )
}

typealias WorkbenchScaffoldState = AppSidebarScaffoldState

@Composable
fun rememberWorkbenchScaffoldState(
    defaultSidebarRatio: Float = 0.22f,
): WorkbenchScaffoldState = rememberAppSidebarScaffoldState(
    defaultSidebarRatio = defaultSidebarRatio,
)

@Composable
fun AppSidebarScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    defaultSidebarRatio: Float = 0.22f,
    state: AppSidebarScaffoldState = rememberAppSidebarScaffoldState(defaultSidebarRatio),
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    WorkbenchScaffold(
        sidebar = sidebar,
        content = content,
        modifier = modifier,
        defaultSidebarRatio = defaultSidebarRatio,
        state = state,
        minSidebarWidth = minSidebarWidth,
        maxSidebarWidth = maxSidebarWidth,
        outerPadding = outerPadding,
        contentPadding = contentPadding,
    )
}

@Composable
fun WorkbenchScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    contentHeader: (@Composable RowScope.() -> Unit)? = null,
    contentHeaderScrollable: Boolean = true,
    detail: (@Composable BoxScope.() -> Unit)? = null,
    defaultSidebarRatio: Float = 0.22f,
    state: AppSidebarScaffoldState = rememberAppSidebarScaffoldState(defaultSidebarRatio),
    minSidebarWidth: Dp = 248.dp,
    maxSidebarWidth: Dp = 360.dp,
    detailWidth: Dp = 320.dp,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    detailPadding: PaddingValues = PaddingValues(0.dp),
    sidebarContainerModifier: Modifier = Modifier,
    mainContainerModifier: Modifier = Modifier,
    headerContainerModifier: Modifier = Modifier,
    detailContainerModifier: Modifier = Modifier,
    dividerColor: Color = WorkbenchTokens.dividerColor,
    thumbColor: Color = WorkbenchTokens.thumbColor,
    thumbBorderColor: Color = WorkbenchTokens.thumbBorder,
) {
    ResizableSidebarShell(
        modifier = modifier,
        sidebar = sidebar,
        state = state,
        minSidebarWidth = minSidebarWidth,
        maxSidebarWidth = maxSidebarWidth,
        outerPadding = outerPadding,
        sidebarContainerModifier = sidebarContainerModifier,
        mainContainerModifier = mainContainerModifier,
        dividerColor = dividerColor,
        thumbColor = thumbColor,
        thumbBorderColor = thumbBorderColor,
        body = { layoutClass ->
            MainWorkbenchPanel(
                modifier = Modifier.weight(1f),
                contentHeader = contentHeader,
                contentHeaderScrollable = contentHeaderScrollable,
                contentPadding = contentPadding,
                headerContainerModifier = headerContainerModifier,
                dividerColor = dividerColor,
                content = content,
            )

            if (detail != null && layoutClass == WorkbenchLayoutClass.Expanded) {
                Box(
                    modifier = Modifier.workbenchDivider(
                        color = dividerColor,
                    ),
                )
                Box(
                    modifier = Modifier.width(detailWidth)
                        .fillMaxHeight()
                        .then(detailContainerModifier)
                        .padding(detailPadding),
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    ) {
                        detail()
                    }
                }
            }
        },
    )
}

@Composable
private fun ResizableSidebarShell(
    modifier: Modifier,
    sidebar: @Composable BoxScope.() -> Unit,
    body: @Composable RowScope.(WorkbenchLayoutClass) -> Unit,
    state: AppSidebarScaffoldState,
    minSidebarWidth: Dp,
    maxSidebarWidth: Dp,
    outerPadding: PaddingValues,
    sidebarContainerModifier: Modifier,
    mainContainerModifier: Modifier,
    dividerColor: Color,
    thumbColor: Color,
    thumbBorderColor: Color,
) {
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val minSidebarWidthPx = with(density) { minSidebarWidth.toPx() }
    val maxSidebarWidthPx = with(density) { maxSidebarWidth.toPx() }
    val handleHotZoneWidth = WorkbenchTokens.handleHotZoneWidth
    val handleHotZoneWidthPx = with(density) { handleHotZoneWidth.toPx() }
    val sidebarWidthPx = remember(containerWidthPx, state.sidebarRatio, minSidebarWidthPx, maxSidebarWidthPx) {
        if (containerWidthPx <= 0f) {
            0f
        } else {
            state.currentSidebarWidthPx(containerWidthPx)
                .coerceIn(minSidebarWidthPx, maxSidebarWidthPx)
        }
    }
    val sidebarWidth = with(density) {
        sidebarWidthPx.roundToInt().toDp()
    }
    val layoutClass = resolveWorkbenchLayoutClass(
        widthDp = with(density) { containerWidthPx.toDp() },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(outerPadding)
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
                state.onContainerMeasured(
                    containerWidthPx = containerWidthPx,
                    minSidebarWidthPx = minSidebarWidthPx,
                    maxSidebarWidthPx = maxSidebarWidthPx,
                )
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.width(sidebarWidth)
                    .fillMaxHeight()
                    .then(sidebarContainerModifier),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    sidebar()
                }
            }
            Row(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
                    .then(mainContainerModifier),
                content = {
                    body(layoutClass)
                },
            )
        }

        if (containerWidthPx > 0f) {
            SidebarResizeHandle(
                modifier = Modifier.sidebarHandleOverlay(
                    sidebarWidthPx = sidebarWidthPx,
                    handleHotZoneWidthPx = handleHotZoneWidthPx,
                    handleHotZoneWidth = handleHotZoneWidth,
                ),
                dividerColor = dividerColor,
                thumbColor = thumbColor,
                thumbBorderColor = thumbBorderColor,
                onDrag = { deltaPx ->
                    state.dragSidebarBy(
                        deltaPx = deltaPx,
                        containerWidthPx = containerWidthPx,
                        minSidebarWidthPx = minSidebarWidthPx,
                        maxSidebarWidthPx = maxSidebarWidthPx,
                    )
                },
            )
        }
    }
}

@Composable
private fun MainWorkbenchPanel(
    modifier: Modifier,
    contentHeader: (@Composable RowScope.() -> Unit)?,
    contentHeaderScrollable: Boolean,
    contentPadding: PaddingValues,
    headerContainerModifier: Modifier,
    dividerColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        if (contentHeader != null) {
            val headerOverflowModifier = if (contentHeaderScrollable) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .then(headerOverflowModifier)
                    .then(headerContainerModifier)
                    .workbenchHeaderFrame(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    contentHeader()
                }
            }
            Box(
                modifier = Modifier.workbenchHorizontalDivider(
                    color = dividerColor,
                ),
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(contentPadding),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SidebarResizeHandle(
    modifier: Modifier,
    dividerColor: Color,
    thumbColor: Color,
    thumbBorderColor: Color,
    onDrag: (Float) -> Unit,
) {
    Box(
        modifier = modifier.dragHandlePointer(onDrag = onDrag),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.dragHandleRail(
                color = dividerColor,
            ),
        ) {
            Box(
                modifier = Modifier.align(Alignment.Center).dragHandleThumb(
                    color = thumbColor,
                    borderColor = thumbBorderColor,
                ),
            )
        }
    }
}

/** 拖拽命中区：把分隔线扩成稳定热区，但不在布局上占出肉眼可见的缝。 */
private fun Modifier.dragHandlePointer(
    onDrag: (Float) -> Unit,
): Modifier {
    return pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            onDrag(dragAmount.x)
        }
    }
}

/** 侧栏拖拽热区：悬浮在侧栏边界上，默认无缝但仍然好抓。 */
private fun Modifier.sidebarHandleOverlay(
    sidebarWidthPx: Float,
    handleHotZoneWidthPx: Float,
    handleHotZoneWidth: Dp,
): Modifier {
    return offset {
        IntOffset(
            x = (sidebarWidthPx - handleHotZoneWidthPx / 2f).roundToInt(),
            y = 0,
        )
    }.width(handleHotZoneWidth)
        .fillMaxHeight()
        .zIndex(1f)
}

/** 拖拽分隔轨道：只保留一条极轻的界线，避免出现肉眼可见的大缝隙。 */
private fun Modifier.dragHandleRail(
    color: Color,
): Modifier {
    return width(1.dp)
        .fillMaxHeight()
        .padding(vertical = 10.dp)
        .background(color, CircleShape)
}

/** 拖拽手柄：中间一段胶囊高亮，提示这里支持拖拽调宽。 */
private fun Modifier.dragHandleThumb(
    color: Color,
    borderColor: Color,
): Modifier {
    return width(6.dp)
        .fillMaxHeight(0.08f)
        .background(color, CircleShape)
        .border(1.dp, borderColor, CircleShape)
}

/** 工作台主区域顶栏：默认就是紧凑工具栏，不额外制造卡片缝隙。 */
private fun Modifier.workbenchHeaderFrame(): Modifier {
    return padding(horizontal = 16.dp, vertical = 12.dp)
}

/** 纵向分隔线：给主内容和右侧详情栏一个轻微边界。 */
private fun Modifier.workbenchDivider(
    color: Color,
): Modifier {
    return width(1.dp)
        .fillMaxHeight()
        .background(color)
}

/** 顶栏下分隔线：让 header 和正文在无缝布局里也有清晰层级。 */
private fun Modifier.workbenchHorizontalDivider(
    color: Color,
): Modifier {
    return fillMaxWidth()
        .height(1.dp)
        .background(color)
}

private object WorkbenchTokens {
    val handleHotZoneWidth = 14.dp
    val dividerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f)
    val thumbColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f)
    val thumbBorder = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.10f)
    val expandedBreakpoint = 1200.dp
    val mediumBreakpoint = 760.dp
}

private enum class WorkbenchLayoutClass {
    Compact,
    Medium,
    Expanded,
}

private fun resolveWorkbenchLayoutClass(
    widthDp: Dp,
): WorkbenchLayoutClass {
    return when {
        widthDp.value >= WorkbenchTokens.expandedBreakpoint.value -> WorkbenchLayoutClass.Expanded
        widthDp.value >= WorkbenchTokens.mediumBreakpoint.value -> WorkbenchLayoutClass.Medium
        else -> WorkbenchLayoutClass.Compact
    }
}
