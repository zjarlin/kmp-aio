package site.addzero.kcloud.screens.musicstudio

enum class MusicStudioTab(
    val title: String,
    val subtitle: String,
) {
    COVER(
        title = "翻唱",
        subtitle = "贴 URL 直接翻唱，默认打开这一栏。",
    ),
    GENERATE(
        title = "生成音乐",
        subtitle = "先整理歌词，再补 persona 和 vibe 参数。",
    ),
}
