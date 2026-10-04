package cn.spacexc.wearbili.remake.common.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.proto.settings.AnimationLevel

/*
 * 动画档位系统
 *
 * 原项目用 hasAnimation 布尔量控制动画开关（低端手表关闭动画）。
 * 2026 改版升级为 4 档枚举，既保留低端降级能力，又让高端设备能"动效拉满"。
 *
 * Off(0)      —— 无动画，最低功耗
 * Minimal(1)  —— 仅必要的淡入淡出，无弹簧
 * Standard(2) —— 标准（默认），适度弹簧
 * Full(3)     —— 拉满，Expressive 弹簧物理全开
 */

/** 当前动画档位（从配置读取） */
val currentAnimationLevel: AnimationLevel
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = LocalConfiguration.current.customization.animationLevel

/** 动画是否启用（非 Off） */
val isAnimationEnabled: Boolean
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = currentAnimationLevel != AnimationLevel.Off

/** 是否为低性能模式（Off 或 Minimal 视为低性能） */
val isLowPerformanceMode: Boolean
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = currentAnimationLevel == AnimationLevel.Off ||
            currentAnimationLevel == AnimationLevel.Minimal

/**
 * 通用弹簧规格（尺寸类变化）
 *
 * Full 档用 Expressive 的中等回弹，Standard 用轻微回弹，
 * Minimal/Off 用极快线性（近似无感）。
 */
val animationSpecSize: FiniteAnimationSpec<IntSize>
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = when (currentAnimationLevel) {
        AnimationLevel.Full -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        AnimationLevel.Standard -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        else -> tween(durationMillis = 90, easing = LinearOutSlowInEasing)
    }

/** 通用弹簧规格（位移类变化） */
val animationSpecOffset: FiniteAnimationSpec<IntOffset>
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = when (currentAnimationLevel) {
        AnimationLevel.Full -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        AnimationLevel.Standard -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        else -> tween(durationMillis = 90, easing = LinearOutSlowInEasing)
    }

/*
 * ---------------- Material 3 Expressive 动效体系 ----------------
 *
 * M3 Expressive 的核心是「motioan physics（运动物理）」：用弹簧而非固定时长曲线，
 * 让同一套动效在不同设备/帧率下保持一致的"手感"。
 *
 * 这里按动画档位给出统一入口，业务代码只引用 animationSpec* 系列，
 * 避免各处硬编码 spring(...) 导致降级策略失效。
 */

/** 默认弹簧规格（用于缩放 / 透明度 / 旋转） */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun <T> animationSpecDefault(): FiniteAnimationSpec<T> =
    when (currentAnimationLevel) {
        AnimationLevel.Full -> spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        AnimationLevel.Standard -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        else -> tween(durationMillis = 90, easing = LinearOutSlowInEasing)
    }

/** 弹性更强的"夸张"弹簧 —— 用于强调性动效（如按钮按下回弹、FAB 变形） */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun <T> animationSpecExpressive(): FiniteAnimationSpec<T> =
    when (currentAnimationLevel) {
        AnimationLevel.Full -> spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
        AnimationLevel.Standard -> spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
        else -> tween(durationMillis = 90, easing = LinearOutSlowInEasing)
    }

/** 空间位移弹簧 —— 用于共享元素、容器变换（比默认更慢更稳，避免小屏眩晕） */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun <T> animationSpecSpatial(): FiniteAnimationSpec<T> =
    when (currentAnimationLevel) {
        AnimationLevel.Full -> spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
        AnimationLevel.Standard -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        else -> tween(durationMillis = 120, easing = LinearOutSlowInEasing)
    }

/**
 * 按压反馈缩放值。
 *
 * 手表屏幕小，按压反馈是"点得到"的主要反馈手段之一，
 * 因此即便在 Minimal 档位也保留（但幅度更小）。
 */
val pressedScale: Float
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = when (currentAnimationLevel) {
        AnimationLevel.Full -> 0.90f
        AnimationLevel.Standard -> 0.94f
        AnimationLevel.Minimal -> 0.97f
        else -> 1f
    }

/**
 * 圆角变形幅度（Expressive 的特征动效）。
 *
 * Expressive 会在状态切换时让容器形状"呼吸"——例如卡片从 16dp 变到 24dp。
 * 返回开启状态下的目标圆角，关闭时返回同值即可退化为无变形。
 */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun expressiveCornerDp(
    collapsed: androidx.compose.ui.unit.Dp,
    expanded: androidx.compose.ui.unit.Dp,
): androidx.compose.ui.unit.Dp =
    if (currentAnimationLevel == AnimationLevel.Full) expanded else collapsed
