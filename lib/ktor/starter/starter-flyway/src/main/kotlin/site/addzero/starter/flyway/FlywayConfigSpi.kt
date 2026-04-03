package site.addzero.starter.flyway

interface FlywayConfigSpi {
    val order: Int
        get() = Int.MAX_VALUE

    fun plan(): FlywayConfigPlan
}
