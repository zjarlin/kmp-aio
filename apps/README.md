# Apps - 业务应用模块

此目录包含所有 KMP Compose Multiplatform 桌面应用模块。

## 目录结构

```
apps/
├── README.md              # 本文件
├── template/              # 应用模板（复制此目录创建新应用）
│   ├── build.gradle.kts   # 构建配置模板
│   └── src/               # 源代码模板
│       └── jvmMain/
│           └── kotlin/
│               └── site/
│                   └── addzero/
│                       └── template/
│                           └── Main.kt
├── vibepocket/            # VibePocket 音乐播放器应用
└── notes/                 # VibeNotes 笔记应用（AI + RAG + 图谱）
    ├── build.gradle.kts
    └── src/
```

## 创建新应用

### 方法 1: 复制模板（推荐）

```bash
# 1. 复制模板目录
cp -r apps/template apps/myapp

# 2. 修改 build.gradle.kts 中的应用配置
#    - appName = "myapp"
#    - appNamespace = "site.addzero.myapp"
#    - 添加业务依赖

# 3. 重命名包目录
mv apps/myapp/src/jvmMain/kotlin/site/addzero/template \
   apps/myapp/src/jvmMain/kotlin/site/addzero/myapp

# 4. 修改 Main.kt 中的包名

# 5. 构建应用
./gradlew :apps:myapp:package
```

### 方法 2: 复制现有应用

```bash
# 复制 vibepocket 作为基础
cp -r apps/vibepocket apps/myapp

# 修改 build.gradle.kts 中的应用名和包名
# 删除不需要的依赖和代码
```

## 打包输出

打包文件输出到各应用的 `build/compose-binaries/` 目录：

```
apps/myapp/build/compose-binaries/
├── main/
│   └── myapp                    # 可执行文件（Linux/Mac）
├── myapp-1.0.0.dmg             # macOS 安装包
├── myapp-1.0.0.msi             # Windows 安装包
└── myapp_1.0.0_amd64.deb       # Linux 安装包
```

## 常用命令

```bash
# 运行指定应用
./gradlew :apps:vibepocket:run

# 运行 VibeNotes
./gradlew :apps:notes:run

# 单独启动 VibeNotes API
./gradlew :apps:notes:server:run

# 打包指定应用（所有平台）
./gradlew :apps:vibepocket:package

# 仅打包 macOS
./gradlew :apps:vibepocket:packageDmg

# 仅打包 Windows
./gradlew :apps:vibepocket:packageMsi

# 仅打包 Linux
./gradlew :apps:vibepocket:packageDeb
```

## 应用配置说明

每个应用的 `build.gradle.kts` 中需要配置：

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `appName` | 应用名，决定打包文件名 | `"vibepocket"` |
| `appNamespace` | 包名 | `"site.addzero.vibepocket"` |
| `appVersion` | 版本号 | `"1.0.0"` |
| `mainClass` | 主类 | `"$appNamespace.MainKt"` |

## 图标配置

在各应用的 `src/jvmMain/resources/` 目录放置图标文件：

- `icon.icns` - macOS 图标
- `icon.ico` - Windows 图标
- `icon.png` - Linux 图标（推荐 512x512）
# 参考资料
https://github.com/CharlesPikachu/musicdl?tab=readme-ov-file
https://github.com/kxzjoker/163music_search 
https://gitee.com/gnnu/yumbo-music-utils
https://github.com/zhixuanziben/NeteaseCloudMusic-vue
https://github.com/GitHub-ZC/wp_MusicApi?tab=readme-ov-file
