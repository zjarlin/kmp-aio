package com.kcloud.progress

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 任务信息
 */
data class TaskInfo(
    val fileName: String,
    val overallPercent: Int
)

/**
 * 队列进度
 */
data class QueueProgress(
    val activeTasks: List<TaskInfo> = emptyList(),
    val pendingTasks: List<TaskInfo> = emptyList()
)

/**
 * 简化的进度追踪器
 */
class ProgressTracker {
    private val _queueProgress = MutableStateFlow<QueueProgress?>(null)
    val queueProgress: StateFlow<QueueProgress?> = _queueProgress.asStateFlow()

    fun updateProgress(progress: QueueProgress) {
        _queueProgress.value = progress
    }
}
