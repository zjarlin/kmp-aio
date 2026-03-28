package site.addzero.coding.playground.server.entity

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "codegen_project")
interface CodegenProject {
    @Id
    val id: String

    val name: String
    val description: String?

    @Column(name = "created_at")
    val createdAt: LocalDateTime

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime

    @OneToMany(mappedBy = "project")
    val targets: List<GenerationTarget>
}
