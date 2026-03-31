# mcu-console server

这个模块现在按 project-level KSP plugin 接 Modbus RTU 代码生成，不再直接在业务模块里手写：

- `kspJvm(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))`
- `implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))`
- `ksp { arg("addzero.modbus.*", ...) }`

## 当前接法

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.ksp.modbus-rtu")
}

modbusRtu {
    codegenModes.set(listOf("server"))
    contractPackages.set(listOf("site.addzero.kcloud.plugins.mcuconsole.service.modbus"))
}
```

说明：

- `site.addzero.ksp.modbus-rtu` 负责注入 `modbus-ksp-rtu`
- 插件会自动补 `modbus-runtime`
- 业务模块只保留自己的业务 KSP 参数，例如 `controller2api`、`spring2ktor`、`entity2iso`

## 本地联调

当本地存在 `/Users/zjarlin/IdeaProjects/addzero-lib-jvm` 时，`kmp-aio/settings.gradle.kts` 会做两件事：

1. 在 `pluginManagement` 里把 `site.addzero.ksp.modbus-rtu` 绑定到本地 `addzero-lib-jvm` 的版本号。
2. 把下面这些处理器相关项目路径映射进当前构建：

- `:lib:ksp:metadata:modbus:modbus-ksp-rtu`
- `:lib:ksp:metadata:modbus:modbus-ksp-core`
- `:lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway`
- `:lib:ksp:metadata:modbus:modbus-ksp-c-contract`
- `:lib:ksp:metadata:modbus:modbus-ksp-markdown`

这里不再把整个 `addzero-lib-jvm` 塞进 `pluginManagement.includeBuild(...)`。当前仓库组合下，那样会把构建带到 `modbus-ksp-core` 的缺失项目路径错误。

本地联调前，先把插件 artifact 发到 `mavenLocal`：

```bash
cd /Users/zjarlin/IdeaProjects/addzero-lib-jvm
./gradlew \
  :lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base:publishToMavenLocal \
  :lib:ksp:metadata:modbus:modbus-rtu-gradle-plugin:publishToMavenLocal
```

之后 `plugins { id("site.addzero.ksp.modbus-rtu") }` 会从 `mavenLocal` 解析插件实现，再优先把处理器与 runtime 绑定到上面这些本地 project path。

如果本地没有 `addzero-lib-jvm`，则退回到仓库里可解析的 `site.addzero:modbus-rtu-gradle-plugin` 与其依赖。
