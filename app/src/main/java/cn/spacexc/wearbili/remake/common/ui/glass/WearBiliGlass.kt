package cn.spacexc.wearbili.remake.common.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.proto.settings.AnimationLevel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Re-WearBili 液态玻璃（Haze）接入层。
 *
 * 设计目标：
 * 1. **可开关** —— 由 `Customization.glassEnabled` 控制，关闭时退化为普通纯色/半透明，
 *    不影响布局与可读性（手表小屏上模糊过度会看不清内容）。
 * 2. **低配降级** —— animationLevel == Off 时自动关闭模糊，改用不透明度模拟，
 *    避免在低端手表上反复重建 RenderEffect 造成掉帧。
 * 3. **主题联动** —— 玻璃底色取自 ColorScheme.surface，自动跟随 Monet / 装扮。
 *
 * 用法：
 * ```
 * val hazeState = rememberHazeState()
 * Box {
 *     // 背景层（会被模糊采样）
 *     Content(Modifier.hazeSource(hazeState))
 *     // 前景玻璃层
 *     TitleBar(Modifier.wearBiliGlass(hazeState, shape = RoundedCornerShape(16.dp)))
 * }
 * ```
 */

/** 玻璃层级：越靠前的层越薄、越通透；越靠后越厚、越实 */
enum class GlassLevel {
    /** 极薄 —— 浮层提示、chip */
    UltraThin,

    /** 薄 —— 标题栏、工具栏 */
    Thin,

    /** 常规 —— 卡片、列表项 */
    Regular,

    /** 厚 —— 对话框、底部面板 */
    Thick,
}

/**
 * Haze 状态作用域。
 *
 * 为什么用 CompositionLocal 而不是层层传参：
 * 手表端页面层级深（TitleBackground → 页面 → 卡片），层层透传 HazeState 会污染大量签名。
 * 这里用 CompositionLocal 提供，调用方可选地读取。
 */
val LocalHazeState: ProvidableCompositionLocal<HazeState?> = staticCompositionLocalOf { null }

/** 是否允许使用毛玻璃（受配置 + 动画档位双重约束） */
val isGlassEffectEnabled: Boolean
    @Composable
    @ReadOnlyComposable
    get() {
        val customization = LocalConfiguration.current.customization
        // 动画全关 → 视为低配设备，不做实时模糊
        val lowPerf = customization.animationLevel == AnimationLevel.Off
        return customization.glassEnabled && !lowPerf
    }

/**
 * 创建并向下提供 HazeState。
 *
 * 包裹在页面根节点，页面内的 [wearBiliGlass] 即可自动生效。
 */
@Composable
fun ProvideHazeState(
    content: @Composable () -> Unit
) {
    val hazeState = rememberHazeState()
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        content()
    }
}

/**
 * 标记「作为模糊采样源」的内容层。
 *
 * 放在背景内容上（如整个滚动列表容器），玻璃层才能采样到它。
 * 当玻璃效果关闭时退化为 no-op，避免无谓的离屏渲染开销。
 */
fun Modifier.hazeSourceIfEnabled(state: HazeState): Modifier = composed {
    if (isGlassEffectEnabled) {
        this.hazeSource(state)
    } else {
        this
    }
}

/**
 * 给 [Modifier] 附加液态玻璃效果。
 *
 * @param state      采样源状态；默认取最近的 [LocalHazeState]（通过 [ProvideHazeState] 提供）
 * @param level      玻璃厚度档位
 * @param shape      裁剪形状，需与外部 clip 一致
 * @param tintAlpha  玻璃底色不透明度补充（部分场景需要更实的底以保证文字可读）
 */
@Composable
fun Modifier.wearBiliGlass(
    state: HazeState? = LocalHazeState.current,
    level: GlassLevel = GlassLevel.Regular,
    shape: Shape = RoundedCornerShape(0.dp),
    tintAlpha: Float = 0f,
): Modifier {
    val enabled = isGlassEffectEnabled
    val scheme = MaterialTheme.colorScheme

    // 玻璃底色：surface + 主题色调偏移，保证在浅色/深色下都有足够对比
    val baseColor = remember(level, tintAlpha, scheme.surface) {
        scheme.surface.copy(alpha = level.baseAlpha() + tintAlpha)
    }

    val style: HazeBlurStyle = when (level) {
        GlassLevel.UltraThin -> HazeMaterials.ultraThin(containerColor = baseColor)
        GlassLevel.Thin -> HazeMaterials.thin(containerColor = baseColor)
        GlassLevel.Regular -> HazeMaterials.regular(containerColor = baseColor)
        GlassLevel.Thick -> HazeMaterials.thick(containerColor = baseColor)
    }

    return composed {
        if (!enabled || state == null) {
            // 降级：用半透明纯色模拟玻璃观感（零渲染开销）
            this.background(color = baseColor, shape = shape)
        } else {
            this.hazeBlur(
                input = state.asInput(),
                style = style,
            )
        }
    }
}

/**
 * 把 HazeState 适配成 HazeInput。
 *
 * Haze 2.0 用 HazeInput 抽象取代了 1.x 直接传 HazeState 的写法，
 * 支持多源采样与内容自采样。单源场景用 `HazeInput.Backdrop(state)`，
 * 它内部会包装为 Sources 并附带 fallback 语义（采样源尚未就绪时保持上一帧）。
 */
private fun HazeState.asInput(): HazeInput = HazeInput.Backdrop(this)

/** 各档位的基础底色不透明度 */
private fun GlassLevel.baseAlpha(): Float = when (this) {
    GlassLevel.UltraThin -> 0.20f
    GlassLevel.Thin -> 0.35f
    GlassLevel.Regular -> 0.50f
    GlassLevel.Thick -> 0.70f
}
