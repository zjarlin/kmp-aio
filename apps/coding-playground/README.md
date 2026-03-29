# Kotlin 声明式代码生成台

`apps/coding-playground` 现在是一个独立的 Kotlin 声明式代码生成工作台，不再使用旧业务元数据方案，也不再使用 LLVM IR 作为产品术语。

## 模块结构

- `apps/coding-playground`
  Compose Desktop 宿主，负责中文工作台界面、状态管理和内嵌本地服务启动。
- `apps/coding-playground/shared`
  放稳定 DTO、请求对象、同步结果、KSP 索引预览 DTO 和公共 service contract。
- `apps/coding-playground/server`
  放 SQLite + Jimmer 元数据存储、CRUD 服务、源码渲染、托管文件写盘、源码导回和 KSP 索引预览。
- `apps/coding-playground/annotations`
  放生成源码使用的稳定注解与索引契约。
- `apps/coding-playground/processor`
  放 KSP 处理器，扫描托管注解并生成 `GeneratedCodeIndex`。

## 当前能力

- 结构化建模：
  `项目 -> 生成目标 -> Kotlin 文件 -> 声明`
- 支持的声明预设：
  `data class / enum class / interface / object / annotation class`
- 声明子节点：
  导包、构造参数、属性、枚举项、函数桩、声明注解
- 本地 API：
  统一挂在 `/api/codegen/*`
- 托管同步：
  生成文件头会写稳定 marker，只托管工作台生成的整文件
- KSP companion：
  生成源码统一引用 `GeneratedManagedDeclaration`
  目标工程可通过处理器生成 `GeneratedCodeIndex`
- 集成示例：
  Inspector 面板新增 `网易云 Demo` 标签页，直接调用 `:lib:api:api-netease` 的
  `MusicSearchClient.musicApi.search(...)` 与 `getLyric(...)`
  用来演示业务应用如何直接消费共享 API 库

## 关键 API

- `/api/codegen/projects`
- `/api/codegen/targets`
- `/api/codegen/files`
- `/api/codegen/declarations`
- `/api/codegen/annotations`
- `/api/codegen/sync/export`
- `/api/codegen/sync/import`
- `/api/codegen/sync/conflicts`
- `/api/codegen/render/preview/{fileId}`
- `/api/codegen/ksp/index-preview/{targetId}`

## 本地运行

启动桌面工作台：

```bash
./gradlew :apps:coding-playground:runJvm
```

启动后打开右侧 Inspector，切换到 `网易云 Demo` 标签页即可直接试搜歌和看歌词。

单独验证编译：

```bash
./gradlew :apps:coding-playground:compileKotlinJvm \
  :apps:coding-playground:annotations:compileCommonMainKotlinMetadata \
  :apps:coding-playground:processor:compileKotlinJvm
```

运行 smoke 测试：

```bash
./gradlew :apps:coding-playground:server:test :apps:coding-playground:jvmTest
```

## 生成物依赖

如果目标工程要编译工作台生成的源码，至少需要引入：

- `:apps:coding-playground:annotations`

如果目标工程还要在编译期自动产出索引对象，则再接入：

- `:apps:coding-playground:processor`

并为 KSP 指定索引包，例如：

```kotlin
ksp {
    arg("coding.playground.indexPackage", "site.addzero.generated.index")
}
```
