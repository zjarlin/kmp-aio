package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "suno_task_resource")
interface SunoTaskResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    @Column(name = "task_id")
    val taskId: String

    val type: String

    val status: String

    @Column(name = "request_json")
    val requestJson: String?

    @Column(name = "tracks_json")
    val tracksJson: String

    @Column(name = "detail_json")
    val detailJson: String?

    @Column(name = "error_message")
    val errorMessage: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime
}
