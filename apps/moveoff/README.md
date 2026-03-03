# MoveOff 开源工具 - 技术设计与需求升级文档

> 基于 Kotlin Multiplatform（KMP）构建跨平台桌面工具，采用「共享业务逻辑 + 原生系统集成」模式，目标是提供接近 JetBrains Toolbox 的迁移与文件托管体验。

## 一、核心技术栈定版（Kotlin Multiplatform）

### 1.1 整体技术架构

MoveOff 采用 KMP 架构，将可复用逻辑放在共享层，平台差异能力在桌面端做原生桥接，保证一致交互与系统级能力。

| 分层 | 技术选型 | 职责 |
| --- | --- | --- |
| 共享层（Common） | Kotlin/JVM + Kotlin Coroutines | 核心业务逻辑（SSH 连接、文件传输、进度计算、存储策略）、数据模型、通用工具类 |
| 桌面端 UI 层 | macOS：Compose for Desktop + Swift Interop（右键菜单）<br>Windows：Compose for Desktop + JNA（注册表 / 右键菜单） | 复刻 JetBrains Toolbox 风格面板、进度窗口、配置界面；系统级右键菜单集成 |
| 底层依赖 | 文件传输：sshj（SFTP/SSH） + rsync 封装<br>加密存储：macOS Keychain（Swift）、Windows DPAPI（JNA）<br>进程管理：Kotlin Coroutines + 后台服务<br>挂载远程目录：SSHFS（macOS/Linux）、WinFsp（Windows） | 底层 IO、系统交互、安全存储、后台任务与目录挂载 |

### 1.2 关键依赖说明

| 依赖库 | 用途 | 优势 |
| --- | --- | --- |
| `org.jetbrains.compose.desktop` | 跨平台桌面 UI（Compose for Desktop） | JetBrains 技术栈一致，便于复刻 Toolbox 交互风格，Kotlin 全栈开发 |
| `com.hierynomus:sshj` | SSH/SFTP 客户端 | 跨平台、纯 JVM、无外部二进制依赖，支持细粒度传输监听 |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 协程调度与并发控制 | 非阻塞 IO、UI 无卡顿、任务可取消/暂停/恢复 |
| `net.java.dev.jna:jna` | Windows 系统能力调用 | 注册表写入、托盘能力、DPAPI 调用、右键菜单集成 |
| `com.github.winterreisender:sshfs-jna` | SSHFS 挂载封装 | 提供按需访问基础能力，接近 OneDrive/Toolbox “本地可见、远程实际存储”体验 |

---

## 二、需求升级：JetBrains Toolbox 风格面板 + 服务器文件管理

### 2.1 核心面板设计（复刻 Toolbox 风格）

整体采用「顶部入口 + 左侧导航 + 右侧工作区 + 底部任务栏」四区布局，保持低干扰、可恢复、强反馈。

#### 2.1.1 面板布局（顶部菜单栏 + 左侧导航 + 右侧内容区）

| 区域 | 功能 | 交互参考（Toolbox） |
| --- | --- | --- |
| 顶部菜单栏（系统托盘） | macOS：菜单栏图标 + 下拉面板<br>Windows：系统托盘 + 右键菜单<br>核心入口：打开面板 / 挂载远程目录 / 设置 / 退出 | 点击图标直达主面板，常驻后台、低打扰 |
| 左侧导航栏 | 1）快速迁移<br>2）服务器管理<br>3）文件管理<br>4）迁移记录<br>5）设置 | 极简导航，图标 + 文本，Hover 高亮，选中态清晰 |
| 右侧内容区 | 展示对应业务页，核心为「文件管理面板」 | 卡片式信息布局，优先操作效率与状态反馈 |

#### 2.1.2 服务器托管文件管理面板（核心升级）

| 功能点 | 交互设计 | 技术实现 |
| --- | --- | --- |
| 目录树可视化 | 左侧目录树（按后缀/标签分类），右侧文件列表（名称、大小、上传时间、状态） | SSHJ 遍历远程目录；Compose 树形与虚拟列表渲染 |
| 文件操作 | 下载回本地（带进度）<br>预览（按需挂载）<br>删除（二次确认）<br>重命名/移动（拖拽）<br>标签编辑（同步目录） | SFTP 原子操作 + 协程回调 + 拖拽事件映射 |
| 筛选/搜索 | 按后缀、标签、文件名搜索；按大小/上传时间排序 | 本地缓存元数据 + 远程增量同步 |
| 空间统计 | 展示已用/总空间；按后缀/标签分类占比 | 远程容量信息采集 + Compose 图表组件展示 |

### 2.2 核心交互升级（贴合 Toolbox 体验）

#### 2.2.1 右键菜单 + 面板联动

1. 用户在 Finder/Explorer 多选文件。  
2. 触发 `MoveOff - 释放空间`。  
3. 主面板自动聚焦并切至「迁移确认页」（嵌入右侧内容区，非阻塞弹窗）。  
4. 确认后进入「进度页」，实时展示每文件与总任务进度。  
5. 完成后自动刷新「文件管理」列表并生成迁移记录。

#### 2.2.2 后台任务管理（Toolbox 风格）

- 面板底部显示任务栏：当前任务 + 队列任务。  
- 单任务支持：暂停、恢复、取消、重试。  
- 点击任务展开：进度、速度、剩余时间、失败原因、文件明细。  
- 完成/失败后显示轻量通知（Toast），不打断当前操作。

#### 2.2.3 极简配置流程（首次启动引导）

首次启动提供 3 步配置向导：

1. 添加 SSH 服务器（地址、端口、认证方式）。  
2. 配置远程根目录与存储策略（默认按后缀分类）。  
3. 选择本地处理策略（删除原文件 / 保留快捷方式）。  

完成后自动进入「快速迁移」页，并提示可通过右键或拖拽触发迁移。

---

## 三、核心功能技术实现细节

### 3.1 进度条精准控制（耗时 IO 操作可视化）

基于 Kotlin 协程 + SSHJ 监听实现「分阶段、高精度、可聚合」进度体系，支持单任务与并行任务统一展示。

#### 3.1.1 分阶段进度模型

将一次迁移拆分为可观测阶段，避免“长时间卡在 0%/99%”：

| 阶段 | 含义 | 权重 |
| --- | --- | --- |
| `PRECHECK` | SSH 连接、权限、目标路径预检 | 5% |
| `SCAN_LOCAL` | 本地文件扫描、大小统计、冲突检查 | 10% |
| `TRANSFER` | 实际上传（字节流传输） | 70% |
| `VERIFY` | 远端校验（大小/哈希/可读性） | 10% |
| `FINALIZE` | 元数据写入、索引更新、任务落库 | 5% |

总进度计算公式：

```text
overall = sum(doneStageWeight) + currentStageWeight * currentStageProgress(0..1)
```

#### 3.1.2 Kotlin 参考实现（可直接迁移到 shared 层）

```kotlin
package com.moveoff.progress

import kotlin.math.roundToInt
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TransferStage(val weight: Double) {
    PRECHECK(0.05),
    SCAN_LOCAL(0.10),
    TRANSFER(0.70),
    VERIFY(0.10),
    FINALIZE(0.05)
}

data class StageUpdate(
    val taskId: String,
    val fileName: String,
    val stage: TransferStage,
    val stageProgress: Double,
    val transferredBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L
)

data class TaskProgress(
    val taskId: String,
    val fileName: String,
    val stage: TransferStage,
    val stagePercent: Int,
    val overallPercent: Int,
    val transferredBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val etaSeconds: Long
)

class ProgressTracker {
    private val order = TransferStage.entries
    private val _state = MutableStateFlow<Map<String, TaskProgress>>(emptyMap())
    val state: StateFlow<Map<String, TaskProgress>> = _state.asStateFlow()

    fun update(event: StageUpdate) {
        val normalized = event.stageProgress.coerceIn(0.0, 1.0)
        val completedWeight = order
            .takeWhile { it != event.stage }
            .sumOf { it.weight }

        val overall = ((completedWeight + event.stage.weight * normalized) * 100.0)
            .coerceIn(0.0, 100.0)
            .roundToInt()

        val stagePercent = (normalized * 100.0).roundToInt().coerceIn(0, 100)
        val snapshot = TaskProgress(
            taskId = event.taskId,
            fileName = event.fileName,
            stage = event.stage,
            stagePercent = stagePercent,
            overallPercent = overall,
            transferredBytes = max(0L, event.transferredBytes),
            totalBytes = max(0L, event.totalBytes),
            speedBytesPerSec = max(0L, event.speedBytesPerSec),
            etaSeconds = max(0L, event.etaSeconds)
        )

        _state.value = _state.value.toMutableMap().apply {
            this[event.taskId] = snapshot
        }
    }
}
```

#### 3.1.3 SSHJ 进度事件接入方式

接入策略：将 SSHJ 字节回调统一转为 `StageUpdate`，由 `ProgressTracker` 聚合后推送给 UI。

```kotlin
suspend fun uploadWithProgress(
    emitter: (StageUpdate) -> Unit
) {
    emitter(StageUpdate(taskId, fileName, TransferStage.PRECHECK, 1.0))
    emitter(StageUpdate(taskId, fileName, TransferStage.SCAN_LOCAL, 1.0))

    val total = fileSize
    var transferred = 0L
    val startedAt = System.nanoTime()

    while (transferred < total) {
        val chunk = nextChunkSize()
        transferred = (transferred + chunk).coerceAtMost(total)
        val elapsedSec = ((System.nanoTime() - startedAt) / 1_000_000_000.0).coerceAtLeast(0.001)
        val speed = (transferred / elapsedSec).toLong()
        val remain = (total - transferred).coerceAtLeast(0L)
        val eta = if (speed > 0) remain / speed else 0L

        emitter(
            StageUpdate(
                taskId = taskId,
                fileName = fileName,
                stage = TransferStage.TRANSFER,
                stageProgress = transferred.toDouble() / total.toDouble(),
                transferredBytes = transferred,
                totalBytes = total,
                speedBytesPerSec = speed,
                etaSeconds = eta
            )
        )
    }

    emitter(StageUpdate(taskId, fileName, TransferStage.VERIFY, 1.0))
    emitter(StageUpdate(taskId, fileName, TransferStage.FINALIZE, 1.0))
}
```

> 实际项目中可将 `while` 替换为 SSHJ 的传输监听回调；核心点是保持 `TRANSFER` 阶段按“已传输字节/总字节”实时换算。

#### 3.1.4 多任务总进度聚合

当有并发任务时，建议用「按文件大小加权」而不是简单平均：

```text
queueOverall = Σ(taskOverall * taskTotalBytes) / Σ(taskTotalBytes)
```

这样可避免小文件大量完成导致总进度虚高，保证与真实剩余时间更一致。

#### 3.1.5 UI 展示建议（Compose）

- 主进度条：展示 `overallPercent`。  
- 子进度信息：阶段名、速度、剩余时间、已传输/总大小。  
- 刷新节流：UI 层建议 `100~200ms` 采样一次，既流畅又不过度刷新。  
- 失败态：保留最后快照与错误原因，支持“从失败文件重试”。

---

## 四、MVP 交付范围（建议）

### 4.1 第一阶段（可上线）

1. 右键触发迁移 + 主面板联动。  
2. 单服务器连接管理（密码或密钥）。  
3. 文件上传、下载、删除、重命名。  
4. 分阶段进度条与后台任务栏。  
5. 基础迁移记录与错误重试。

### 4.2 第二阶段（体验增强）

1. 多服务器分组与标签同步。  
2. SSHFS/WinFsp 挂载预览。  
3. 空间统计图与智能清理建议。  
4. 冲突策略（覆盖、跳过、重命名）可配置。  
5. 增量同步与断点续传。

---

## 五、验收标准（针对 3.1）

- 大文件（>2GB）迁移时，进度持续变化，不出现长时间停滞。  
- 进度值单调递增，最终准确收敛到 `100%`。  
- 速度与剩余时间可随网络变化动态调整。  
- 并发任务总进度与实际完成时间误差可控（建议 <10%）。  
- 任务暂停/恢复/取消后，进度状态与 UI 显示一致。

---

如果你愿意，我下一步可以直接补一版 `build.gradle.kts` + KMP 模块骨架，把 `3.1` 的 `ProgressTracker` 变成可运行 demo（含 Compose 进度面板）。
