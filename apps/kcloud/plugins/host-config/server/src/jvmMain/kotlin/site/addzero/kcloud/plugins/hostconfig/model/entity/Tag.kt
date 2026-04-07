package site.addzero.kcloud.plugins.hostconfig.model.entity

import java.math.BigDecimal
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType

@Entity
@Table(name = "host_config_tag")
interface Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val description: String?

    val registerAddress: Int

    val enabled: Boolean

    val defaultValue: String?

    val exceptionValue: String?

    val pointType: PointType?

    val debounceMs: Int?

    val sortIndex: Int

    val scalingEnabled: Boolean

    val scalingOffset: BigDecimal?

    val rawMin: BigDecimal?

    val rawMax: BigDecimal?

    val engMin: BigDecimal?

    val engMax: BigDecimal?

    val forwardEnabled: Boolean

    val forwardRegisterAddress: Int?

    val createdAt: Long

    val updatedAt: Long

    @ManyToOne
    val device: Device

    @ManyToOne
    val dataType: DataType

    @ManyToOne
    val registerType: RegisterType

    @ManyToOne
    val forwardRegisterType: RegisterType?

    @OneToMany(mappedBy = "tag")
    val valueTexts: List<TagValueText>
}
