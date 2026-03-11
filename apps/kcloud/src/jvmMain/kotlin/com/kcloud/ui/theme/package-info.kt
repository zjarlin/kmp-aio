/**
 * UI 主题层 - 视觉样式定义
 *
 * ## 职责
 * - 定义应用的色彩系统
 * - 定义字体排版
 * - 提供浅色/深色主题支持
 * - 定义组件默认样式
 *
 * ## 主题组成
 * - [Color] - 颜色定义（主色、次色、背景、表面、错误等）
 * - [Type] - 字体排版（标题、正文、标签等）
 * - [Theme] - 主题组合和 Material3 适配
 *
 * ## 使用方式
 * ```kotlin
 * MoveOffTheme(darkTheme = true) {
 *     // 内容自动应用主题
 *     Surface {
 *         Text("Hello", color = MaterialTheme.colorScheme.primary)
 *     }
 * }
 * ```
 *
 * ## 动态主题
 * - 支持跟随系统主题设置
 * - 支持手动切换
 * - 考虑支持 Material You 动态取色（Android 特性移植）
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.ui.theme
