package site.addzero.kbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncStatus
import site.addzero.kbox.core.model.KboxSyncTransferQueueState
import site.addzero.kbox.plugins.tools.storagetool.KboxSyncToolState
import java.awt.BasicStroke
import java.awt.Color
import java.awt.EventQueue
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import kotlin.math.roundToInt
import java.util.Locale

@Composable
fun KboxSystemTrayHost(
    enabled: Boolean,
    windowVisible: Boolean,
    syncState: KboxSyncToolState,
    onToggleWindow: () -> Unit,
    onStartSync: () -> Unit,
    onPauseSync: () -> Unit,
    onRefreshSync: () -> Unit,
    onReleaseLocalSpace: () -> Unit,
    onExit: () -> Unit,
) {
    if (!enabled) {
        return
    }

    val controller = remember { KboxSystemTrayController() }
    val toggleWindowState = rememberUpdatedState(onToggleWindow)
    val startSyncState = rememberUpdatedState(onStartSync)
    val pauseSyncState = rememberUpdatedState(onPauseSync)
    val refreshSyncState = rememberUpdatedState(onRefreshSync)
    val releaseLocalState = rememberUpdatedState(onReleaseLocalSpace)
    val exitState = rememberUpdatedState(onExit)

    DisposableEffect(controller) {
        controller.install(
            onToggleWindow = { toggleWindowState.value() },
            onStartSync = { startSyncState.value() },
            onPauseSync = { pauseSyncState.value() },
            onRefreshSync = { refreshSyncState.value() },
            onReleaseLocalSpace = { releaseLocalState.value() },
            onExit = { exitState.value() },
        )
        onDispose {
            controller.dispose()
        }
    }

    SideEffect {
        val runState = syncState.runState
        val canStartSync = syncState.canStartSync &&
            runState.status !in setOf(KboxSyncStatus.STARTING, KboxSyncStatus.SCANNING, KboxSyncStatus.RUNNING)
        val canPauseSync = runState.status !in setOf(KboxSyncStatus.STOPPED, KboxSyncStatus.PAUSED)
        controller.update(
            windowVisible = windowVisible,
            runStatus = runState.status,
            lastError = runState.lastError,
            queue = syncState.transferQueue,
            canStartSync = canStartSync,
            canPauseSync = canPauseSync,
            canRefreshSync = syncState.canStartSync,
            releasableEntryCount = syncState.releasableEntryCount,
            releasableBytes = syncState.releasableBytes,
        )
    }
}

private class KboxSystemTrayController {
    private val statusItem = MenuItem("Status: Stopped").apply { isEnabled = false }
    private val queueItem = MenuItem("Queue: idle").apply { isEnabled = false }
    private val windowItem = MenuItem("Open KBox")
    private val startItem = MenuItem("Start sync")
    private val pauseItem = MenuItem("Pause sync")
    private val refreshItem = MenuItem("Refresh sync")
    private val releaseItem = MenuItem("Release local space")
    private val exitItem = MenuItem("Exit KBox")

    private var trayIcon: TrayIcon? = null

    fun install(
        onToggleWindow: () -> Unit,
        onStartSync: () -> Unit,
        onPauseSync: () -> Unit,
        onRefreshSync: () -> Unit,
        onReleaseLocalSpace: () -> Unit,
        onExit: () -> Unit,
    ) {
        if (!SystemTray.isSupported() || trayIcon != null) {
            return
        }
        val popup = PopupMenu().apply {
            add(statusItem)
            add(queueItem)
            addSeparator()
            add(windowItem)
            addSeparator()
            add(startItem)
            add(pauseItem)
            add(refreshItem)
            add(releaseItem)
            addSeparator()
            add(exitItem)
        }
        windowItem.addActionListener { onToggleWindow() }
        startItem.addActionListener { onStartSync() }
        pauseItem.addActionListener { onPauseSync() }
        refreshItem.addActionListener { onRefreshSync() }
        releaseItem.addActionListener { onReleaseLocalSpace() }
        exitItem.addActionListener { onExit() }
        val icon = TrayIcon(
            createTrayImage(KboxTrayVisualState.IDLE, 0f),
            "KBox",
            popup,
        ).apply {
            isImageAutoSize = true
            addActionListener { onToggleWindow() }
        }
        trayIcon = icon
        EventQueue.invokeLater {
            runCatching {
                SystemTray.getSystemTray().add(icon)
            }
        }
    }

    fun update(
        windowVisible: Boolean,
        runStatus: KboxSyncStatus,
        lastError: String,
        queue: KboxSyncTransferQueueState,
        canStartSync: Boolean,
        canPauseSync: Boolean,
        canRefreshSync: Boolean,
        releasableEntryCount: Int,
        releasableBytes: Long,
    ) {
        val icon = trayIcon ?: return
        EventQueue.invokeLater {
            val visualState = resolveVisualState(runStatus, queue, releasableEntryCount)
            statusItem.label = buildStatusLabel(runStatus, lastError, releasableEntryCount)
            queueItem.label = buildQueueLabel(queue)
            windowItem.label = if (windowVisible) "Hide KBox" else "Open KBox"
            startItem.isEnabled = canStartSync
            pauseItem.isEnabled = canPauseSync
            refreshItem.isEnabled = canRefreshSync
            releaseItem.isEnabled = releasableEntryCount > 0 || canRefreshSync
            releaseItem.label = if (releasableEntryCount > 0) {
                "Release local space ($releasableEntryCount)"
            } else {
                "Release local space"
            }
            icon.toolTip = buildTooltip(
                runStatus = runStatus,
                queue = queue,
                releasableEntryCount = releasableEntryCount,
                releasableBytes = releasableBytes,
                lastError = lastError,
            )
            icon.image = createTrayImage(visualState, queue.overallProgressFraction)
        }
    }

    fun dispose() {
        val icon = trayIcon ?: return
        trayIcon = null
        EventQueue.invokeLater {
            runCatching {
                SystemTray.getSystemTray().remove(icon)
            }
        }
    }
}

private enum class KboxTrayVisualState {
    IDLE,
    SYNCING,
    WARNING,
    ERROR,
    PAUSED,
}

private fun resolveVisualState(
    runStatus: KboxSyncStatus,
    queue: KboxSyncTransferQueueState,
    releasableEntryCount: Int,
): KboxTrayVisualState {
    return when {
        runStatus == KboxSyncStatus.ERROR -> KboxTrayVisualState.ERROR
        runStatus == KboxSyncStatus.PAUSED -> KboxTrayVisualState.PAUSED
        queue.activeTasks.isNotEmpty() -> KboxTrayVisualState.SYNCING
        releasableEntryCount > 0 -> KboxTrayVisualState.WARNING
        runStatus == KboxSyncStatus.SCANNING || runStatus == KboxSyncStatus.STARTING -> KboxTrayVisualState.SYNCING
        else -> KboxTrayVisualState.IDLE
    }
}

private fun buildStatusLabel(
    runStatus: KboxSyncStatus,
    lastError: String,
    releasableEntryCount: Int,
): String {
    val suffix = when {
        lastError.isNotBlank() -> " / ${lastError.take(32)}"
        releasableEntryCount > 0 -> " / $releasableEntryCount releasable"
        else -> ""
    }
    return "Status: ${runStatus.name.lowercase().replaceFirstChar(Char::titlecase)}$suffix"
}

private fun buildQueueLabel(
    queue: KboxSyncTransferQueueState,
): String {
    val task = queue.currentTask ?: return "Queue: idle"
    val percent = (task.progressFraction * 100).roundToInt()
    return "Queue: ${actionLabel(task.action)} ${percent}%"
}

private fun buildTooltip(
    runStatus: KboxSyncStatus,
    queue: KboxSyncTransferQueueState,
    releasableEntryCount: Int,
    releasableBytes: Long,
    lastError: String,
): String {
    val task = queue.currentTask
    val detail = when {
        lastError.isNotBlank() -> lastError
        task != null -> "${actionLabel(task.action)} ${task.relativePath}"
        releasableEntryCount > 0 -> "$releasableEntryCount files / ${formatBytes(releasableBytes)} reclaimable"
        else -> "Idle"
    }
    return "KBox ${runStatus.name.lowercase().replaceFirstChar(Char::titlecase)} - $detail"
}

private fun createTrayImage(
    state: KboxTrayVisualState,
    progress: Float,
): Image {
    val size = 32
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    try {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.color = when (state) {
            KboxTrayVisualState.IDLE -> Color(0x2E, 0x7D, 0x32)
            KboxTrayVisualState.SYNCING -> Color(0x15, 0x65, 0xC0)
            KboxTrayVisualState.WARNING -> Color(0xEF, 0x6C, 0x00)
            KboxTrayVisualState.ERROR -> Color(0xC6, 0x28, 0x28)
            KboxTrayVisualState.PAUSED -> Color(0x9E, 0x9D, 0x24)
        }
        graphics.fillOval(2, 2, size - 4, size - 4)
        if (state == KboxTrayVisualState.SYNCING) {
            graphics.color = Color.WHITE
            graphics.stroke = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            val sweep = (progress.coerceIn(0f, 1f) * 300f).roundToInt().coerceAtLeast(32)
            graphics.drawArc(5, 5, size - 10, size - 10, 90, -sweep)
        }
        graphics.color = Color.WHITE
        graphics.font = Font("SansSerif", Font.BOLD, 16)
        drawCenteredString(graphics, "K", size, size)
    } finally {
        graphics.dispose()
    }
    return image
}

private fun drawCenteredString(
    graphics: Graphics2D,
    text: String,
    width: Int,
    height: Int,
) {
    val metrics = graphics.fontMetrics
    val x = (width - metrics.stringWidth(text)) / 2
    val y = (height - metrics.height) / 2 + metrics.ascent
    graphics.drawString(text, x, y)
}

private fun actionLabel(
    action: KboxSyncAction,
): String {
    return when (action) {
        KboxSyncAction.UPLOAD -> "Upload"
        KboxSyncAction.DOWNLOAD -> "Download"
        KboxSyncAction.RELEASE_LOCAL -> "Release"
        KboxSyncAction.KEEP_LOCAL -> "Keep local"
        KboxSyncAction.KEEP_REMOTE -> "Keep remote"
        KboxSyncAction.COMPARE_CONTENT -> "Compare"
    }
}

private fun formatBytes(
    bytes: Long,
): String {
    if (bytes <= 0) {
        return "0 B"
    }
    if (bytes < 1024) {
        return "$bytes B"
    }
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index += 1
    }
    return String.format(Locale.US, "%.1f %s", value, units[index])
}
