package site.addzero.starter.statuspages.spi

import io.ktor.server.application.Application
import io.ktor.server.plugins.statuspages.StatusPagesConfig

/**
 * Allows feature modules to contribute Ktor StatusPages handlers without
 * hard-coding plugin-specific exceptions into the shared starter.
 */
interface StatusPagesSpi {
    val order
        get() = 0

    fun StatusPagesConfig.configure(application: Application)
}
