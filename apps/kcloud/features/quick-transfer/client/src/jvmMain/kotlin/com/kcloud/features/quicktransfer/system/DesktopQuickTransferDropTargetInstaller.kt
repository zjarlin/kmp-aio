package com.kcloud.features.quicktransfer.system

import com.kcloud.features.quicktransfer.QuickTransferDropService
import com.kcloud.features.quicktransfer.ui.components.DragState
import java.awt.Window
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

interface QuickTransferDropTargetInstaller {
    fun install(
        window: Window,
        coroutineScope: CoroutineScope,
        onDragStateChange: (DragState) -> Unit,
        onFilesDropped: (List<File>) -> Unit = {},
    ): AutoCloseable
}

@Single
class DesktopQuickTransferDropTargetInstaller(
    private val service: QuickTransferDropService,
) : QuickTransferDropTargetInstaller {
    override fun install(
        window: Window,
        coroutineScope: CoroutineScope,
        onDragStateChange: (DragState) -> Unit,
        onFilesDropped: (List<File>) -> Unit,
    ): AutoCloseable {
        val previousDropTarget = window.dropTarget
        val dropTarget = DropTarget().apply {
            addDropTargetListener(
                object : DropTargetAdapter() {
                    override fun dragEnter(event: DropTargetDragEvent?) {
                        updateDragState(event = event, onDragStateChange = onDragStateChange)
                    }

                    override fun dragOver(event: DropTargetDragEvent?) {
                        updateDragState(event = event, onDragStateChange = onDragStateChange)
                    }

                    override fun dragExit(event: DropTargetEvent?) {
                        onDragStateChange(DragState.NONE)
                    }

                    override fun drop(event: DropTargetDropEvent?) {
                        handleDrop(
                            event = event,
                            coroutineScope = coroutineScope,
                            onDragStateChange = onDragStateChange,
                            onFilesDropped = onFilesDropped,
                        )
                    }
                },
            )
        }

        window.dropTarget = dropTarget

        return AutoCloseable {
            onDragStateChange(DragState.NONE)
            if (window.dropTarget === dropTarget) {
                window.dropTarget = previousDropTarget
            }
        }
    }

    private fun updateDragState(
        event: DropTargetDragEvent?,
        onDragStateChange: (DragState) -> Unit,
    ) {
        event ?: return
        if (isDragAcceptable(event)) {
            onDragStateChange(DragState.VALID)
            event.acceptDrag(DnDConstants.ACTION_COPY)
            return
        }

        onDragStateChange(DragState.INVALID)
        event.rejectDrag()
    }

    private fun handleDrop(
        event: DropTargetDropEvent?,
        coroutineScope: CoroutineScope,
        onDragStateChange: (DragState) -> Unit,
        onFilesDropped: (List<File>) -> Unit,
    ) {
        event ?: return
        onDragStateChange(DragState.NONE)

        runCatching {
            event.acceptDrop(DnDConstants.ACTION_COPY)

            val transferable = event.transferable
            if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.dropComplete(false)
                return
            }

            @Suppress("UNCHECKED_CAST")
            val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>

            coroutineScope.launch {
                val result = service.stageDroppedFiles(files)
                if (result.success) {
                    onFilesDropped(files)
                }
            }

            event.dropComplete(true)
        }.onFailure { throwable ->
            throwable.printStackTrace()
            event.dropComplete(false)
        }
    }

    private fun isDragAcceptable(event: DropTargetDragEvent): Boolean {
        return runCatching {
            event.transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        }.getOrDefault(false)
    }
}
