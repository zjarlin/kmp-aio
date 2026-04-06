//package site.addzero.kcloud.shell.menu
//
//import androidx.compose.foundation.layout.RowScope
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.SmartToy
//import androidx.compose.runtime.Composable
//import org.koin.core.annotation.Single
//import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
//import site.addzero.cupertino.workbench.button.WorkbenchIconButton
//import site.addzero.cupertino.workbench.material3.Icon
//import site.addzero.cupertino.workbench.menu.WorkbenchTopBarActionContributor
//
///**
// * 壳层动作聚合器。
// *
// * 脚手架只依赖这一层，具体按钮通过 IoC 自动扩展。
// */
//
//@Single
//class AiAssistantTopBarActionContributor(
//    private val aiOverlayState: AddChatOverlayState,
//) : WorkbenchTopBarActionContributor {
//    override val order = 10
//
//    @Composable
//    override fun RowScope.Render() {
//        WorkbenchIconButton(
//            onClick = aiOverlayState::toggle,
//            modifier = androidx.compose.ui.Modifier,
//            tooltip = if (aiOverlayState.visible) "关闭 AI 助手" else "打开 AI 助手",
//            variant = if (aiOverlayState.visible) {
//                    WorkbenchButtonVariant.Secondary
//                } else {
//                    WorkbenchButtonVariant.Outline
//                },
//            content = fun RowScope.() {
//                Icon(
//                    imageVector = Icons.Default.SmartToy,
//                    contentDescription = null,
//                )
//            },
//        )
//    }
//}