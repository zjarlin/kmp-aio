# MoveOff 原生系统集成

## 概述

此目录包含 MoveOff 与操作系统原生集成的代码，包括 Finder/Explorer 右键菜单和文件状态图标。

## macOS - Finder Sync Extension

### 目录结构
```
macos/
└── MoveOffFinderExtension/
    ├── FinderSync.swift          # 主扩展代码
    └── Info.plist                # 扩展配置
```

### 功能
- ✅ 在 Finder 中显示文件同步状态图标（badge）
- ✅ 右键菜单：立即同步、解决冲突、在 MoveOff 中显示、获取共享链接
- ✅ 通过 XPC 与主应用通信

### 构建步骤

1. **创建 Xcode 项目**
```bash
cd native/macos
# 使用 Xcode 创建一个新的 App Extension 项目
# 选择 "Finder Extension" 模板
```

2. **复制代码**
将 `FinderSync.swift` 和 `Info.plist` 复制到 Xcode 项目中

3. **配置 App Group（可选）**
如果需要通过 App Group 共享数据，在 Xcode 中配置 App Group

4. **构建并嵌入**
- 将 Finder Extension 作为子项目添加到主应用
- 在 Build Phases 中添加 "Embed App Extensions"

### 安装/启用

用户需要在 **系统设置 > 扩展 > 添加的扩展** 中手动启用 MoveOff Finder Extension

### XPC 通信

Finder Extension 通过 XPC 与主应用通信：
- Mach Service: `site.addzero.moveoff.xpc`
- 协议: `MoveOffXPCProtocol`

## Windows - Shell Context Menu Extension

### 目录结构
```
windows/
└── MoveOffShellExt/
    ├── MoveOffShellExt.h       # 头文件
    ├── MoveOffShellExt.cpp     # 实现
    └── MoveOffShellExt.def     # 模块定义
```

### 功能
- ✅ 在资源管理器右键菜单中显示 MoveOff 菜单
- ✅ 显示文件同步状态
- ✅ 菜单项：立即同步、解决冲突、在 MoveOff 中显示、获取共享链接

### 构建步骤

1. **使用 Visual Studio 创建项目**
   - 选择 "Dynamic-Link Library (DLL)" 模板
   - 启用 ATL/MFC 支持（可选）

2. **复制代码**
将 `.h`, `.cpp`, `.def` 文件添加到项目中

3. **生成新的 GUID**
使用 Visual Studio 的 "Create GUID" 工具生成新的 GUID，替换代码中的 `CLSID_MoveOffContextMenu`

4. **编译**
选择 Release/x64 配置，生成 `MoveOffShellExt.dll`

### 注册/安装

**手动注册（需要管理员权限）：**
```cmd
regsvr32 MoveOffShellExt.dll
```

**卸载：**
```cmd
regsvr32 /u MoveOffShellExt.dll
```

**通过安装程序自动注册：**
在 Inno Setup 或 WiX 安装包中添加注册步骤：
```pascal
[Run]
Filename: "regsvr32"; Parameters: "/s ""{app}\MoveOffShellExt.dll"""; Flags: waituntilterminated
```

### 图标覆盖（可选）

Windows Shell 扩展还可以实现图标覆盖处理器（Icon Overlay Handler），在文件图标上显示同步状态徽章。

需要实现 `IShellIconOverlayIdentifier` 接口，并注册到：
```
HKLM\Software\Microsoft\Windows\CurrentVersion\Explorer\ShellIconOverlayIdentifiers
```

**注意：** Windows 限制最多 15 个图标覆盖处理器，需要谨慎使用。

## 主应用集成

### Java/Kotlin 端

在 `Main.kt` 中初始化原生集成：

```kotlin
// 启动时初始化
NativeIntegration.initialize()

// 文件状态更新时通知原生扩展
NativeIntegration.updateFileStatus(
    path = "/path/to/file",
    status = FileSyncStatus.SYNCED
)
```

### IPC 通信

**macOS (XPC):**
主应用需要启动 XPC 服务监听 Finder Extension 的请求：

```kotlin
// 启动 XPC 服务
XPCServer.start { message ->
    when (message.action) {
        "GET_FILE_STATUS" -> getFileStatus(message.path)
        "TRIGGER_SYNC" -> triggerSync(message.path)
        "SHOW_IN_APP" -> showInApp(message.path)
    }
}
```

**Windows (命名管道/TCP):**
主应用启动命名管道服务器：

```kotlin
// 启动命名管道服务器
NamedPipeServer.start("\\\\.\\pipe\\MoveOff") { request ->
    handleRequest(request)
}
```

## 待实现

- [ ] macOS: 完整的 XPC 服务实现
- [ ] Windows: 命名管道 IPC 实现
- [ ] Windows: 图标覆盖处理器
- [ ] Linux: Nautilus/Dolphin 扩展
- [ ] 自动构建脚本
