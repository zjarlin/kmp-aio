# media-playlist-player

一个基于 Compose Multiplatform 和 MediaPlayer-KMP 的通用列表播放器组件。

Maven 工件坐标：

```text
site.addzero:media-playlist-player
```

## Features

- 泛型列表渲染，不强绑业务实体
- 内置封面、歌名、歌手、时长展示
- 内置音频 URL 异步解析和错误提示
- 直接集成 `MediaPlayer-KMP` 做试听播放
- 解析到空 URL 或识别到无音源错误时，自动禁用试听按钮并显示“无音源”
- 仅依赖 Material 3，不耦合具体业务主题

## Usage

```kotlin
MediaPlaylistPlayer(
    items = songs,
    itemKey = { it.id },
    titleOf = { it.name },
    artistOf = { it.artist },
    durationMsOf = { it.durationMs },
    coverUrlOf = { it.coverUrl },
    resolveUrl = { song -> song.audioUrl },
    itemActions = { song ->
        FilledTonalButton(onClick = { onImport(song) }) {
            Text("导入")
        }
    },
)
```

## Notes

- `resolveUrl` 适合接播放链接解析接口
- `itemActions` 适合挂业务按钮，例如导入、下载、收藏
- 默认样式是原生 Material 3，适合继续在业务侧覆写外层布局
