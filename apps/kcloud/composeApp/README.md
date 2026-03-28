# KCloud Compose App

这里是 `kcloud` 的桌面入口模块。它负责把桌面壳、共享代码和当前接入的插件模块组装起来。

更完整的项目说明看上一级文档：[`../README.md`](../README.md)

## 常用命令

运行桌面端：

```bash
./gradlew :apps:kcloud:composeApp:jvmRun
```

打包当前操作系统的桌面安装包：

```bash
./gradlew :apps:kcloud:composeApp:packageDistributionForCurrentOS
```

## 说明

- 这是当前主要开发入口
- `jvmMain` 会带上 `:apps:kcloud:server`，方便一起调试
- README 只保留模块级说明，避免和上一级文档重复
