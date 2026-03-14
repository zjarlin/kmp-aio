package com.kcloud.plugins.quicktransfer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcloud.plugins.quicktransfer.system.QuickTransferDropTargetInstaller
import java.awt.Window
import java.io.File
import org.koin.compose.koinInject

@Composable
fun DragDropUploadArea(
    modifier: Modifier = Modifier,
    window: Window? = null,
    installer: QuickTransferDropTargetInstaller = koinInject(),
    onFilesDropped: (List<File>) -> Unit = {},
) {
    var dragState by remember { mutableStateOf(DragState.NONE) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(window, installer, coroutineScope) {
        val dropHandle = window?.let { currentWindow ->
            installer.install(
                window = currentWindow,
                coroutineScope = coroutineScope,
                onDragStateChange = { state -> dragState = state },
                onFilesDropped = onFilesDropped,
            )
        }

        onDispose {
            dragState = DragState.NONE
            dropHandle?.close()
        }
    }

    DragDropUploadOverlay(
        dragState = dragState,
        modifier = modifier,
    )
}

@Composable
fun FileManagerWithDragDrop(
    content: @Composable () -> Unit,
) {
    var showUploadOverlay by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        DragDropUploadArea(
            modifier = Modifier.fillMaxSize(),
        )

        UploadProgressBanner(
            visible = showUploadOverlay,
            uploadProgress = uploadProgress,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
