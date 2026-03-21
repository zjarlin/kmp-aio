package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

/**
 * 音乐生成任务记录
 */
@Entity
@Table(name = "music_task")
interface MusicTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** Suno 任务 ID */
    @Column(name = "task_id")
    val taskId: String

    /** 任务状态: queued / processing / complete / error */
    val status: String

    /** 歌曲标题 */
    val title: String?

    /** 风格标签 */
    val tags: String?

    /** 歌词 */
    @Column(name = "prompt")
    val prompt: String?

    /** 模型版本 */
    val mv: String?

    /** 音频 URL */
    @Column(name = "audio_url")
    val audioUrl: String?

    /** 视频 URL */
    @Column(name = "video_url")
    val videoUrl: String?

    /** 错误信息 */
    @Column(name = "error_message")
    val errorMessage: String?

    /** 创建时间 */
    @Column(name = "created_at")
    val createdAt: LocalDateTime

    /** 更新时间 */
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
