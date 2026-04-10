package site.addzero.kcloud.plugins.codegencontext.screen.contexts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.robinpcrd.cupertino.CupertinoSegmentedControl
import io.github.robinpcrd.cupertino.CupertinoSegmentedControlTab
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextWorkbenchTab

/**
 * Codegen Context 页面工作区 tab 切换插槽。
 *
 * 工作区面板整体排版仍保留在 `WorkbenchTabsPanel`，
 * 这里专门承接“当前选中哪个工作区 tab”的交互块，方便后续替换成不同宿主的切换器样式或交互策略。
 */
interface CodegenContextWorkbenchTabSwitchSpi {
    @Composable
    fun Render(
        tabs: List<Pair<CodegenContextWorkbenchTab, String>>,
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
        modifier: Modifier = Modifier,
    )
}

/**
 * Codegen Context 页面工作区 tab 切换的默认实现。
 *
 * 当前默认使用 Cupertino 分段控件切换“设备功能 / 物模型字段”两个视图；
 * 如果后续要替换成宿主自定义 tabs、权限裁剪或更多面板，只替换这一实现即可。
 */
@Single
class DefaultCodegenContextWorkbenchTabSwitchSpi : CodegenContextWorkbenchTabSwitchSpi {
    @OptIn(ExperimentalCupertinoApi::class)
    @Composable
    override fun Render(
        tabs: List<Pair<CodegenContextWorkbenchTab, String>>,
        state: CodegenContextScreenState,
        viewModel: CodegenContextViewModel,
        modifier: Modifier,
    ) {
        val selectedIndex = tabs.indexOfFirst { (tab, _) ->
            tab == state.selectedWorkbenchTab
        }.coerceAtLeast(0)
        CupertinoSegmentedControl(
            modifier = modifier,
            selectedTabIndex = selectedIndex,
        ) {
            tabs.forEach { (tab, label) ->
                CupertinoSegmentedControlTab(
                    isSelected = state.selectedWorkbenchTab == tab,
                    onClick = { viewModel.selectWorkbenchTab(tab) },
                ) {
                    CupertinoText(label)
                }
            }
        }
    }
}
