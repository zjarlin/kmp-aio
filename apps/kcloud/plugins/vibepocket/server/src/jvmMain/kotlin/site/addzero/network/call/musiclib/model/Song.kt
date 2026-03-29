package site.addzero.network.call.musiclib.model

data class Song(
    val id: String,
    val name: String,
    val artist: String = "",
    val album: String = "",
    val duration: Int = 0,
    val source: String = "",
    val cover: String = "",
    val link: String = "",
    val extra: Map<String, String> = emptyMap(),
    val url: String = "",
) {
    fun filename(): String {
        val baseName = listOf(artist, name)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_')
            .ifBlank { "audio" }
        val extension = url.substringAfterLast('/', "")
            .substringAfterLast('.', "")
            .substringBefore('?')
            .lowercase()
            .takeIf { it.isNotBlank() }
            ?: "mp3"
        return "$baseName.$extension"
    }
}
