package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "etl_wrapper_meta")
interface EtlWrapperMeta {
    @Id
    val id: String

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: ProjectMeta

    @IdView
    val projectId: String

    val name: String
    val key: String
    val description: String?

    @Column(name = "script_body")
    val scriptBody: String

    val enabled: Boolean

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "etlWrapper")
    val templates: List<TemplateMeta>
}
