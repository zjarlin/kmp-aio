package site.addzero.kcloud.jimmer.spi

/**
 * 数据源属性（从配置中心覆盖后的 datasources.xxx 读取）
 */
interface DatasourcePropertiesSpi {
    val default get() = false
    val url: String
    get() = "jdbc:sqlite:./config-center.sqlite"

    val driverClassName: String
    val user: String
    val password: String
}