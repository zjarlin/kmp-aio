@file:OptIn(
    ExperimentalCupertinoApi::class,
    ExperimentalAdaptiveApi::class,
)

package site.addzero.cupertinodemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoAlertDialog
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoButton
import io.github.robinpcrd.cupertino.CupertinoButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoIcon
import io.github.robinpcrd.cupertino.CupertinoIconButton
import io.github.robinpcrd.cupertino.CupertinoIconButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoIconButtonSize
import io.github.robinpcrd.cupertino.CupertinoIconToggleButton
import io.github.robinpcrd.cupertino.CupertinoIconToggleButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoSearchTextField
import io.github.robinpcrd.cupertino.CupertinoSegmentedControl
import io.github.robinpcrd.cupertino.CupertinoSegmentedControlTab
import io.github.robinpcrd.cupertino.CupertinoSlider
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoSwitch
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.adaptive.AdaptiveButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveCheckbox
import io.github.robinpcrd.cupertino.adaptive.AdaptiveFilledIconToggleButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveIconButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveIconToggleButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveSlider
import io.github.robinpcrd.cupertino.adaptive.AdaptiveSwitch
import io.github.robinpcrd.cupertino.adaptive.AdaptiveTextButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveTheme
import io.github.robinpcrd.cupertino.adaptive.AdaptiveTonalButton
import io.github.robinpcrd.cupertino.adaptive.AdaptiveTriStateCheckbox
import io.github.robinpcrd.cupertino.adaptive.CupertinoThemeSpec
import io.github.robinpcrd.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.robinpcrd.cupertino.adaptive.MaterialThemeSpec
import io.github.robinpcrd.cupertino.adaptive.Theme
import io.github.robinpcrd.cupertino.adaptive.icons.AdaptiveIcons
import io.github.robinpcrd.cupertino.adaptive.icons.Search
import io.github.robinpcrd.cupertino.adaptive.icons.Settings
import io.github.robinpcrd.cupertino.adaptive.icons.ThumbUp
import io.github.robinpcrd.cupertino.cancel
import io.github.robinpcrd.cupertino.default
import io.github.robinpcrd.cupertino.icons.CupertinoIcons
import io.github.robinpcrd.cupertino.icons.filled.Banknote
import io.github.robinpcrd.cupertino.icons.filled.Heart
import io.github.robinpcrd.cupertino.icons.outlined.Gearshape
import io.github.robinpcrd.cupertino.icons.outlined.House
import io.github.robinpcrd.cupertino.icons.outlined.ListBullet
import io.github.robinpcrd.cupertino.icons.outlined.MoonStars
import io.github.robinpcrd.cupertino.icons.outlined.Paintpalette
import io.github.robinpcrd.cupertino.icons.outlined.SidebarLeft
import io.github.robinpcrd.cupertino.icons.outlined.SliderHorizontal3
import io.github.robinpcrd.cupertino.icons.outlined.SunMax
import io.github.robinpcrd.cupertino.section.CupertinoSection
import io.github.robinpcrd.cupertino.section.SectionItem
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import io.github.robinpcrd.cupertino.theme.darkColorScheme
import io.github.robinpcrd.cupertino.theme.lightColorScheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.plugin.module.dsl.koinConfiguration
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme
import site.addzero.cupertinodemo.adaptive_page.AdaptivePageAdaptiveIconButtonSpi

private val DemoAccent = Color(0xFF0A84FF)
private const val CupertinoVersion = "3.3.1"

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<CupertinoDemoKoinApplication>(),
    ) {
        val state: CupertinoDemoState = koinInject()

        CupertinoDemoTheme(darkMode = state.darkMode) {
            if (state.showAlert) {
                CupertinoAlertDialog(
                    onDismissRequest = state::dismissAlert,
                    title = { org.koin.compose.koinInject<AppCupertinoAlertDialogTitleSpi>().Render() },
                    message = {
                        CupertinoText("顶部栏、侧边栏、内容工作区和状态区现在都按 Cupertino 方式组织，M3 不再作为主界面暴露。")
                    },
                    buttons = {
                        cancel(onClick = state::dismissAlert) {
                            CupertinoText("关闭")
                        }
                        default(onClick = state::dismissAlert) {
                            CupertinoText("继续")
                        }
                    },
                )
            }

            CupertinoWorkbenchScaffold(
                config = CupertinoWorkbenchScaffoldConfig(
                    appTitle = "Cupertino Starter",
                    pageTitle = state.selectedPage.title,
                    pageCaption = state.selectedPage.caption,
                    navItems = demoNavItems(),
                    selectedKey = state.selectedPage.name,
                    sidebarCollapsed = state.sidebarCollapsed,
                    sidebarQuery = state.sidebarQuery,
                    statusText = buildStatusText(state),
                ),
                actions = CupertinoWorkbenchScaffoldActions(
                    onSelectItem = { key ->
                        state.selectPage(CupertinoDemoPage.valueOf(key))
                    },
                    onToggleSidebar = state::toggleSidebar,
                    onSidebarQueryChange = state::updateSidebarQuery,
                ),
                topBarActions = {
                    CupertinoIconButton(
                        onClick = state::toggleDarkMode,
                        colors = CupertinoIconButtonDefaults.bezeledGrayButtonColors(),
                        size = CupertinoIconButtonSize.Medium,
                    ) {
                        CupertinoIcon(
                            imageVector = if (state.darkMode) {
                                CupertinoIcons.Outlined.SunMax
                            } else {
                                CupertinoIcons.Outlined.MoonStars
                            },
                            contentDescription = "切换明暗",
                        )
                    }
                    CupertinoTopBarTextAction(
                        label = "提示",
                        onClick = state::showAlert,
                    )
                },
            ) {
                when (state.selectedPage) {
                    CupertinoDemoPage.Overview -> OverviewPage(state)
                    CupertinoDemoPage.Forms -> FormsPage(state)
                    CupertinoDemoPage.Controls -> ControlsPage(state)
                    CupertinoDemoPage.Adaptive -> AdaptivePage(state)
                }
            }
        }
    }
}


@Composable
private fun CupertinoDemoTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit,
) {
    AdaptiveTheme(
        target = Theme.Cupertino,
        material = MaterialThemeSpec(
            colorScheme = if (darkMode) {
                materialDarkColorScheme(
                    primary = DemoAccent,
                    secondary = Color(0xFF64D2FF),
                )
            } else {
                materialLightColorScheme(
                    primary = DemoAccent,
                    secondary = Color(0xFF64D2FF),
                )
            },
        ),
        cupertino = CupertinoThemeSpec(
            colorScheme = if (darkMode) {
                darkColorScheme(accent = DemoAccent)
            } else {
                lightColorScheme(accent = DemoAccent)
            },
        ),
        content = content,
    )
}

private fun demoNavItems(): List<CupertinoWorkbenchNavItem> =
    listOf(
        CupertinoWorkbenchNavItem(
            key = CupertinoDemoPage.Overview.name,
            title = "Workbench",
            caption = "桌面壳层结构",
            icon = CupertinoIcons.Outlined.House,
        ),
        CupertinoWorkbenchNavItem(
            key = CupertinoDemoPage.Forms.name,
            title = "Forms",
            caption = "输入与设置",
            icon = CupertinoIcons.Outlined.ListBullet,
        ),
        CupertinoWorkbenchNavItem(
            key = CupertinoDemoPage.Controls.name,
            title = "Controls",
            caption = "控件与交互",
            icon = CupertinoIcons.Outlined.SliderHorizontal3,
        ),
        CupertinoWorkbenchNavItem(
            key = CupertinoDemoPage.Adaptive.name,
            title = "Adaptive API",
            caption = "固定输出 Cupertino",
            icon = CupertinoIcons.Outlined.Gearshape,
        ),
    )

private fun buildStatusText(
    state: CupertinoDemoState,
): String =
    "compose-cupertino $CupertinoVersion · ${state.selectedPage.title} · ${if (state.darkMode) "Dark" else "Light"}"

@Composable
private fun OverviewPage(
    state: CupertinoDemoState,
) {
    WorkbenchCard(
        title = "脚手架定位",
        caption = "这个壳层现在更像真正的桌面产品，而不是单页组件展板。",
    ) {
        InfoRow("导航结构", "左侧导航 + 顶栏动作 + 内容工作区 + 底部状态")
        InfoRow("主视觉", "完全按 Cupertino theme 和组件组织，不暴露 M3 页面切换")
        InfoRow("运行命令", "./gradlew :apps:cupertino-demo:run")
        InfoRow("当前版本", "io.github.robinpcrd:cupertino $CupertinoVersion")
    }

    WorkbenchCard(
        title = "项目草稿",
        caption = "这里放一个最常见的桌面工作台开场区域。",
    ) {
        CupertinoBorderedTextField(
            value = state.projectName,
            onValueChange = state::updateProjectName,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                CupertinoText("输入项目名称")
            },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CupertinoButton(
                onClick = state::showAlert,
                colors = CupertinoButtonDefaults.filledButtonColors(),
            ) {
                CupertinoText("创建工作区")
            }
            CupertinoButton(
                onClick = state::toggleSidebar,
                colors = CupertinoButtonDefaults.grayButtonColors(),
            ) {
                CupertinoIcon(
                    imageVector = CupertinoIcons.Outlined.SidebarLeft,
                    contentDescription = null,
                )
                CupertinoText("切换导航")
            }
        }
    }
}

@Composable
private fun FormsPage(
    state: CupertinoDemoState,
) {
    WorkbenchCard(
        title = "基础输入",
        caption = "常见字段直接用 Cupertino 风格输入组件就够了。",
    ) {
        CupertinoBorderedTextField(
            value = state.projectName,
            onValueChange = state::updateProjectName,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                CupertinoText("项目名")
            },
            leadingIcon = {
                CupertinoIcon(
                    imageVector = CupertinoIcons.Outlined.Paintpalette,
                    contentDescription = null,
                )
            },
        )
        CupertinoSearchTextField(
            value = state.searchText,
            onValueChange = state::updateSearchText,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    WorkbenchCard(
        title = "设置项",
        caption = "Section 风格很适合桌面工具里的轻设置页面。",
    ) {
        CupertinoSection(
            title = {
                CupertinoText("偏好设置")
            },
        ) {
            SectionItem(
                trailingContent = {
                    CupertinoSwitch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = state::updateNotificationsEnabled,
                    )
                },
                title = {
                    SettingText(
                        title = "通知提醒",
                        caption = "用于后台任务和操作结果提示",
                    )
                },
            )
            SectionItem(
                trailingContent = {
                    CupertinoIconToggleButton(
                        checked = state.favoriteEnabled,
                        onCheckedChange = state::updateFavoriteEnabled,
                        colors = CupertinoIconToggleButtonDefaults.bezeledFilledButtonColors(),
                    ) {
                        CupertinoIcon(
                            imageVector = CupertinoIcons.Filled.Heart,
                            contentDescription = "收藏",
                        )
                    }
                },
                title = {
                    SettingText(
                        title = "标记收藏",
                        caption = "用于保留常用模板和页面配置",
                    )
                },
            )
        }
    }
}

@Composable
private fun ControlsPage(
    state: CupertinoDemoState,
) {
    WorkbenchCard(
        title = "按钮组",
        caption = "Filled / Tinted / Plain 基本就够覆盖绝大多数桌面场景。",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CupertinoButton(
                onClick = state::showAlert,
                colors = CupertinoButtonDefaults.filledButtonColors(),
            ) {
                CupertinoText("Primary")
            }
            CupertinoButton(
                onClick = state::showAlert,
                colors = CupertinoButtonDefaults.tintedButtonColors(),
            ) {
                CupertinoText("Tinted")
            }
            CupertinoButton(
                onClick = state::showAlert,
                colors = CupertinoButtonDefaults.plainButtonColors(),
            ) {
                CupertinoText("Plain")
            }
        }
    }

    WorkbenchCard(
        title = "分段与滑杆",
        caption = "这两个控件很适合放在工具类界面的参数切换区。",
    ) {
        CupertinoSegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = state.selectedCoreSegment,
        ) {
            listOf("概览", "输入", "图标").forEachIndexed { index, label ->
                CupertinoSegmentedControlTab(
                    isSelected = state.selectedCoreSegment == index,
                    onClick = {
                        state.updateSelectedCoreSegment(index)
                    },
                ) {
                    CupertinoText(label)
                }
            }
        }
        CupertinoText("滑杆值 ${(state.coreSliderValue * 100).toInt()}%")
        CupertinoSlider(
            value = state.coreSliderValue,
            onValueChange = state::updateCoreSliderValue,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    WorkbenchCard(
        title = "图标按钮",
        caption = "这个 fork 的 icon toggle button 比较实用，适合工作台工具条。",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CupertinoIconButton(
                onClick = state::showAlert,
                size = CupertinoIconButtonSize.Medium,
                colors = CupertinoIconButtonDefaults.bezeledFilledButtonColors(),
            ) {
                CupertinoIcon(
                    imageVector = CupertinoIcons.Filled.Banknote,
                    contentDescription = "动作",
                )
            }
            CupertinoIconButton(
                onClick = state::showAlert,
                size = CupertinoIconButtonSize.Medium,
                colors = CupertinoIconButtonDefaults.bezeledGrayButtonColors(),
            ) {
                CupertinoIcon(
                    imageVector = CupertinoIcons.Outlined.Paintpalette,
                    contentDescription = "主题",
                )
            }
            CupertinoIconToggleButton(
                checked = state.favoriteEnabled,
                onCheckedChange = state::updateFavoriteEnabled,
                size = CupertinoIconButtonSize.Medium,
                colors = CupertinoIconToggleButtonDefaults.bezeledFilledButtonColors(),
            ) {
                CupertinoIcon(
                    imageVector = CupertinoIcons.Filled.Heart,
                    contentDescription = "收藏",
                )
            }
        }
    }
}

@Composable
private fun AdaptivePage(
    state: CupertinoDemoState,
) {
    WorkbenchCard(
        title = "Adaptive 组件",
        caption = "虽然 API 名叫 Adaptive，但根主题固定成 Theme.Cupertino，所以界面仍然是 Cupertino 风格。",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdaptiveButton(onClick = state::showAlert) {
                CupertinoText("Primary")
            }
            AdaptiveTextButton(onClick = state::showAlert) {
                CupertinoText("Text")
            }
            AdaptiveTonalButton(onClick = state::showAlert) {
                CupertinoText("Tonal")
            }
        }
    }

    WorkbenchCard(
        title = "Adaptive 控件",
        caption = "如果你将来要兼容别的 target，也可以先写这层 API，但当前默认仍输出 Cupertino。",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdaptiveSwitch(
                checked = state.adaptiveSwitchEnabled,
                onCheckedChange = state::updateAdaptiveSwitchEnabled,
            )
            AdaptiveCheckbox(
                checked = state.adaptiveCheckboxEnabled,
                onCheckedChange = state::updateAdaptiveCheckboxEnabled,
            )
            AdaptiveTriStateCheckbox(
                state = state.adaptiveTriState,
                onClick = state::cycleAdaptiveTriState,
            )
        }
        CupertinoText("TriState: ${adaptiveTriStateLabel(state.adaptiveTriState)}")
        AdaptiveSlider(
            value = state.adaptiveSliderValue,
            onValueChange = state::updateAdaptiveSliderValue,
            modifier = Modifier.fillMaxWidth(),
            steps = 4,
        )
    }

    WorkbenchCard(
        title = "Adaptive 图标按钮",
        caption = "同一套 API 也能直接吃 Cupertino icon。",
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            org.koin.compose.koinInject<AdaptivePageAdaptiveIconButtonSpi>().Render(state = state)
            AdaptiveIconToggleButton(
                checked = state.adaptiveIconSelected,
                onCheckedChange = state::updateAdaptiveIconSelected,
            ) {
                CupertinoIcon(
                    imageVector = AdaptiveIcons.Outlined.ThumbUp,
                    contentDescription = "Like",
                )
            }
            AdaptiveFilledIconToggleButton(
                checked = state.adaptiveIconSelected,
                onCheckedChange = state::updateAdaptiveIconSelected,
            ) {
                CupertinoIcon(
                    imageVector = AdaptiveIcons.Outlined.Settings,
                    contentDescription = "Settings",
                )
            }
        }
    }
}


@Composable
private fun WorkbenchCard(
    title: String,
    caption: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    CupertinoSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = CupertinoTheme.shapes.large,
        color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CupertinoText(
                text = title,
                style = CupertinoTheme.typography.title3,
            )
            CupertinoText(
                text = caption,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption1,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        CupertinoText(
            text = value,
            style = CupertinoTheme.typography.body,
        )
    }
}

@Composable
private fun SettingText(
    title: String,
    caption: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CupertinoText(title)
        CupertinoText(
            text = caption,
            style = CupertinoTheme.typography.footnote,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
    }
}

private fun adaptiveTriStateLabel(state: ToggleableState): String =
    when (state) {
        ToggleableState.Off -> "Off"
        ToggleableState.Indeterminate -> "Indeterminate"
        ToggleableState.On -> "On"
    }

/**
 * 应用级提示弹窗标题插槽。
 *
 * 这个示例保留在根文件里，用来说明全局壳层文案也能通过 SPI 接入；
 * 但页面级的人机交互入口仍然优先落到各自页面包中管理。
 */
interface AppCupertinoAlertDialogTitleSpi {
    @androidx.compose.runtime.Composable
    fun Render()
}

/**
 * 应用级提示弹窗标题的默认实现。
 *
 * 默认仅渲染一段固定标题文案，后续如果宿主要替换成品牌化标题或环境提示，
 * 只需要替换这一份实现，不用改动弹窗布局。
 */
@org.koin.core.annotation.Single
class DefaultAppCupertinoAlertDialogTitleSpi : AppCupertinoAlertDialogTitleSpi {
    @androidx.compose.runtime.Composable
    override fun Render() {
        CupertinoText("脚手架已经换成纯 Cupertino")
    }
}
