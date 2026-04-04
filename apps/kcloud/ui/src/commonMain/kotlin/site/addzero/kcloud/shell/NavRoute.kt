package site.addzero.kcloud.shell

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class NavRoute(
    val routePath: String = "",
) : NavKey
