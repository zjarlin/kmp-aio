# Remote Compose Demo

`apps/remotecompose` 是一个平级于 `apps/liquiddemo` 的桌面 demo，用来演示“服务端下发可序列化 UI schema，客户端解释成 Compose 界面”这一条 remote compose 链路。

模块拆分：

- `apps/remotecompose/shared`：共享协议、UI schema、动作模型
- `apps/remotecompose/client`：Compose renderer、状态管理、HTTP 拉取逻辑
- `apps/remotecompose/server`：`spring2ktor` 路由、schema 组装服务
- `apps/remotecompose`：桌面宿主，负责启动本地 Koin 与内嵌 Ktor server

## Run

```bash
./gradlew :apps:remotecompose:jvmRun
```
