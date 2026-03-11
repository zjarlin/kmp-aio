package com.kcloud.plugin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 侧边栏贡献者接口 - SPI 扩展点
 *
 * 实现此接口并注册到 Koin（@Single/@Factory），
 * 即可自动聚合到主窗口侧边栏。
 *
 * 示例：
 * ```kotlin
 * @Single
 * class FileManagerContributor : SidebarContributor {
 *     override val id = "filemanager"
 *     override val title = "文件管理"
 *     override val order = 10
 *     override val icon = Icons.Default.Folder
 *
 *     @Composable
 *     override fun Content() {
 *         FileManagerScreen()
 *     }
 * }
 * ```
 */
interface SidebarContributor {
    /** 唯一标识，用于路由和状态保持 */
    val id: String

    /** 显示标题 */
    val title: String

    /** 排序值，越小越靠前（默认 100） */
    val order: Int get() = 100

    /** 图标 */
    val icon: ImageVector? get() = null

    /** 内容区域 */
    @Composable
    fun Content()
}
