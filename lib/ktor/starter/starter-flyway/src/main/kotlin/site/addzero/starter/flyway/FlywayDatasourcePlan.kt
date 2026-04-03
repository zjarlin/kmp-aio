package site.addzero.starter.flyway

data class FlywayDatasourcePlan(
    val name: String,
    val enabled: Boolean,
    val url: String,
    val user: String,
    val password: String,
    val driver: String,
    val flywayEnabled: Boolean,
    val defaults: FlywayDefaults = FlywayDefaults(),
)
