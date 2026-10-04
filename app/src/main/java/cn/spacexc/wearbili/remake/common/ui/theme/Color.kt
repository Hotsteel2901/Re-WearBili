package cn.spacexc.wearbili.remake.common.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Re-WearBili 主题配色系统
 *
 * 设计原则：
 * 1. 默认装扮沿用原项目的粉色基调（BilibiliPink #FE679A）
 * 2. 所有颜色集中于此，业务代码只通过 MaterialTheme.colorScheme 取色
 * 3. Monet 动态取色在 API 31+ 可用，低版本回落到此处的默认粉色方案
 * 4. 纯黑（PureBlack）模式为 OLED 手表省电设计
 */

/** B 站品牌粉 —— 默认装扮的主色 */
val BrandPink = Color(0xFFFE679A)

/** B 站品牌蓝 —— 辅助色 */
val BrandBlue = Color(0xFF00A1D6)

/** 纯黑（OLED 省电）底色 */
val OledBlack = Color(0xFF000000)

/** 深色模式底色（非纯黑，带一点层次） */
val DarkSurfaceBase = Color(0xFF121014)

/**
 * 默认深色方案 —— 粉色基调
 *
 * 原项目 UI 是硬编码深色的（Typography 全部 Color.White），
 * 因此深色方案是本项目的"原生"外观，作为默认值。
 */
val DefaultPinkDarkScheme = darkColorScheme(
    primary = BrandPink,
    onPrimary = Color(0xFF3A0018),
    primaryContainer = Color(0xFF5C1233),
    onPrimaryContainer = Color(0xFFFFD9E3),

    secondary = Color(0xFFFFB1C8),
    onSecondary = Color(0xFF5E1130),
    secondaryContainer = Color(0xFF7C2947),
    onSecondaryContainer = Color(0xFFFFD9E3),

    tertiary = BrandBlue,
    onTertiary = Color(0xFF00344A),
    tertiaryContainer = Color(0xFF004C69),
    onTertiaryContainer = Color(0xFFC5E7FF),

    background = DarkSurfaceBase,
    onBackground = Color(0xFFEDE0E3),
    surface = DarkSurfaceBase,
    onSurface = Color(0xFFEDE0E3),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFD5C2C6),

    outline = Color(0xFF9E8C90),
    outlineVariant = Color(0xFF514347),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    scrim = Color(0xFF000000),
)

/**
 * 默认浅色方案 —— 粉色基调
 */
val DefaultPinkLightScheme = lightColorScheme(
    primary = Color(0xFFB0004F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E3),
    onPrimaryContainer = Color(0xFF3F001A),

    secondary = Color(0xFF7C2947),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E3),
    onSecondaryContainer = Color(0xFF31101F),

    tertiary = Color(0xFF00658A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC5E7FF),
    onTertiaryContainer = Color(0xFF001E2C),

    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF3DDE1),
    onSurfaceVariant = Color(0xFF514347),

    outline = Color(0xFF837377),
    outlineVariant = Color(0xFFD6C2C6),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    scrim = Color(0xFF000000),
)

/**
 * 纯黑（OLED 省电）方案
 *
 * 手表多为 OLED 屏，纯黑像素不发光，可显著省电。
 * 以深色方案为基底，把 background/surface 压到纯黑。
 */
val PureBlackScheme = DefaultPinkDarkScheme.copy(
    background = OledBlack,
    surface = OledBlack,
    surfaceVariant = Color(0xFF1C1B1C),
    onSurfaceVariant = Color(0xFFCFC4C6),
)
