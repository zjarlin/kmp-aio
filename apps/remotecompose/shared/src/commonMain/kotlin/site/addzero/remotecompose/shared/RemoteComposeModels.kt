package site.addzero.remotecompose.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class RemoteComposeLocale(
    val code: String,
) {
    ZH_CN("zh-CN"),
    EN_US("en-US");

    fun toggle(): RemoteComposeLocale {
        return if (this == ZH_CN) EN_US else ZH_CN
    }

    companion object {
        fun fromCode(value: String?): RemoteComposeLocale {
            return entries.firstOrNull { it.code.equals(value, ignoreCase = true) } ?: ZH_CN
        }
    }
}

@Serializable
enum class RemoteComposeTone {
    NEUTRAL,
    ACCENT,
    SUCCESS,
    WARNING,
    DANGER,
    INFO,
}

@Serializable
enum class RemoteComposeTextStyle {
    TITLE,
    SUBTITLE,
    BODY,
    CAPTION,
    OVERLINE,
    MONO,
}

@Serializable
enum class RemoteComposeButtonStyle {
    PRIMARY,
    SECONDARY,
    GHOST,
}

@Serializable
data class RemoteComposeScreenSummary(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String = "",
    val tone: RemoteComposeTone = RemoteComposeTone.NEUTRAL,
)

@Serializable
data class RemoteComposeScreenPayload(
    val screenId: String,
    val title: String,
    val subtitle: String,
    val serverNote: String,
    val schemaVersion: String = "remote-compose-demo/v1",
    val root: RemoteComposeNode,
)

@Serializable
data class RemoteComposeStatItem(
    val label: String,
    val value: String,
    val caption: String = "",
    val tone: RemoteComposeTone = RemoteComposeTone.NEUTRAL,
)

@Serializable
data class RemoteComposeActionButton(
    val id: String,
    val label: String,
    val style: RemoteComposeButtonStyle = RemoteComposeButtonStyle.SECONDARY,
    val action: RemoteComposeAction,
)

@Serializable
sealed class RemoteComposeAction {
    @Serializable
    @SerialName("refresh")
    data object Refresh : RemoteComposeAction()

    @Serializable
    @SerialName("open_screen")
    data class OpenScreen(
        val screenId: String,
    ) : RemoteComposeAction()

    @Serializable
    @SerialName("show_message")
    data class ShowMessage(
        val message: String,
        val tone: RemoteComposeTone = RemoteComposeTone.INFO,
    ) : RemoteComposeAction()
}

@Serializable
sealed class RemoteComposeNode {
    abstract val id: String
}

@Serializable
@SerialName("column")
data class RemoteComposeColumnNode(
    override val id: String,
    val gap: Int = 16,
    val children: List<RemoteComposeNode> = emptyList(),
) : RemoteComposeNode()

@Serializable
@SerialName("row")
data class RemoteComposeRowNode(
    override val id: String,
    val gap: Int = 16,
    val children: List<RemoteComposeNode> = emptyList(),
) : RemoteComposeNode()

@Serializable
@SerialName("card")
data class RemoteComposeCardNode(
    override val id: String,
    val title: String,
    val subtitle: String = "",
    val tone: RemoteComposeTone = RemoteComposeTone.NEUTRAL,
    val actions: List<RemoteComposeActionButton> = emptyList(),
    val children: List<RemoteComposeNode> = emptyList(),
) : RemoteComposeNode()

@Serializable
@SerialName("text")
data class RemoteComposeTextNode(
    override val id: String,
    val text: String,
    val style: RemoteComposeTextStyle = RemoteComposeTextStyle.BODY,
    val tone: RemoteComposeTone = RemoteComposeTone.NEUTRAL,
) : RemoteComposeNode()

@Serializable
@SerialName("stats")
data class RemoteComposeStatsNode(
    override val id: String,
    val columns: Int = 3,
    val items: List<RemoteComposeStatItem> = emptyList(),
) : RemoteComposeNode()

@Serializable
@SerialName("bullet_list")
data class RemoteComposeBulletListNode(
    override val id: String,
    val items: List<String> = emptyList(),
) : RemoteComposeNode()

@Serializable
@SerialName("actions")
data class RemoteComposeActionRowNode(
    override val id: String,
    val buttons: List<RemoteComposeActionButton> = emptyList(),
) : RemoteComposeNode()

object RemoteComposeJson {
    val instance = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
        classDiscriminator = "kind"
    }
}
