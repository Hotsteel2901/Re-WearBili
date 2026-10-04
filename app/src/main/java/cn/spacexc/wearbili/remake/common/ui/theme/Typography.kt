package cn.spacexc.wearbili.remake.common.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cn.spacexc.wearbili.remake.R

/**
 * Created by XC-Qan on 2023/3/21.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 *
 * 2026 改版说明：
 * - 从 M2 Typography 迁移到 M3 Typography
 * - 移除硬编码的 Color.White：颜色改由 LocalContentColor 决定，
 *   否则切换浅色主题时文字会全部不可读
 * - 原 slot 名（h1/h2/h3/body1/body2）通过扩展属性保留映射，
 *   供 87 处 AppTheme.typography.xxx 调用点继续使用
 */

val wearbiliFontFamily = FontFamily(
    Font(R.font.misans_regular, FontWeight.Normal),
    Font(R.font.misans_medium, FontWeight.Medium),
    Font(R.font.misans_bold, FontWeight.Bold),
)

/** M3 Typography，用于 MaterialTheme(typography = ...) */
val wearbiliTypography = Typography(
    // 标题类
    headlineLarge = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 17.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 15.sp,
    ),
    // 正文类
    bodyLarge = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 14.sp,
    ),
    // 标签类
    labelLarge = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontFamily = wearbiliFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
    ),
)

/**
 * 旧版 slot 名 → M3 TextStyle 的映射。
 *
 * 原项目 87 处写成 `AppTheme.typography.h1`（M2 API），
 * 迁移到 M3 后这些 slot 名不存在。为了让调用点免于全量改写，
 * 通过下面的扩展属性在 M3 Typography 上补齐同名 slot。
 *
 * 颜色刻意不在此处指定 —— 由 LocalContentColor 决定。
 */
val Typography.h1: TextStyle get() = headlineLarge
val Typography.h2: TextStyle get() = headlineMedium
val Typography.h3: TextStyle get() = headlineSmall
val Typography.body1: TextStyle get() = bodyLarge
val Typography.body2: TextStyle get() = bodyMedium
