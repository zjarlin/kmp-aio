package site.addzero.kcloud.plugins.hostconfig.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

/**
 * 定义主机配置node类型枚举。
 */
enum class HostConfigNodeKind {
  PROJECT,
  PROTOCOL,
  MODULE,
  DEVICE,
  TAG,
}

/**
 * 表示主机配置树node。
 *
 * @property id 主键 ID。
 * @property kind 类型。
 * @property projectId 项目 ID。
 * @property entityId entity ID。
 * @property parentEntityId parententity ID。
 * @property label label。
 * @property caption caption。
 * @property children children。
 */
data class HostConfigTreeNode(
  val id: String,
  val kind: HostConfigNodeKind,
  val projectId: Long,
  val entityId: Long,
  val parentEntityId: Long? = null,
  val label: String,
  val caption: String? = null,
  val children: List<HostConfigTreeNode> = emptyList(),
)

/**
 * 处理主机配置node类型。
 */
fun HostConfigNodeKind.icon(): ImageVector {
  return when (this) {
    HostConfigNodeKind.PROJECT -> Icons.Outlined.SettingsApplications
    HostConfigNodeKind.PROTOCOL -> Icons.Outlined.CloudQueue
    HostConfigNodeKind.MODULE -> Icons.Outlined.Memory
    HostConfigNodeKind.DEVICE -> Icons.Outlined.DeviceHub
    HostConfigNodeKind.TAG -> Icons.Outlined.Tag
  }
}

/**
 * 处理校验位。
 */
fun Parity.label(): String {
  return when (this) {
    Parity.NONE -> "无校验"
    Parity.ODD -> "奇校验"
    Parity.EVEN -> "偶校验"
  }
}

/**
 * 处理point类型。
 */
fun PointType.label(): String {
  return when (this) {
    PointType.NORMAL -> "普通点"
    PointType.PULSE -> "脉冲点"
  }
}

/**
 * 处理传输类型。
 */
fun TransportType.label(): String {
  return when (this) {
    TransportType.TCP -> "TCP"
    TransportType.RTU -> "RTU"
  }
}

/**
 * 处理双字节字节序。
 */
fun ByteOrder2.label(): String {
  return when (this) {
    ByteOrder2.ORDER_21 -> "21"
    ByteOrder2.ORDER_12 -> "12"
  }
}

/**
 * 处理四字节字节序。
 */
fun ByteOrder4.label(): String {
  return when (this) {
    ByteOrder4.ORDER_4321 -> "4321"
    ByteOrder4.ORDER_1234 -> "1234"
    ByteOrder4.ORDER_2143 -> "2143"
    ByteOrder4.ORDER_3412 -> "3412"
  }
}

/**
 * 处理浮点字序。
 */
fun FloatOrder.label(): String {
  return when (this) {
    FloatOrder.ORDER_4321 -> "4321"
    FloatOrder.ORDER_1234 -> "1234"
    FloatOrder.ORDER_2143 -> "2143"
    FloatOrder.ORDER_3412 -> "3412"
  }
}

/**
 * 处理string。
 */
fun String?.orDash(): String {
  return this?.takeIf(String::isNotBlank) ?: "未设置"
}

/**
 * 处理long。
 */
fun Long?.toSizeLabel(): String {
  val size = this ?: return "-"
  if (size < 1024) {
    return "$size B"
  }
  val kiloBytes = size / 1024.0
  if (kiloBytes < 1024) {
    return "${kiloBytes.toSingleDecimalLabel()} KB"
  }
  val megaBytes = kiloBytes / 1024.0
  return "${megaBytes.toSingleDecimalLabel()} MB"
}

/**
 * 处理double。
 */
private fun Double.toSingleDecimalLabel(): String {
  val rounded = kotlin.math.round(this * 10.0) / 10.0
  return rounded.toString()
}

/**
 * 处理列表。
 *
 * @param nodeId node ID。
 */
fun List<HostConfigTreeNode>.findNode(
  nodeId: String?,
): HostConfigTreeNode? {
  if (nodeId == null) {
    return null
  }

  /**
   * 处理search。
   *
   * @param nodes nodes。
   */
  fun search(nodes: List<HostConfigTreeNode>): HostConfigTreeNode? {
    nodes.forEach { node ->
      if (node.id == nodeId) {
        return node
      }
      val child = search(node.children)
      if (child != null) {
        return child
      }
    }
    return null
  }

  return search(this)
}
