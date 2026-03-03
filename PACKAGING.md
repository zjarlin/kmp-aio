# VibePocket 打包指南

## 目录
1. [Server 后端打包](#server-后端打包)
2. [ComposeApp 客户端打包](#composeapp-客户端打包)
3. [Docker 部署](#docker-部署)
4. [CI/CD 自动打包](#cicd-自动打包)

---

## Server 后端打包

### 本地构建 JAR
```bash
# 构建 Server JAR（位于 server/build/libs/）
./gradlew :server:build

# 运行 JAR
java -jar server/build/libs/server.jar
```

### 使用 Docker Compose 部署（无需手动构建 JAR）
```bash
cd server

# 1. 复制环境变量模板
cp .env.example .env
# 编辑 .env 填入实际配置

# 2. 构建并启动（需要先本地构建 JAR）
./gradlew :server:build
docker-compose up -d

# 3. 或者带数据库一起启动
docker-compose --profile with-db up -d
```

---

## ComposeApp 客户端打包

ComposeApp 是 Kotlin Multiplatform 项目，支持以下平台：

### 1. Desktop 桌面端
```bash
# 自动检测当前平台并打包
./gradlew :composeApp:packageDistributionForCurrentOS

# 输出目录
composeApp/build/compose/binaries/main/

# 打包产物：
# - Linux: .deb, .rpm, .tar.gz
# - macOS: .dmg, .pkg
# - Windows: .msi, .exe
```

### 2. Android APK
```bash
# 调试版 APK
./gradlew :composeApp:assembleDebug

# 发布版 APK（需要签名配置）
./gradlew :composeApp:assembleRelease

# 输出目录
composeApp/build/outputs/apk/
```

### 3. iOS（仅限 macOS）
```bash
# 构建 iOS 框架
./gradlew :composeApp:compileKotlinIosArm64

# 注意：完整 iOS 应用需要 Xcode 项目配置
# 详见：https://kotlinlang.org/docs/multiplatform-mobile-create-first-application.html
```

---

## Docker 部署

### 快速启动（使用预构建镜像）
```bash
cd server

# 创建配置
cp .env.example .env
# 编辑 .env

# 启动服务
docker-compose up -d
```

### 常用命令
```bash
# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启
docker-compose restart

# 更新（拉取新镜像后）
docker-compose pull && docker-compose up -d
```

---

## CI/CD 自动打包

项目已配置 GitHub Actions，**提交即自动打包**。

### 触发条件

| 事件 | 行为 |
|------|------|
| 推送到 `master`/`main` | 构建并保存 artifact 7 天 |
| 推送到 `develop` | 构建并保存 artifact 7 天 |
| 推送标签 `v*` | 创建 GitHub Release 并上传所有包 |
| Pull Request | 仅构建，不保存产物 |

### 自动构建产物

每次推送标签（如 `v1.0.0`）会自动创建 Release，包含：

- `server-v1.0.0.zip` - 后端 JAR 包
- `desktop-linux-v1.0.0.zip` - Linux 桌面版
- `desktop-macos-v1.0.0.zip` - macOS 桌面版
- `desktop-windows-v1.0.0.zip` - Windows 桌面版
- `android-v1.0.0.zip` - Android APK

### 手动触发 Release

```bash
# 1. 更新版本号（修改版本相关文件）
git add .
git commit -m "Release v1.0.0"

# 2. 打标签并推送
git tag v1.0.0
git push origin v1.0.0

# 3. GitHub Actions 自动构建并创建 Release
```

### CI 配置说明

配置文件：`.github/workflows/build-and-release.yml`

**Jobs 说明：**
1. **build-server** - 在 Ubuntu 上构建 Server JAR
2. **build-desktop** - 矩阵构建 Linux/macOS/Windows 桌面版
3. **build-android** - 在 Ubuntu 上构建 Android APK
4. **build-ios** - 在 macOS 上构建 iOS 框架（仅主分支/标签）
5. **create-release** - 创建 GitHub Release（仅标签）
6. **build-docker** - 构建并推送 Docker 镜像（需配置 Docker Hub 密钥）

### 必需的 Secrets

如需 Docker 自动推送，请在 GitHub 仓库设置：
- `DOCKER_USERNAME` - Docker Hub 用户名
- `DOCKER_PASSWORD` - Docker Hub 密码/Token

---

## 本地开发快速启动

```bash
# 1. 启动后端（开发模式）
./gradlew :server:run

# 2. 启动桌面客户端（新终端）
./gradlew :composeApp:run

# 或者同时运行（使用 Gradle 复合构建）
./gradlew :server:run :composeApp:run --parallel
```

---

## 常见问题

### Q: Server JAR 打包后配置文件在哪里？
A: 配置文件位于 `server/src/main/resources/application*.conf`，打包后会包含在 JAR 内。运行时可通过 `-config` 参数指定外部配置。

### Q: 如何修改服务端口？
A: 三种方式（优先级从高到低）：
1. 环境变量：`SERVER_PORT=9000 docker-compose up -d`
2. 修改 `.env` 文件中的 `SERVER_PORT`
3. 修改 `server/application-prod.conf` 中的端口

### Q: Desktop 应用如何连接后端？
A: composeApp 的 `jvmMain` 已内嵌 Server，无需单独部署后端。如需连接远程后端，修改代码中的 API 地址。

### Q: CI 构建失败怎么办？
A: 检查以下几点：
1. 确保 `gradle.properties` 没有本地绝对路径
2. 检查 Java 版本兼容性（项目使用 Java 21）
3. 查看 GitHub Actions 日志定位具体错误
