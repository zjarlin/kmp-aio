package site.addzero.starter.flyway

data class FlywayConfigPlan(
    val enabled: Boolean = true,
    val defaults: FlywayDefaults = FlywayDefaults(),
    val datasources: List<FlywayDatasourcePlan> = emptyList(),
)
