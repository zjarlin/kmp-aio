package site.addzero.kcloud.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class WorkbenchNavRoute(
    val routePath: String = "",
) : NavKey
