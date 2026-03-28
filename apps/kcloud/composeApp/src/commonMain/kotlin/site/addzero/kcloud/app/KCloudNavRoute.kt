package site.addzero.kcloud.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class KCloudNavRoute(
    val routePath: String = "",
) : NavKey
