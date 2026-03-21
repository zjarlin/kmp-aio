package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

/**
 * 收藏表
 */
@Entity
@Table(name = "favorite_track")
interface FavoriteTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 音轨 ID（唯一标识） */
    @Key
    @Column(name = "track_id")
    val trackId: String

    /** 所属任务 ID */
    @Column(name = "task_id")
    val taskId: String

    /** 音频 URL */
    @Column(name = "audio_url")
    val audioUrl: String?

    /** 标题 */
    val title: String?

    /** 风格标签 */
    val tags: String?

    /** 封面图 URL */
    @Column(name = "image_url")
    val imageUrl: String?

    /** 时长（秒） */
    val duration: Double?

    /** 创建时间 */
    @Column(name = "created_at")
    val createdAt: LocalDateTime
}
