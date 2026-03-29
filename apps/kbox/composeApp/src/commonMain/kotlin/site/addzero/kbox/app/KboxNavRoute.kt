package site.addzero.kbox.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class KboxNavRoute(
    val routePath: String = "",
) : NavKey
