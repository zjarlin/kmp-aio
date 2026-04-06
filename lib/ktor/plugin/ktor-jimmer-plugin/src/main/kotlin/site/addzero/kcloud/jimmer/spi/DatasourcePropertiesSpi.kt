package site.addzero.kcloud.jimmer.spi

data class DatasourceProperties(
    val name: String,
    val enabled: Boolean = true,
    val default: Boolean = false,
    val url: String,
    val driverClassName: String,
    val user: String = "",
    val password: String = "",
)

/**
 * 数据源属性 SPI。
 * app 侧只实现这一份 SPI，插件内部自己处理默认源与扩展源。
 */
interface DatasourcePropertiesSpi {
    fun datasources(): List<DatasourceProperties>
}
