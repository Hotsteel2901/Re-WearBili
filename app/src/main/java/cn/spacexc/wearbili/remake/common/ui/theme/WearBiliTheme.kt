package cn.spacexc.wearbili.remake.common.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.app.settings.ProvideConfiguration
import cn.spacexc.wearbili.remake.proto.settings.Appearance
import cn.spacexc.wearbili.remake.proto.settings.DeviceLayout

/**
 * Created by XC-Qan on 2023/3/21.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 *
 * 2026 改版：
 * - 从 MaterialTheme(M2) 迁移到 Material3，支持 Monet 动态取色
 * - 保留 `AppTheme.typography.{h1,h2,h3,body1,body2}` 这一旧入口（92 处调用点），
 *   避免全量改写；这些 slot 现在映射到 M3 Typography 的对应样式
 */

/**
 * 应用主题入口（兼容层）。
 *
 * 原实现为 `typealias AppTheme = MaterialTheme`（M2）。
 * 迁移后 M3 的 Typography 没有 h1/h2/h3/body1/body2 这些 slot 名，
 * 但通过 Typography.kt 里的扩展属性补齐了同名入口，
 * 因此这里用 object 包装一层即可让 92 处旧调用继续工作。
 */
object AppTheme {
    /** 当前 M3 Typography（含 h1/h2/h3/body1/body2 兼容 slot） */
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /** 当前 M3 ColorScheme（新增，供新代码使用动态配色） */
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}

/** 是否处于深色系（深色 / 纯黑） */
@Composable
private fun isDarkAppearance(appearance: Appearance): Boolean = when (appearance) {
    Appearance.FollowSystem -> isSystemInDarkTheme()
    Appearance.AlwaysDark, Appearance.PureBlack -> true
    Appearance.AlwaysLight -> false
    else -> true // UNRECOGNIZED 保守取深色（与原 UI 硬编码深色一致）
}

/**
 * 计算当前应使用的 ColorScheme。
 *
 * 优先级：
 *  1. 纯黑模式 → PureBlackScheme（OLED 省电）
 *  2. Monet 开启且 API >= 31 → 系统动态取色
 *  3. 回落默认粉色方案（深色/浅色）
 *  4. 自定义主色覆盖 primary（仅路径 3 生效）
 */
@Composable
private fun rememberWearBiliColorScheme(
    appearance: Appearance,
    monetEnabled: Boolean,
    themeColorHex: String,
): ColorScheme {
    val context = LocalContext.current
    val dark = isDarkAppearance(appearance)
    val monetAvailable = monetEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return remember(appearance, monetEnabled, themeColorHex, dark, monetAvailable) {
        when {
            // 1. 纯黑模式优先（OLED 省电，不受 Monet 影响）
            appearance == Appearance.PureBlack -> PureBlackScheme

            // 2. Monet 动态取色
            monetAvailable ->
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

            // 3. 默认粉色方案 + 可选自定义主色
            else -> {
                val base = if (dark) DefaultPinkDarkScheme else DefaultPinkLightScheme
                themeColorHex.toComposeColorOrNull()?.let { base.withPrimary(it) } ?: base
            }
        }
    }
}

/** 解析 "#RRGGBB" / "RRGGBB" / "#AARRGGBB" 为 Color，失败返回 null */
private fun String.toComposeColorOrNull(): Color? {
    val cleaned = trim().removePrefix("#")
    if (cleaned.isEmpty()) return null
    return try {
        val argb = when (cleaned.length) {
            6 -> 0xFF000000L or cleaned.toLong(16)
            8 -> cleaned.toLong(16)
            else -> return null
        }
        Color(argb.toInt())
    } catch (_: NumberFormatException) {
        null
    }
}

/**
 * 以给定 primary 重建 ColorScheme。
 *
 * Compose 的 ColorScheme 是只读数据类，没有 copy()，
 * 需用对应构造函数重建。此处按原方案明暗自动选择构造函数。
 */
private fun ColorScheme.withPrimary(newPrimary: Color): ColorScheme {
    val isLight = background.luminance() > 0.5f
    return if (isLight) {
        androidx.compose.material3.lightColorScheme(
            primary = newPrimary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            scrim = scrim,
        )
    } else {
        androidx.compose.material3.darkColorScheme(
            primary = newPrimary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            scrim = scrim,
        )
    }
}

@Composable
fun WearBiliTheme(content: @Composable () -> Unit) {
    ProvideConfiguration {
        ProvideLocalDensity {
            val customization = LocalConfiguration.current.customization
            val colorScheme = rememberWearBiliColorScheme(
                appearance = customization.appearance,
                monetEnabled = customization.monetEnabled,
                themeColorHex = customization.themeColorHex,
            )
            // 设备布局规范：手表 / 手机两套密度与字号，动画行为保持一致
            val layoutSpec = when (customization.deviceLayout) {
                DeviceLayout.DevicePhone -> PhoneLayoutSpec
                else -> WatchLayoutSpec
            }
            CompositionLocalProvider(LocalLayoutSpec provides layoutSpec) {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = wearbiliTypography,
                ) {
                    content()
                }
            }
        }
    }
}
