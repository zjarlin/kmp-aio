# KCloud - 类 Nextcloud 的跨平台同步客户端

> 基于 Kotlin Multiplatform 构建的开源云存储客户端，灵感来自 **Nextcloud/OwnCloud**。支持 WebDAV/S3/SSH 多种存储后端，端到端加密，服务器端零部署，客户端通过本地 SQLite 维护同步状态，实现双向增量同步与冲突检测。
>
> **愿景**：让每个人都能拥有自己的私有云。

---

## 📑 目录

- [核心设计](#一核心设计决策必读)
- [技术架构](#二技术架构)
- [核心同步算法](#三核心同步算法)
- [功能模块](#四功能模块)
  - [同步引擎](#41-同步引擎-syncenginekt)
  - [状态管理](#42-状态管理-appstatekt)
  - [事件总线](#43-事件总线-eventbuskt)
  - [数据库](#44-数据库-databasekt--databaseimplkt)
  - [存储客户端](#45-存储客户端)
    - [S3存储](#s3存储-s3storageclientkt)
    - [SSH存储](#ssh存储-sshstorageclientkt)
    - [故障转移](#故障转移-failoverstorageclientkt)
  - [UI组件](#46-ui组件)
    - [分阶段进度条](#分阶段进度条-stagedprogressindicatorkt)
    - [冲突解决对话框](#冲突解决对话框-conflictresolutiondialogkt)
    - [版本历史](#版本历史-versionhistoryscreenkt)
    - [选择性同步](#选择性同步-selectivesyncscreenkt)
    - [文件去重](#文件去重-deduplicationscreenkt)
    - [P2P传输](#p2p传输-p2ptransferscreenkt)
    - [智能同步策略](#智能同步策略-smartsyncscreenkt)
  - [安全加密](#47-安全加密-encryptionmanagerkt)
  - [自动更新](#48-自动更新-updatecheckerktskipversion)
  - [团队共享](#49-团队共享)
  - [操作审计](#410-操作审计-auditloggerkt)
  - [原生集成](#411-原生集成)
    - [macOS-Finder扩展](#macos-findersyncswift)
    - [Windows-Shell扩展](#windows-shellext)
- [MVP交付计划](#五mvp-交付计划)
- [代码结构](#八代码结构已落地)
- [待决策事项](#九待决策事项)

---

## 初衷：为什么要有这个工具？

你有没有想过，有些东西**本该只存在一份**？

就像单例模式一样——Dotfiles（`.gitignore`、`.claude/`、`.codex/`）、构建脚本（`build-logic/`、`build.gradle.kts`、`pom.xml`）、IDE配置、Shell脚本……这些配置文件和工具，现在是怎么管理的？**复制、粘贴、再复制。** 每换一台电脑，重新设置一遍；每个新项目，把构建逻辑复制一份；每次升级或写法变化，各个项目之间根本无法保持"同步"。

这不是重复劳动，这是**重复劳动的平方**。

所以有了这个问题：**有没有终极的解决方案，让这些文件自动出现在它们该出现的地方？**

KCloud 的回答是：**只存一份，但处处可用。** 用同步代替复制，用统一代替分散。你的配置、你的脚本、你的工作流，只维护一个源头，然后让它们像单例一样，在你所有设备、所有项目中自动就位。

这不是一个文件同步工具那么简单——这是**对重复劳动的终极反抗**。

### 更深层的思考：元数据统一 + 环境变换

如果把问题抽象一层，会发现一个模式：**元数据（语义）是单例，表现形式（路径、语法）是变换。**

就像函数式编程中的 `map` 和 `filter`——源数据只有一份，但通过不同的变换函数，得到不同环境下的具体实现：

| 概念（元数据/单例） | macOS/Linux 路径 | Windows 路径 | 内容变换策略 |
|-------------------|-----------------|--------------|-------------|
| Shell 配置文件 | `~/.zshrc` / `~/.bashrc` | `%USERPROFILE%\Documents\PowerShell\Microsoft.PowerShell_profile.ps1` | 语法转换：bash函数 → PowerShell函数 |
| Git 全局配置 | `~/.gitconfig` | `%USERPROFILE%\.gitconfig` | 路径分隔符、换行符转换 |
| IDE 配置 | `~/.config/JetBrains/` | `%APPDATA%\JetBrains\` | 路径变量替换 |
| 环境变量/函数 | `~/.zshenv` / `~/.bashrc` 中的 `export` | `$PROFILE` (Microsoft.PowerShell_profile.ps1) 中的 `$env:` 和 `function` 定义 | key=value 统一存储，按需生成对应语法 |
| SSH 密钥 | `~/.ssh/` | `%USERPROFILE%\.ssh\` | 权限模式变换（600 → ACL） |
| 构建脚本模板 | `build-logic/` | 同上 | 无变换，纯同步 |

**核心洞察**：
- **Filter**: 某些文件只在特定 OS/环境出现（如 `.DS_Store` 只在 macOS，Windows 注册表脚本只在 Windows）
- **Map**: 同一份逻辑，在不同环境有不同的语法/路径表现
- **Reduce**: 合并多个源（全局配置 + 项目配置 + 本地覆盖）生成最终配置

**这对 KCloud 的架构指导意义**：

1. **存储层**：云端不存原始文件，而是存**语义化的元数据**（统一格式）
2. **变换引擎**：同步时根据目标环境执行 `path_transform()` 和 `content_transform()`
3. **插件系统**：允许用户定义自己的变换规则（如把 `.env` 同步到 Windows 注册表特定键）
4. **冲突解决**：不再是"选 A 还是选 B"，而是"如何合并两个元数据变更"
5. **版本历史**：回滚的不是文件内容，而是**配置状态的快照**

KCloud 的最终形态：**像 Nextcloud 一样易用，像 Syncthing 一样去中心化**——你拥有完全的数据控制权，无需信任任何第三方云服务。

**📖 架构文档**：
- [UI架构设计](docs/UI_ARCHITECTURE.md) - 系统托盘、Compose UI、窗口管理完整设计

---

## 一、核心设计决策（必读）

### 1.1 为什么必须有本地数据库？

**我必须直接告诉你**：声称"不需要本地数据库"的同步工具要么功能受限，要么隐藏了实现（如用文件属性/xattr）。双向同步的本质是**检测变化的方向**，这需要"上次同步时的状态"作为基准。

| 场景 | 无数据库的问题 | 有数据库的解决方案 |
|-----|--------------|------------------|
| 你修改了文件A | 不知道这是"修改"还是"新建" | DB记录了旧mtime/size，对比即知变化 |
| 远程也有更新 | 无法判断冲突 | DB记录远程ETag，对比发现两边都变 |
| 你删除了文件B | 无法区分"本地删除"还是"远程新增" | DB有B的记录但本地不存在=本地删除 |
| 离线后恢复 | 无法知道离线期间发生了什么 | DB队列记录待同步操作 |

**结论**：本地SQLite必需，但只存元数据（路径、哈希、时间戳），不存文件内容。

### 1.2 S3 vs SSH 的权衡

| 维度 | S3（优先） | SSH（备用） |
|-----|-----------|------------|
| **服务器要求** | 只需S3兼容存储（MinIO/OSS/COS），服务器零部署 | 需sshd运行，可用rsync增量 |
| **并发写入** | 依赖S3版本ID + 客户端冲突检测 | sftp原子操作，但无全局锁 |
| **变更通知** | 需轮询ListObjects或S3 Event→SNS→客户端 | 无原生通知，需轮询 |
| **增量同步** | 基于ETag/Last-Modified对比 | rsync的块级增量，更高效 |
| **大文件** | 分片上传（Multipart） | sftp直接流传输 |
| **成本** | API调用次数计费 | 只耗流量 |

**策略**：S3为默认协议，SSH用于局域网高速传输或S3不可用时降级。

---

## 二、技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端（每太机器）                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │ Compose UI  │  │ 同步引擎     │  │ 状态管理     │  │ 系统集成 │ │
│  │ - 主面板    │◄─┤ - 差异检测   │◄─┤ - SQLite    │◄─┤ - 右键菜单│ │
│  │ - 进度窗口  │  │ - 上传下载   │  │ - 待同步队列  │  │ - 状态图标│ │
│  │ - 冲突解决  │  │ - 冲突处理   │  │ - 历史版本   │  │ - 托盘   │ │
│  └─────────────┘  └──────┬──────┘  └─────────────┘  └────┬────┘ │
│                          │                                │      │
└──────────────────────────┼────────────────────────────────┼──────┘
                           │                                │
                    S3 API │ HTTPS                         │ JNA/Swift
                           ▼                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        存储层                                    │
│  ┌─────────────────┐           ┌─────────────────────────────┐  │
│  │ S3兼容对象存储   │           │ 可选：SSH/SFTP服务器         │  │
│  │ - 文件内容       │           │ - rsync增量传输              │  │
│  │ - 版本控制       │           │ - 直接文件系统访问            │  │
│  └─────────────────┘           └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 同步状态机

```
                    ┌──────────┐
    本地修改 ──────►│ 待上传   │──────► 上传中 ──────► 已同步 ◄──────┐
                    │ (local)  │         │            │   │        │
                    └──────────┘         │            │   │        │
                                         ▼            │   ▼        │
                                      上传失败 ────────┘  远程修改 ──┤
                                                         (冲突)     │
                                          ▲                        │
                                          └────────── 冲突解决 ─────┘

    远程修改 ──────► 待下载 ──────► 下载中 ──────► 已同步
    (检测到)         (remote)
```

### 2.3 本地SQLite Schema

```sql
-- 文件元数据表（核心）
CREATE TABLE files (
    id INTEGER PRIMARY KEY,
    path TEXT UNIQUE NOT NULL,           -- 相对路径
    local_mtime INTEGER,                  -- 本地修改时间
    local_size INTEGER,
    local_hash TEXT,                      -- SHA-256内容哈希

    remote_etag TEXT,                     -- S3 ETag或远程标识
    remote_version_id TEXT,               -- S3版本ID（用于冲突检测）
    remote_mtime INTEGER,                 -- 服务器上的修改时间
    remote_size INTEGER,

    sync_state TEXT,                      -- synced/pending_upload/pending_download/conflict
    last_sync_time INTEGER,               -- 上次成功同步时间
    conflict_strategy TEXT                -- local_wins/remote_wins/keep_both
);

-- 待同步队列（支持断点续传）
CREATE TABLE sync_queue (
    id INTEGER PRIMARY KEY,
    file_id INTEGER REFERENCES files(id),
    operation TEXT,                       -- upload/download/delete
    status TEXT,                          -- pending/running/paused/failed
    progress_bytes INTEGER,               -- 已传输字节（断点续传）
    total_bytes INTEGER,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at INTEGER,
    updated_at INTEGER
);

-- 同步历史（用于审计和撤销）
CREATE TABLE sync_history (
    id INTEGER PRIMARY KEY,
    operation TEXT,
    path TEXT,
    details TEXT,
    success BOOLEAN,
    timestamp INTEGER
);
```

---

## 三、核心同步算法

### 3.1 增量检测流程

**源码**: [`SyncEngine.kt`](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt)

```kotlin
class SyncEngine(
    private val db: Database,
    private val s3: S3Client,
    private val localFs: LocalFileSystem
) {
    suspend fun detectChanges(): SyncPlan {
        val plan = SyncPlan()

        // 1. 扫描本地文件系统，与DB对比
        localFs.walkSyncDir().forEach { localFile ->
            val record = db.getFile(localFile.path)
            when {
                record == null -> plan.toUpload.add(localFile) // 新增
                record.local_mtime != localFile.mtime ||
                    record.local_size != localFile.size -> {
                    // 可能修改，需计算哈希确认
                    if (record.local_hash != localFile.computeHash()) {
                        plan.toUpload.add(localFile)
                    }
                }
            }
        }

        // 2. 检测本地删除（DB中有记录但文件不存在）
        db.getAllFiles().forEach { record ->
            if (!localFs.exists(record.path) && record.sync_state == "synced") {
                plan.toDeleteRemote.add(record)
            }
        }

        // 3. 获取远程变更（带分页，支持大目录）
        val remoteFiles = s3.listAllObjects(prefix = db.syncRoot)
        remoteFiles.forEach { remote ->
            val record = db.getFile(remote.key)
            when {
                record == null -> plan.toDownload.add(remote) // 远程新增
                record.remote_etag != remote.etag ||
                record.remote_version_id != remote.versionId -> {
                    // 远程修改，检查本地是否也修改
                    if (record.local_mtime > record.last_sync_time) {
                        plan.conflicts.add(Conflict(record, remote))
                    } else {
                        plan.toDownload.add(remote)
                    }
                }
            }
        }

        // 4. 检测远程删除
        val remotePaths = remoteFiles.map { it.key }.toSet()
        db.getAllFiles().forEach { record ->
            if (record.path !in remotePaths && record.sync_state == "synced") {
                plan.toDeleteLocal.add(record)
            }
        }

        return plan
    }
}
```

### 3.2 冲突解决策略

**源码**: [`ConflictResolutionDialog.kt`](src/jvmMain/kotlin/com/moveoff/ui/components/ConflictResolutionDialog.kt) | [`SyncEngine.resolveConflict()`](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt)

```kotlin
sealed class ConflictResolution {
    data class UseLocal(val backupRemote: Boolean = true) : ConflictResolution()
    data class UseRemote(val backupLocal: Boolean = true) : ConflictResolution()
    object KeepBoth : ConflictResolution()
    class Merge(val merger: FileMerger) : ConflictResolution() // 文本文件尝试合并
}

suspend fun resolveConflict(
    conflict: Conflict,
    strategy: ConflictStrategy
): ConflictResolution {
    return when (strategy) {
        ConflictStrategy.AUTO -> {
            // 启发式策略：
            // 1. 如果一方是删除，优先保留存在的一方
            // 2. 文本文件尝试三路合并（如果有共同祖先）
            // 3. 否则使用timestamp大的
            autoResolve(conflict)
        }
        ConflictStrategy.LOCAL_WINS -> ConflictResolution.UseLocal()
        ConflictStrategy.REMOTE_WINS -> ConflictResolution.UseRemote()
        ConflictStrategy.KEEP_BOTH -> ConflictResolution.KeepBoth
        ConflictStrategy.ASK_USER -> showConflictDialog(conflict)
    }
}
```

### 3.3 大文件分片与断点续传

**源码**: [`S3StorageClient.kt`](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt) (multipartUpload方法) | [`SSHStorageClient.kt`](src/jvmMain/kotlin/com/moveoff/storage/SSHStorageClient.kt) (ProgressTransferListener)

```kotlin
class MultipartUploader(
    private val s3: S3Client,
    private val chunkSize: Long = 8 * 1024 * 1024 // 8MB
) {
    suspend fun uploadWithResume(
        file: File,
        remotePath: String,
        progress: (Progress) -> Unit
    ) {
        // 检查是否有未完成的分片上传
        val existingUpload = db.getPendingMultipart(remotePath)

        val uploadId = existingUpload?.uploadId
            ?: s3.initiateMultipartUpload(remotePath)

        val completedParts = existingUpload?.completedParts ?: mutableListOf()
        val totalParts = (file.size + chunkSize - 1) / chunkSize

        for (partNum in 1..totalParts) {
            if (partNum in completedParts) continue

            val etag = s3.uploadPart(
                uploadId = uploadId,
                partNumber = partNum,
                data = file.readChunk(partNum, chunkSize)
            )

            completedParts.add(CompletedPart(partNum, etag))
            db.saveMultipartProgress(remotePath, uploadId, completedParts)

            progress(Progress(partNum, totalParts, partNum * chunkSize, file.size))
        }

        s3.completeMultipartUpload(uploadId, completedParts)
        db.clearMultipartProgress(remotePath)
    }
}
```

---

## 四、系统级集成

### 4.1 状态图标（复刻坚果云）

**源码**:
- macOS: [`FinderSync.swift`](native/macos/MoveOffFinderExtension/FinderSync.swift) (badgeIdentifier)
- Windows: [`MoveOffIconOverlay.cpp`](native/windows/MoveOffIconOverlay/) (IShellIconOverlayIdentifier)
- Linux: [`moveoff_extension.py`](native/linux/nautilus/moveoff_extension.py)

| 图标 | 含义 | 技术实现 |
|-----|-----|---------|
| ✓ 绿色 | 已同步 | macOS: Finder Sync Extension + xattr标记；Windows: Shell Icon Overlay |
| ↻ 蓝色 | 同步中 | 实时更新，通过本地socket与主进程通信 |
| ⚠ 黄色 | 等待同步 | 队列中待处理 |
| ✕ 红色 | 冲突/错误 | 需要用户处理 |
| ☁ 灰色 | 仅云端有（按需下载）| 本地只有占位符 |

### 4.2 右键菜单集成

**源码**:
- macOS: [`FinderSync.swift`](native/macos/MoveOffFinderExtension/FinderSync.swift) (menu方法)
- Windows: [`MoveOffShellExt.cpp`](native/windows/MoveOffShellExt/MoveOffShellExt.cpp) (IContextMenu)
- Linux: [`moveoff_extension.py`](native/linux/nautilus/moveoff_extension.py)

```kotlin
// macOS: Finder Sync Extension (Swift)
class FinderSync: FIFinderSync {
    override func menu(for menuKind: FIMenuKind) -> NSMenu {
        let menu = NSMenu()
        menu.addItem(withTitle: "KCloud - 立即同步", action: #selector(syncNow))
        menu.addItem(withTitle: "KCloud - 释放本地空间", action: #selector(evictLocal))
        menu.addItem(withTitle: "KCloud - 查看版本历史", action: #selector(showHistory))
        return menu
    }
}

// Windows: 注册表 + COM Shell Extension
// HKEY_CLASSES_ROOT\*\shell\MoveOff
// HKEY_CLASSES_ROOT\Directory\shell\MoveOff
```

---

## 五、MVP 交付计划

### 阶段1：基础同步（核心）
- [x] S3连接配置（支持MinIO/阿里云OSS/AWS）→ [`S3StorageClient.kt`](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt) [`S3Config`](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt#L15)
- [x] 本地SQLite元数据管理 → [`Database.kt`](src/commonMain/kotlin/com/moveoff/db/Database.kt) [`DatabaseImpl.kt`](src/jvmMain/kotlin/com/moveoff/db/DatabaseImpl.kt)
- [x] 基础增量同步算法（upload/download/delete）→ [`SyncEngine.kt`](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt)
- [x] 简单的冲突检测（timestamp-based）→ [`SyncEngine.kt`](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt#L548)
- [x] 系统托盘 + 基础UI面板 → [`EnhancedTrayManager.kt`](src/jvmMain/kotlin/com/moveoff/system/EnhancedTrayManager.kt) [`MainWindow.kt`](src/jvmMain/kotlin/com/moveoff/ui/MainWindow.kt)

### 阶段2：用户体验
- [x] [分阶段进度条](src/jvmMain/kotlin/com/moveoff/ui/components/StagedProgressIndicator.kt)（扫描/传输/验证）
- [x] [大文件分片上传](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt)/[断点续传](src/jvmMain/kotlin/com/moveoff/storage/SSHStorageClient.kt)
- [x] Finder/Explorer[状态图标](native/macos/MoveOffFinderExtension/FinderSync.swift)
- [x] [右键菜单集成](native/windows/MoveOffShellExt/MoveOffShellExt.cpp)
- [x] [冲突解决对话框](src/jvmMain/kotlin/com/moveoff/ui/components/ConflictResolutionDialog.kt)

### 阶段3：高级功能
- [x] [版本历史](src/commonMain/kotlin/com/moveoff/version/VersionHistory.kt)（基于S3版本控制）
- [x] [选择性同步](src/commonMain/kotlin/com/moveoff/sync/SyncFilter.kt)（忽略某些文件/目录）
- [x] [局域网P2P传输](src/commonMain/kotlin/com/moveoff/p2p/P2PManager.kt)（同一网络下直连）
- [x] [SSH备用协议](src/jvmMain/kotlin/com/moveoff/storage/SSHStorageClient.kt)
- [x] [文件去重](src/commonMain/kotlin/com/moveoff/dedup/DeduplicationManager.kt)（基于内容哈希）

### 阶段4：企业级
- [x] [E2E加密](src/commonMain/kotlin/com/moveoff/security/EncryptionManager.kt)（客户端加密后上传）
- [x] [团队共享空间](src/commonMain/kotlin/com/moveoff/team/TeamSpaceManager.kt) - 成员管理、权限控制
- [x] [操作审计日志](src/commonMain/kotlin/com/moveoff/audit/AuditLogger.kt) - 操作记录、审计查询
- [x] [智能同步策略](src/commonMain/kotlin/com/moveoff/sync/SmartSyncManager.kt)（按网络类型调整）
- [x] [局域网P2P传输](src/commonMain/kotlin/com/moveoff/p2p/P2PManager.kt) - mDNS发现、直连传输

---

## 六、竞品技术参考

### 坚果云
- 协议：WebDAV + 私有扩展
- 冲突：基于文件版本号
- 锁机制：上传前获取文件锁

### Dropbox
- 协议：私有二进制协议
- 同步：块级去重（4MB块），只传变化的块
- 数据库：本地SQLite存储文件哈希索引

### Syncthing（开源）
- 协议：Block Exchange Protocol v1
- 发现：全球发现服务器 + 本地广播
- 同步：无中心服务器，纯P2P

### rclone
- 策略：mtime或checksum对比
- 优势：支持40+种存储后端
- 缺点：无实时同步，需定时任务

---

## 七、风险与应对

| 风险 | 影响 | 应对策略 |
|-----|-----|---------|
| S3 ListObjects在大目录下慢 | 首次同步或全量检测卡顿 | 1. 分页加载 + 并发；2. 使用S3 Inventory；3. 本地缓存目录树 |
| 多客户端并发写入冲突 | 数据丢失 | 1. S3版本控制必开；2. 客户端冲突检测；3. 文件级锁（用S3对象锁）|
| 本地DB损坏 | 丢失同步状态，可能重复上传/下载 | 1. 定期备份DB到S3；2. DB损坏时全量对比重建 |
| 网络中断 | 同步中断 | 1. 断点续传；2. 指数退避重试；3. 离线队列 |

---

## 八、代码结构（已落地）

```
apps/moveoff/
├── src/commonMain/kotlin/com/moveoff/     # 共享业务逻辑
│   ├── state/
│   │   └── [AppState.kt](src/commonMain/kotlin/com/moveoff/state/AppState.kt)                    # 全局状态管理（SyncStatus、AppStateManager）
│   ├── event/
│   │   └── [EventBus.kt](src/commonMain/kotlin/com/moveoff/event/EventBus.kt)                    # 全局事件总线（UIEvent、EventBus、EventShortcuts）
│   ├── progress/
│   │   └── [ProgressTracker.kt](src/commonMain/kotlin/com/moveoff/progress/ProgressTracker.kt)             # 进度追踪
│   ├── sync/
│   │   ├── [SyncEngine.kt](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt)                  # 同步引擎核心
│   │   ├── [SyncFilter.kt](src/commonMain/kotlin/com/moveoff/sync/SyncFilter.kt)                  # 选择性同步过滤器
│   │   └── [api/StorageClient.kt](src/commonMain/kotlin/com/moveoff/sync/api/StorageClient.kt)             # 存储客户端接口
│   ├── model/
│   │   └── [Models.kt](src/commonMain/kotlin/com/moveoff/model/Models.kt)                      # 数据模型
│   ├── db/
│   │   └── [Database.kt](src/commonMain/kotlin/com/moveoff/db/Database.kt)                    # 数据库接口
│   ├── storage/
│   │   └── [SettingsStorage.kt](src/commonMain/kotlin/com/moveoff/storage/SettingsStorage.kt)             # 设置存储
│   ├── security/
│   │   ├── [EncryptionManager.kt](src/commonMain/kotlin/com/moveoff/security/EncryptionManager.kt)        # 端到端加密
│   │   └── [KeyStoreManager.kt](src/jvmMain/kotlin/com/moveoff/security/KeyStoreManager.kt)              # 密钥存储（JVM）
│   ├── version/
│   │   └── [VersionHistory.kt](src/commonMain/kotlin/com/moveoff/version/VersionHistory.kt)              # 版本历史管理
│   ├── dedup/
│   │   └── [DeduplicationManager.kt](src/commonMain/kotlin/com/moveoff/dedup/DeduplicationManager.kt)          # 文件去重管理
│   ├── p2p/
│   │   └── [P2PManager.kt](src/commonMain/kotlin/com/moveoff/p2p/P2PManager.kt)                    # 局域网P2P传输
│   ├── team/
│   │   └── [TeamSpaceManager.kt](src/commonMain/kotlin/com/moveoff/team/TeamSpaceManager.kt)              # 团队共享空间
│   └── audit/
│       └── [AuditLogger.kt](src/commonMain/kotlin/com/moveoff/audit/AuditLogger.kt)                 # 操作审计日志
│
├── src/jvmMain/kotlin/com/moveoff/        # 桌面端实现
│   ├── system/
│   │   ├── [EnhancedTrayManager.kt](src/jvmMain/kotlin/com/moveoff/system/EnhancedTrayManager.kt)         # 增强系统托盘
│   │   ├── [WindowManager.kt](src/jvmMain/kotlin/com/moveoff/system/WindowManager.kt)               # 窗口管理器
│   │   ├── [GlobalShortcutManager.kt](src/jvmMain/kotlin/com/moveoff/system/GlobalShortcutManager.kt)       # 全局快捷键
│   │   └── [TrayManager.kt](src/jvmMain/kotlin/com/moveoff/system/TrayManager.kt)                 # 基础托盘（旧）
│   ├── ui/
│   │   ├── [MainWindow.kt](src/jvmMain/kotlin/com/moveoff/ui/MainWindow.kt)                  # 主窗口UI
│   │   ├── screens/                       # 各页面屏幕
│   │   └── theme/                         # 主题配置
│   ├── ui/components/
│   │   ├── [FileManagerComponents.kt](src/jvmMain/kotlin/com/moveoff/ui/components/FileManagerComponents.kt)       # 文件管理器组件
│   │   ├── [DragAndDropComponents.kt](src/jvmMain/kotlin/com/moveoff/ui/components/DragAndDropComponents.kt)       # 拖拽上传
│   │   ├── [StagedProgressIndicator.kt](src/jvmMain/kotlin/com/moveoff/ui/components/StagedProgressIndicator.kt)     # 分阶段进度条
│   │   └── [ConflictResolutionDialog.kt](src/jvmMain/kotlin/com/moveoff/ui/components/ConflictResolutionDialog.kt)    # 冲突解决对话框
│   ├── ui/screens/
│   │   ├── [FileManagerScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/FileManagerScreen.kt)           # 文件管理器屏幕
│   │   ├── [VersionHistoryScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/VersionHistoryScreen.kt)        # 版本历史屏幕
│   │   ├── [SelectiveSyncScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/SelectiveSyncScreen.kt)         # 选择性同步屏幕
│   │   ├── [DeduplicationScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/DeduplicationScreen.kt)         # 文件去重屏幕
│   │   ├── [P2PTransferScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/P2PTransferScreen.kt)           # P2P传输屏幕
│   │   └── [SmartSyncScreen.kt](src/jvmMain/kotlin/com/moveoff/ui/screens/SmartSyncScreen.kt)             # 智能同步设置屏幕
│   ├── db/
│   │   └── [DatabaseImpl.kt](src/jvmMain/kotlin/com/moveoff/db/DatabaseImpl.kt)                # SQLite实现
│   ├── storage/
│   │   ├── [S3StorageClient.kt](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt)             # S3客户端
│   │   └── [SSHStorageClient.kt](src/jvmMain/kotlin/com/moveoff/storage/SSHStorageClient.kt)            # SSH/SFTP客户端
│   ├── server/
│   │   └── [LocalServer.kt](src/jvmMain/kotlin/com/moveoff/server/LocalServer.kt)                 # 本地HTTP服务器
│   ├── update/
│   │   └── [UpdateChecker.kt](src/jvmMain/kotlin/com/moveoff/update/UpdateChecker.kt)               # 自动更新
│   └── [Main.kt](src/jvmMain/kotlin/com/moveoff/Main.kt)                            # 应用入口
│
├── native/
│   ├── macos/                             # macOS原生扩展
│   │   └── MoveOffFinderExtension/
│   │       └── [FinderSync.swift](native/macos/MoveOffFinderExtension/FinderSync.swift)          # Finder Sync Extension
│   ├── windows/                           # Windows原生扩展
│   │   ├── MoveOffShellExt/
│   │   │   ├── [MoveOffShellExt.cpp](native/windows/MoveOffShellExt/MoveOffShellExt.cpp)      # 右键菜单扩展
│   │   │   └── [MoveOffShellExt.h](native/windows/MoveOffShellExt/MoveOffShellExt.h)        # 头文件
│   │   └── MoveOffIconOverlay/            # 图标覆盖（状态徽章）
│   ├── linux/                             # Linux原生扩展
│   │   └── nautilus/
│   │       └── [moveoff_extension.py](native/linux/nautilus/moveoff_extension.py)       # Nautilus扩展
│   └── [README.md](native/README.md)                          # 原生扩展编译说明
└── docs/
    └── UI_ARCHITECTURE.md                 # UI架构设计文档
```

---

## 九、待决策事项

1. **冲突默认策略**：你倾向于自动解决（保留较新的）还是总是询问用户？
2. **版本历史保留期**：S3版本控制保留多少天？这直接影响存储成本。
3. **是否支持"云盘模式"（按需下载）**：像OneDrive那样，本地只显示占位符，双击才下载？
4. **加密需求**：端到端加密会增加复杂度和CPU消耗，是否需要？

---

**已完成功能**：

**UI层**：
- ✅ [全局状态管理（AppStateManager）](src/commonMain/kotlin/com/moveoff/state/AppState.kt)
- ✅ [事件总线（EventBus）](src/commonMain/kotlin/com/moveoff/event/EventBus.kt)
- ✅ [增强系统托盘（EnhancedTrayManager）](src/jvmMain/kotlin/com/moveoff/system/EnhancedTrayManager.kt)
- ✅ [窗口管理器（WindowManager）](src/jvmMain/kotlin/com/moveoff/system/WindowManager.kt)
- ✅ [悬浮进度窗口（FloatingProgressWindow）](src/jvmMain/kotlin/com/moveoff/system/WindowManager.kt#L227)
- ✅ [Toast通知（ToastManager）](src/jvmMain/kotlin/com/moveoff/system/WindowManager.kt#L419)
- ✅ [冲突解决窗口（ConflictResolutionWindow）](src/jvmMain/kotlin/com/moveoff/ui/components/ConflictResolutionDialog.kt)
- ✅ [分阶段进度条（StagedProgressIndicator）](src/jvmMain/kotlin/com/moveoff/ui/components/StagedProgressIndicator.kt)
- ✅ [文件管理器（FileManager）](src/jvmMain/kotlin/com/moveoff/ui/screens/FileManagerScreen.kt) - 列表/网格视图、状态图标、右键菜单
- ✅ [设置面板（SettingsScreen）](src/jvmMain/kotlin/com/moveoff/ui/screens/SettingsScreen.kt) - S3配置、同步策略、主题设置

**数据层**：
- ✅ [SQLite数据库（DatabaseImpl）](src/jvmMain/kotlin/com/moveoff/db/DatabaseImpl.kt) - files表、sync_queue表
- ✅ [文件记录CRUD操作](src/commonMain/kotlin/com/moveoff/db/Database.kt)
- ✅ [同步队列管理](src/commonMain/kotlin/com/moveoff/db/Database.kt#L166)
- ✅ [统计查询](src/commonMain/kotlin/com/moveoff/db/Database.kt#L222)

**同步层**：
- ✅ [同步引擎（SyncEngine）](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt)
- ✅ [本地文件系统扫描](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt#L573)
- ✅ [远程变化检测](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt#L206)
- ✅ [冲突检测算法](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt#L548)
- ✅ [同步计划生成与执行](src/commonMain/kotlin/com/moveoff/sync/SyncEngine.kt#L350)
- ✅ [S3存储客户端](src/jvmMain/kotlin/com/moveoff/storage/S3StorageClient.kt)（含分片上传）
- ✅ [SSH/SFTP存储客户端](src/jvmMain/kotlin/com/moveoff/storage/SSHStorageClient.kt)
- ✅ [故障转移客户端（FailoverStorageClient）](src/commonMain/kotlin/com/moveoff/sync/FailoverStorageClient.kt)

**服务层**：
- ✅ [本地HTTP服务器（LocalServer）](src/jvmMain/kotlin/com/moveoff/server/LocalServer.kt)
- ✅ RESTful API（/api/sync/*, /api/files, /api/conflicts）
- ✅ WebSocket实时推送

**交互功能**：
- ✅ [全局快捷键（GlobalShortcutManager）](src/jvmMain/kotlin/com/moveoff/system/GlobalShortcutManager.kt) - Cmd/Ctrl+Shift+M/S/P
- ✅ [拖拽上传](src/jvmMain/kotlin/com/moveoff/ui/components/DragAndDropComponents.kt) - 支持文件/文件夹拖入
- ✅ [原生系统集成框架](native/) - Finder/Explorer右键菜单

**安全功能**：
- ✅ [端到端加密（EncryptionManager）](src/commonMain/kotlin/com/moveoff/security/EncryptionManager.kt) - AES-256-GCM
- ✅ [密钥安全存储（KeyStoreManager）](src/jvmMain/kotlin/com/moveoff/security/KeyStoreManager.kt) - PKCS12密钥库
- ✅ [加密存储包装器（EncryptedStorageClient）](src/commonMain/kotlin/com/moveoff/security/EncryptionManager.kt#L238)

**自动更新**：
- ✅ [自动更新检查器（UpdateChecker）](src/jvmMain/kotlin/com/moveoff/update/UpdateChecker.kt) - 版本检查、下载、安装
- ✅ [跳过版本持久化（skipVersion）](src/jvmMain/kotlin/com/moveoff/update/UpdateChecker.kt#L414)

**版本控制**：
- ✅ [版本历史管理器（VersionHistoryManager）](src/commonMain/kotlin/com/moveoff/version/VersionHistory.kt) - 版本列表、恢复、清理
- ✅ [版本历史UI（VersionHistoryScreen）](src/jvmMain/kotlin/com/moveoff/ui/screens/VersionHistoryScreen.kt) - 版本列表界面

**选择性同步**：
- ✅ [同步过滤器（SyncFilter）](src/commonMain/kotlin/com/moveoff/sync/SyncFilter.kt) - 忽略规则（类似.gitignore）
- ✅ [选择性同步UI（SelectiveSyncScreen）](src/jvmMain/kotlin/com/moveoff/ui/screens/SelectiveSyncScreen.kt) - 规则配置界面

**文件去重**：
- ✅ [去重管理器（DeduplicationManager）](src/commonMain/kotlin/com/moveoff/dedup/DeduplicationManager.kt) - 重复文件检测
- ✅ [去重UI（DeduplicationScreen）](src/jvmMain/kotlin/com/moveoff/ui/screens/DeduplicationScreen.kt) - 去重操作界面

