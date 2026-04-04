package site.addzero.starter.flyway

interface FlywayConfigSpi {
    val order
        get() = Int.MAX_VALUE

    fun plan(): FlywayConfigPlan
}
