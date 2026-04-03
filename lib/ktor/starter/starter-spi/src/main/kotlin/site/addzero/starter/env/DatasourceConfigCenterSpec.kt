package site.addzero.starter.env

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "DatasourceConfigKeys",
)
interface DatasourceConfigCenterSpec {
    @ConfigCenterItem(
        key = "datasources.{name}.enabled",
        comment = "是否启用数据源 {name}。",
        defaultValue = "false",
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.url",
        comment = "数据源 {name} 的 JDBC 地址。",
    )
    val url: String

    @ConfigCenterItem(
        key = "datasources.{name}.driver",
        comment = "数据源 {name} 的 JDBC 驱动类名。",
    )
    val driver: String

    @ConfigCenterItem(
        key = "datasources.{name}.user",
        comment = "数据源 {name} 的用户名。",
    )
    val user: String

    @ConfigCenterItem(
        key = "datasources.{name}.password",
        comment = "数据源 {name} 的密码。",
    )
    val password: String
}
