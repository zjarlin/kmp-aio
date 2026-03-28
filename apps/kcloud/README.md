# KCloud

KCloud 目前更像一个还在整理中的桌面工作台。它把几个自己常用的模块收进同一个 Compose Desktop 壳里，方便本地使用和继续迭代，离稳定产品还有距离。

## 当前状态

- 现在的主路径是 desktop-first，开发入口在 `composeApp`
- `server` 提供本地服务入口，便于单独调试
- 当前接入的模块主要有 `mcu-console`、`system/rbac`、`vibepocket`
- `iosApp` 目录还在，但目前不是主要开发方向

## 目录

```text
apps/kcloud/
├── composeApp/      # 桌面入口
├── server/          # 本地服务入口
├── shared/          # 共享代码
├── shared-compose/  # 共享 Compose 代码
├── plugins/
│   ├── mcu-console/
│   ├── system/rbac/
│   └── vibepocket/
└── iosApp/
```

## 运行

桌面端主入口：

```bash
./gradlew :apps:kcloud:composeApp:jvmRun
```

如果只想调试本地服务：

```bash
./gradlew :apps:kcloud:server:runJvm
```

## 打包

当前桌面安装包命令：

```bash
./gradlew :apps:kcloud:composeApp:packageDistributionForCurrentOS
```

现在的 Release 流水线只保留 `kcloud` 的桌面安装包产物。`wasm` 的 dist 还没有整理好，暂时不建议把它当成稳定交付物。

## 已知限制

- 目前还是桌面优先，不是完整的多端产品
- 一些模块边界还在调整，目录和命名后面可能继续收敛
- `wasm` 打包链路还没稳定
- README 只写当前能确认的内容，没把未来设想提前写进来
