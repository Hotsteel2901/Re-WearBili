package cn.spacexc.wearbili.remake.common.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.proto.settings.DeviceLayout

/**
 * 设备布局规范。
 *
 * 手表与手机是两套信息密度：
 *  - Watch：小圆屏，密度低（density 被全局放大）、大触控目标、单列内容
 *  - Phone：常规手机密度，更高的信息密度、多列网格、标准触控目标
 *
 * 两端动画行为完全一致（同一 AnimationLevel），布局差异只体现在
 * 密度 / 字号 / 触控目标 / 网格列数上。
 */
data class WearBiliLayoutSpec(
    val layout: DeviceLayout = DeviceLayout.DeviceWatch,
    /** 内容网格列数（推荐页等列表场景） */
    val gridColumns: Int,
    /** 页面标题字号 */
    val titleFont: TextUnit,
    /** 正文/列表主文字号 */
    val bodyFont: TextUnit,
    /** 辅助文字字号 */
    val captionFont: TextUnit,
    /** 最小触控目标高度（MD3 规范 48dp，手表放宽到 52dp） */
    val minTouchTarget: Dp,
    /** 卡片默认圆角（MD3E 偏大圆角） */
    val cardCorner: Dp,
    /** 页面水平内容边距 */
    val contentPadding: Dp,
    /** 列表项间距 */
    val listSpacing: Dp,
    /** 对话框/菜单圆角 */
    val dialogCorner: Dp,
) {
    val isPhone: Boolean get() = layout == DeviceLayout.DevicePhone
    val isWatch: Boolean get() = layout == DeviceLayout.DeviceWatch
}

/** 手表布局：低密度、大目标 */
val WatchLayoutSpec = WearBiliLayoutSpec(
    layout = DeviceLayout.DeviceWatch,
    gridColumns = 1,
    titleFont = 13.sp,
    bodyFont = 11.sp,
    captionFont = 9.sp,
    minTouchTarget = 52.dp,
    cardCorner = 16.dp,
    contentPadding = 10.dp,
    listSpacing = 6.dp,
    dialogCorner = 20.dp,
)

/** 手机布局：高密度、标准 MD3 目标 */
val PhoneLayoutSpec = WearBiliLayoutSpec(
    layout = DeviceLayout.DevicePhone,
    gridColumns = 2,
    titleFont = 16.sp,
    bodyFont = 14.sp,
    captionFont = 12.sp,
    minTouchTarget = 48.dp,
    cardCorner = 20.dp,
    contentPadding = 16.dp,
    listSpacing = 8.dp,
    dialogCorner = 28.dp,
)

val LocalLayoutSpec = staticCompositionLocalOf { WatchLayoutSpec }

/** 当前设备布局规范（简写访问器） */
@Composable
fun rememberLayoutSpec(): WearBiliLayoutSpec {
    val layout = LocalConfiguration.current.customization.deviceLayout
    return when (layout) {
        DeviceLayout.DevicePhone -> PhoneLayoutSpec
        else -> WatchLayoutSpec
    }
}
