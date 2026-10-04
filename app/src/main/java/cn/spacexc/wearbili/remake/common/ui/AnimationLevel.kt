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
