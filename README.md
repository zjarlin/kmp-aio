# kmp-aio - Kotlin Multiplatform Monorepo

kmp-aio 是一个基于 Kotlin Multiplatform 的 Monorepo 项目，采用模块化架构，支持快速复制业务模块创建新应用。

## 项目结构

```
kmp-aio/
├── apps/                      # 业务应用模块（可复制粘贴创建新应用）
│   ├── README.md             # 应用开发指南
│   ├── template/             # 应用模板（复制此目录创建新应用）
│   └── kmp-aio/           # kmp-aio 音乐播放器应用
│
├── lib/                       # 共享库模块
│   ├── glass-components/     # UI 组件库
│   ├── shadcn-ui-kmp/        # shadcn 风格 UI 组件
│   ├── api-*/                # 业务 API 库
│   ├── starter-*/            # Ktor Starter 模块
│   └── ...
│
├── server/                    # Ktor 后端服务
├── shared/                    # 共享代码（KMP）
├── iosApp/                    # iOS 原生入口
├── checkouts/                 # Git 子模块
│   └── build-logic/          # Gradle 构建逻辑
└── openapi-codegen/          # OpenAPI 代码生成
```

## Monorepo 特点

### 1. 应用即模块

每个应用都是 `apps/` 下的独立模块，可复制粘贴快速创建：

```bash
# 创建新应用只需复制模板
cp -r apps/template apps/myapp

# 修改配置
# - apps/myapp/build.gradle.kts 中的 appName, appNamespace
# - 修改包名和主类

# 构建新应用
./gradlew :apps:myapp:package
```

### 2. 打包输出

每个应用独立打包，输出文件使用模块名：

```
apps/myapp/build/compose-binaries/
├── myapp-1.0.0.dmg          # macOS
├── myapp-1.0.0.msi          # Windows
└── myapp_1.0.0_amd64.deb    # Linux
```

### 3. 模块自动发现

项目使用 `modules-buddy` Gradle 插件自动发现模块，无需手动在 `settings.gradle.kts` 中 `include`。

## 快速开始

### 运行 kmp-aio 应用

```bash
# 桌面端（JVM）
./gradlew :apps:kmp-aio:run

# 打包所有平台
./gradlew :apps:kmp-aio:package

# 仅打包 macOS
./gradlew :apps:kmp-aio:packageDmg

# 仅打包 Windows
./gradlew :apps:kmp-aio:packageMsi

# 仅打包 Linux
./gradlew :apps:kmp-aio:packageDeb
```

### 运行后端服务

```bash
./gradlew :server:run
```

### 创建新应用

详见 [apps/README.md](./apps/README.md)

## 技术栈

- **UI**: Compose Multiplatform (Desktop/JVM)
- **后端**: Ktor + Jimmer + Exposed
- **数据库**: PostgreSQL / SQLite
- **依赖注入**: Koin
- **网络**: Ktor Client + Ktorfit
- **构建**: Gradle + Kotlin DSL

## 模块说明

### apps/ - 业务应用

| 模块 | 说明 |
|------|------|
| `apps/template` | 应用模板，复制创建新应用 |
| `apps/kmp-aio` | kmp-aio 音乐播放器 |

### lib/ - 共享库

| 模块 | 说明 |
|------|------|
| `lib/glass-components` | Glassmorphism 风格 UI 组件 |
| `lib/shadcn-ui-kmp` | shadcn 风格 UI 组件 |
| `lib/api-netease` | 网易云音乐 API |
| `lib/api-suno` | Suno AI 音乐 API |
| `lib/starter-*` | Ktor Starter 模块 |

### server/ - 后端服务

Ktor 后端服务，支持独立部署或内嵌到桌面端。

## 开发指南

### 添加新应用

1. 复制 `apps/template` 到 `apps/{your-app-name}`
2. 修改 `build.gradle.kts` 中的 `appName` 和 `appNamespace`
3. 重命名源代码包路径
4. 添加业务依赖
5. 运行 `./gradlew :apps:{your-app-name}:run`

### 添加新库

1. 在 `lib/` 下创建新模块
2. 创建 `build.gradle.kts`
3. 插件会自动发现并注册模块

## 许可证

MIT License
