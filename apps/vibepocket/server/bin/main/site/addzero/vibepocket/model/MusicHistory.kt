package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

/**
 * 音乐历史表
 */
@Entity
@Table(name = "music_history")
interface MusicHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 任务 ID（唯一标识） */
    @Key
    @Column(name = "task_id")
    val taskId: String

    /** 任务类型（如 generate、extend 等） */
    val type: String

    /** 任务状态 */
    val status: String

    /** JSON 序列化的 tracks 列表 */
    @Column(name = "tracks_json")
    val tracksJson: String

    /** 创建时间 */
    @Column(name = "created_at")
    val createdAt: LocalDateTime
}
