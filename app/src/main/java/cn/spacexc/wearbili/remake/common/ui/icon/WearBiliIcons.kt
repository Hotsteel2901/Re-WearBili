package cn.spacexc.wearbili.remake.common.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Re-WearBili 自绘矢量图标库。
 *
 * 为什么要自绘而不是继续用 res/drawable 的 XML：
 * 1. **可 tint** —— 现有 XML 图标大量硬编码 `fillColor="#FFFFFF"`，无法随主题变色，
 *    在浅色模式或 Monet 动态配色下会"白图标看不清"。ImageVector + Icon(tint=) 可彻底解决。
 * 2. **体积** —— 矢量按需构建，无需为每个尺寸准备多份 PNG。
 * 3. **一致性** —— 统一 24x24 viewport、统一 2dp 描边视觉重量，风格更现代。
 *
 * 命名与设计参考 Material Symbols（Google 官方图标集）的几何规范，
 * 但路径为手绘，避免直接复制他人资产。
 *
 * 所有图标均为 fill 型（非描边型），确保在小尺寸手表屏幕上依然清晰。
 */

private const val VIEWPORT = 24f

/** 图标容器 —— 集中暴露全部自绘图标，用法：`Icon(WearBiliIcons.Home, ...)` */
object WearBiliIcons

/** 构建图标的通用辅助，统一 viewport 与默认尺寸 */
private fun wearbiliIcon(
    name: String,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT
).block().build()

// ---------------------------------------------------------------------------
// 底部导航 / 主功能
// ---------------------------------------------------------------------------

/** 首页 —— 现代几何风格房屋 */
val WearBiliIcons.Home: ImageVector
    get() = wearbiliIcon("Home") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 3.2f)
            curveTo(11.7f, 3.2f, 11.45f, 3.3f, 11.25f, 3.46f)
            lineTo(3.25f, 9.86f)
            curveTo(2.95f, 10.1f, 2.9f, 10.54f, 3.14f, 10.84f)
            curveTo(3.38f, 11.14f, 3.82f, 11.19f, 4.12f, 10.95f)
            lineTo(5f, 10.25f)
            verticalLineTo(19.5f)
            curveTo(5f, 20.6f, 5.9f, 21.5f, 7f, 21.5f)
            horizontalLineTo(10f)
            verticalLineTo(16f)
            curveTo(10f, 15.45f, 10.45f, 15f, 11f, 15f)
            horizontalLineTo(13f)
            curveTo(13.55f, 15f, 14f, 15.45f, 14f, 16f)
            verticalLineTo(21.5f)
            horizontalLineTo(17f)
            curveTo(18.1f, 21.5f, 19f, 20.6f, 19f, 19.5f)
            verticalLineTo(10.25f)
            lineTo(19.88f, 10.95f)
            curveTo(20.18f, 11.19f, 20.62f, 11.14f, 20.86f, 10.84f)
            curveTo(21.1f, 10.54f, 21.05f, 10.1f, 20.75f, 9.86f)
            lineTo(12.75f, 3.46f)
            curveTo(12.55f, 3.3f, 12.3f, 3.2f, 12f, 3.2f)
            close()
        }
    }

/** 动态 —— 播放/脉冲，呼应"动态"语义 */
val WearBiliIcons.Dynamic: ImageVector
    get() = wearbiliIcon("Dynamic") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2.5f)
            curveTo(6.75f, 2.5f, 2.5f, 6.75f, 2.5f, 12f)
            curveTo(2.5f, 17.25f, 6.75f, 21.5f, 12f, 21.5f)
            curveTo(17.25f, 21.5f, 21.5f, 17.25f, 21.5f, 12f)
            curveTo(21.5f, 6.75f, 17.25f, 2.5f, 12f, 2.5f)
            close()
            moveTo(12f, 4.5f)
            curveTo(16.15f, 4.5f, 19.5f, 7.85f, 19.5f, 12f)
            curveTo(19.5f, 16.15f, 16.15f, 19.5f, 12f, 19.5f)
            curveTo(7.85f, 19.5f, 4.5f, 16.15f, 4.5f, 12f)
            curveTo(4.5f, 7.85f, 7.85f, 4.5f, 12f, 4.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(10f, 8.5f)
            lineTo(16f, 12f)
            lineTo(10f, 15.5f)
            close()
        }
    }

/** 个人中心 */
val WearBiliIcons.Profile: ImageVector
    get() = wearbiliIcon("Profile") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2.5f)
            curveTo(9.52f, 2.5f, 7.5f, 4.52f, 7.5f, 7f)
            curveTo(7.5f, 9.48f, 9.52f, 11.5f, 12f, 11.5f)
            curveTo(14.48f, 11.5f, 16.5f, 9.48f, 16.5f, 7f)
            curveTo(16.5f, 4.52f, 14.48f, 2.5f, 12f, 2.5f)
            close()
            moveTo(12f, 4.5f)
            curveTo(13.38f, 4.5f, 14.5f, 5.62f, 14.5f, 7f)
            curveTo(14.5f, 8.38f, 13.38f, 9.5f, 12f, 9.5f)
            curveTo(10.62f, 9.5f, 9.5f, 8.38f, 9.5f, 7f)
            curveTo(9.5f, 5.62f, 10.62f, 4.5f, 12f, 4.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 13f)
            curveTo(7.86f, 13f, 4.5f, 15.24f, 4.5f, 18f)
            verticalLineTo(20.5f)
            curveTo(4.5f, 21.05f, 4.95f, 21.5f, 5.5f, 21.5f)
            horizontalLineTo(18.5f)
            curveTo(19.05f, 21.5f, 19.5f, 21.05f, 19.5f, 20.5f)
            verticalLineTo(18f)
            curveTo(19.5f, 15.24f, 16.14f, 13f, 12f, 13f)
            close()
            moveTo(12f, 15f)
            curveTo(15.42f, 15f, 17.5f, 16.63f, 17.5f, 18f)
            verticalLineTo(19.5f)
            horizontalLineTo(6.5f)
            verticalLineTo(18f)
            curveTo(6.5f, 16.63f, 8.58f, 15f, 12f, 15f)
            close()
        }
    }

// ---------------------------------------------------------------------------
// 设置项
// ---------------------------------------------------------------------------

/** 播放选项 —— 播放器 + 滑块 */
val WearBiliIcons.PlayerSettings: ImageVector
    get() = wearbiliIcon("PlayerSettings") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 3.5f)
            curveTo(3.17f, 3.5f, 2.5f, 4.17f, 2.5f, 5f)
            verticalLineTo(17f)
            curveTo(2.5f, 17.83f, 3.17f, 18.5f, 4f, 18.5f)
            horizontalLineTo(20f)
            curveTo(20.83f, 18.5f, 21.5f, 17.83f, 21.5f, 17f)
            verticalLineTo(5f)
            curveTo(21.5f, 4.17f, 20.83f, 3.5f, 20f, 3.5f)
            close()
            moveTo(4f, 5.5f)
            horizontalLineTo(20f)
            verticalLineTo(16.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(10.5f, 7.5f)
            lineTo(15f, 11f)
            lineTo(10.5f, 14.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 20f)
            horizontalLineTo(21f)
            verticalLineTo(22f)
            horizontalLineTo(3f)
            close()
        }
    }

/** 界面缩放 */
val WearBiliIcons.Scale: ImageVector
    get() = wearbiliIcon("Scale") {
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 3f)
            horizontalLineTo(10f)
            verticalLineTo(5.5f)
            horizontalLineTo(5.5f)
            verticalLineTo(10f)
            horizontalLineTo(3f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(14f, 3f)
            horizontalLineTo(21f)
            verticalLineTo(10f)
            horizontalLineTo(18.5f)
            verticalLineTo(5.5f)
            horizontalLineTo(14f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 14f)
            horizontalLineTo(5.5f)
            verticalLineTo(18.5f)
            horizontalLineTo(10f)
            verticalLineTo(21f)
            horizontalLineTo(3f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(18.5f, 14f)
            horizontalLineTo(21f)
            verticalLineTo(21f)
            horizontalLineTo(14f)
            verticalLineTo(18.5f)
            horizontalLineTo(18.5f)
            close()
        }
    }

/** 快捷功能 —— 网格 + 星标 */
val WearBiliIcons.QuickAccess: ImageVector
    get() = wearbiliIcon("QuickAccess") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 3.5f)
            curveTo(3.17f, 3.5f, 2.5f, 4.17f, 2.5f, 5f)
            verticalLineTo(9f)
            curveTo(2.5f, 9.83f, 3.17f, 10.5f, 4f, 10.5f)
            horizontalLineTo(8f)
            curveTo(8.83f, 10.5f, 9.5f, 9.83f, 9.5f, 9f)
            verticalLineTo(5f)
            curveTo(9.5f, 4.17f, 8.83f, 3.5f, 8f, 3.5f)
            close()
            moveTo(4f, 5.5f)
            horizontalLineTo(8f)
            verticalLineTo(8.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 13.5f)
            curveTo(3.17f, 13.5f, 2.5f, 14.17f, 2.5f, 15f)
            verticalLineTo(19f)
            curveTo(2.5f, 19.83f, 3.17f, 20.5f, 4f, 20.5f)
            horizontalLineTo(8f)
            curveTo(8.83f, 20.5f, 9.5f, 19.83f, 9.5f, 19f)
            verticalLineTo(15f)
            curveTo(9.5f, 14.17f, 8.83f, 13.5f, 8f, 13.5f)
            close()
            moveTo(4f, 15.5f)
            horizontalLineTo(8f)
            verticalLineTo(18.5f)
            horizontalLineTo(4f)
            close()
        }
        // 右侧星标
        path(fill = SolidColor(Color.White)) {
            moveTo(17f, 3f)
            lineTo(18.55f, 6.15f)
            lineTo(22f, 6.65f)
            lineTo(19.5f, 9.1f)
            lineTo(20.1f, 12.55f)
            lineTo(17f, 10.92f)
            lineTo(13.9f, 12.55f)
            lineTo(14.5f, 9.1f)
            lineTo(12f, 6.65f)
            lineTo(15.45f, 6.15f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(17f, 14.2f)
            lineTo(18.1f, 16.42f)
            lineTo(20.55f, 16.78f)
            lineTo(18.78f, 18.51f)
            lineTo(19.2f, 20.95f)
            lineTo(17f, 19.8f)
            lineTo(14.8f, 20.95f)
            lineTo(15.22f, 18.51f)
            lineTo(13.45f, 16.78f)
            lineTo(15.9f, 16.42f)
            close()
        }
    }

/** 个性化 —— 调色板 */
val WearBiliIcons.Personalization: ImageVector
    get() = wearbiliIcon("Personalization") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2.5f)
            curveTo(6.75f, 2.5f, 2.5f, 6.75f, 2.5f, 12f)
            curveTo(2.5f, 17.25f, 6.75f, 21.5f, 12f, 21.5f)
            curveTo(12.83f, 21.5f, 13.5f, 20.83f, 13.5f, 20f)
            curveTo(13.5f, 19.6f, 13.34f, 19.24f, 13.09f, 18.98f)
            curveTo(12.84f, 18.72f, 12.69f, 18.37f, 12.69f, 18f)
            curveTo(12.69f, 17.17f, 13.36f, 16.5f, 14.19f, 16.5f)
            horizontalLineTo(16f)
            curveTo(19.04f, 16.5f, 21.5f, 14.04f, 21.5f, 11f)
            curveTo(21.5f, 6.3f, 17.25f, 2.5f, 12f, 2.5f)
            close()
            moveTo(12f, 4.5f)
            curveTo(16.14f, 4.5f, 19.5f, 7.41f, 19.5f, 11f)
            curveTo(19.5f, 12.93f, 17.93f, 14.5f, 16f, 14.5f)
            horizontalLineTo(14.19f)
            curveTo(12.27f, 14.5f, 10.69f, 16.04f, 10.69f, 18f)
            curveTo(10.69f, 18.76f, 10.94f, 19.45f, 11.36f, 20.01f)
            curveTo(7.64f, 19.6f, 4.5f, 16.19f, 4.5f, 12f)
            curveTo(4.5f, 7.85f, 7.85f, 4.5f, 12f, 4.5f)
            close()
        }
        // 三个色点，构成调色板意象
        path(fill = SolidColor(Color.White)) {
            moveTo(8f, 7f)
            curveTo(6.9f, 7f, 6f, 7.9f, 6f, 9f)
            curveTo(6f, 10.1f, 6.9f, 11f, 8f, 11f)
            curveTo(9.1f, 11f, 10f, 10.1f, 10f, 9f)
            curveTo(10f, 7.9f, 9.1f, 7f, 8f, 7f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 5.5f)
            curveTo(10.9f, 5.5f, 10f, 6.4f, 10f, 7.5f)
            curveTo(10f, 8.6f, 10.9f, 9.5f, 12f, 9.5f)
            curveTo(13.1f, 9.5f, 14f, 8.6f, 14f, 7.5f)
            curveTo(14f, 6.4f, 13.1f, 5.5f, 12f, 5.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(16f, 7f)
            curveTo(14.9f, 7f, 14f, 7.9f, 14f, 9f)
            curveTo(14f, 10.1f, 14.9f, 11f, 16f, 11f)
            curveTo(17.1f, 11f, 18f, 10.1f, 18f, 9f)
            curveTo(18f, 7.9f, 17.1f, 7f, 16f, 7f)
            close()
        }
    }

/** 实验功能 —— 圆底烧瓶 */
val WearBiliIcons.Experimental: ImageVector
    get() = wearbiliIcon("Experimental") {
        path(fill = SolidColor(Color.White)) {
            moveTo(9.5f, 2.5f)
            curveTo(9.22f, 2.5f, 9f, 2.72f, 9f, 3f)
            verticalLineTo(3.5f)
            curveTo(9f, 3.78f, 9.22f, 4f, 9.5f, 4f)
            horizontalLineTo(10f)
            verticalLineTo(9.12f)
            lineTo(4.28f, 18.66f)
            curveTo(3.55f, 19.9f, 4.45f, 21.5f, 5.9f, 21.5f)
            horizontalLineTo(18.1f)
            curveTo(19.55f, 21.5f, 20.45f, 19.9f, 19.72f, 18.66f)
            lineTo(14f, 9.12f)
            verticalLineTo(4f)
            horizontalLineTo(14.5f)
            curveTo(14.78f, 4f, 15f, 3.78f, 15f, 3.5f)
            verticalLineTo(3f)
            curveTo(15f, 2.72f, 14.78f, 2.5f, 14.5f, 2.5f)
            close()
            moveTo(12f, 4f)
            horizontalLineTo(12f)
            verticalLineTo(9.5f)
            lineTo(15.42f, 15.5f)
            horizontalLineTo(8.58f)
            lineTo(12f, 9.5f)
            close()
            moveTo(7.42f, 17.5f)
            horizontalLineTo(16.58f)
            lineTo(17.95f, 19.9f)
            curveTo(18.08f, 20.13f, 17.92f, 20.5f, 18.1f, 20.5f)
            horizontalLineTo(5.9f)
            curveTo(6.08f, 20.5f, 5.92f, 20.13f, 6.05f, 19.9f)
            close()
        }
    }

/** 反馈中心 —— 对话气泡 + 感叹 */
val WearBiliIcons.Feedback: ImageVector
    get() = wearbiliIcon("Feedback") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 3.5f)
            curveTo(2.62f, 3.5f, 1.5f, 4.62f, 1.5f, 6f)
            verticalLineTo(16f)
            curveTo(1.5f, 17.38f, 2.62f, 18.5f, 4f, 18.5f)
            horizontalLineTo(7f)
            verticalLineTo(21.5f)
            lineTo(11.5f, 18.5f)
            horizontalLineTo(20f)
            curveTo(21.38f, 18.5f, 22.5f, 17.38f, 22.5f, 16f)
            verticalLineTo(6f)
            curveTo(22.5f, 4.62f, 21.38f, 3.5f, 20f, 3.5f)
            close()
            moveTo(4f, 5.5f)
            horizontalLineTo(20f)
            verticalLineTo(16.5f)
            horizontalLineTo(10.5f)
            lineTo(8.5f, 17.9f)
            verticalLineTo(16.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 7f)
            horizontalLineTo(13f)
            verticalLineTo(12f)
            horizontalLineTo(11f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 13.5f)
            horizontalLineTo(13f)
            verticalLineTo(15f)
            horizontalLineTo(11f)
            close()
        }
    }

// ---------------------------------------------------------------------------
// 播放控制 / 视频相关
// ---------------------------------------------------------------------------

/** 播放 */
val WearBiliIcons.Play: ImageVector
    get() = wearbiliIcon("Play") {
        path(fill = SolidColor(Color.White)) {
            moveTo(7f, 4.5f)
            verticalLineTo(19.5f)
            lineTo(19f, 12f)
            close()
        }
    }

/** 暂停 */
val WearBiliIcons.Pause: ImageVector
    get() = wearbiliIcon("Pause") {
        path(fill = SolidColor(Color.White)) {
            moveTo(7f, 4.5f)
            horizontalLineTo(10.5f)
            verticalLineTo(19.5f)
            horizontalLineTo(7f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(13.5f, 4.5f)
            horizontalLineTo(17f)
            verticalLineTo(19.5f)
            horizontalLineTo(13.5f)
            close()
        }
    }

/** 弹幕 */
val WearBiliIcons.Danmaku: ImageVector
    get() = wearbiliIcon("Danmaku") {
        path(fill = SolidColor(Color.White)) {
            moveTo(3.5f, 4.5f)
            curveTo(2.67f, 4.5f, 2f, 5.17f, 2f, 6f)
            verticalLineTo(16f)
            curveTo(2f, 16.83f, 2.67f, 17.5f, 3.5f, 17.5f)
            horizontalLineTo(9f)
            verticalLineTo(20f)
            horizontalLineTo(11.5f)
            lineTo(13.5f, 17.5f)
            horizontalLineTo(20.5f)
            curveTo(21.33f, 17.5f, 22f, 16.83f, 22f, 16f)
            verticalLineTo(6f)
            curveTo(22f, 5.17f, 21.33f, 4.5f, 20.5f, 4.5f)
            close()
            moveTo(4f, 6.5f)
            horizontalLineTo(20f)
            verticalLineTo(15.5f)
            horizontalLineTo(12.5f)
            lineTo(10.5f, 18f)
            verticalLineTo(15.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 8.5f)
            horizontalLineTo(14f)
            verticalLineTo(10f)
            horizontalLineTo(6f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 12f)
            horizontalLineTo(18f)
            verticalLineTo(13.5f)
            horizontalLineTo(6f)
            close()
        }
    }

/** 清晰度 / 画质 */
val WearBiliIcons.Quality: ImageVector
    get() = wearbiliIcon("Quality") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2.5f)
            lineTo(3f, 6.5f)
            verticalLineTo(17.5f)
            lineTo(12f, 21.5f)
            lineTo(21f, 17.5f)
            verticalLineTo(6.5f)
            close()
            moveTo(12f, 4.7f)
            lineTo(19f, 7.8f)
            verticalLineTo(16.2f)
            lineTo(12f, 19.3f)
            lineTo(5f, 16.2f)
            verticalLineTo(7.8f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 7.5f)
            horizontalLineTo(13f)
            verticalLineTo(12.5f)
            horizontalLineTo(11f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 14f)
            horizontalLineTo(13f)
            verticalLineTo(16f)
            horizontalLineTo(11f)
            close()
        }
    }

/** 倍速 */
val WearBiliIcons.Speed: ImageVector
    get() = wearbiliIcon("Speed") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 3.5f)
            curveTo(7.3f, 3.5f, 3.5f, 7.3f, 3.5f, 12f)
            curveTo(3.5f, 16.7f, 7.3f, 20.5f, 12f, 20.5f)
            curveTo(16.7f, 20.5f, 20.5f, 16.7f, 20.5f, 12f)
            curveTo(20.5f, 7.3f, 16.7f, 3.5f, 12f, 3.5f)
            close()
            moveTo(12f, 5.5f)
            curveTo(15.59f, 5.5f, 18.5f, 8.41f, 18.5f, 12f)
            curveTo(18.5f, 15.59f, 15.59f, 18.5f, 12f, 18.5f)
            curveTo(8.41f, 18.5f, 5.5f, 15.59f, 5.5f, 12f)
            curveTo(5.5f, 8.41f, 8.41f, 5.5f, 12f, 5.5f)
            close()
        }
        // 指针
        path(fill = SolidColor(Color.White)) {
            moveTo(16.2f, 9.2f)
            lineTo(11.3f, 12.7f)
            curveTo(11.12f, 12.83f, 11f, 13.04f, 11f, 13.27f)
            curveTo(11f, 13.67f, 11.33f, 14f, 11.73f, 14f)
            curveTo(11.9f, 14f, 12.06f, 13.94f, 12.18f, 13.84f)
            lineTo(16.2f, 10.8f)
            close()
        }
    }

/** 收藏 */
val WearBiliIcons.Favourite: ImageVector
    get() = wearbiliIcon("Favourite") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 20.3f)
            lineTo(10.55f, 18.98f)
            curveTo(5.4f, 14.36f, 2f, 11.28f, 2f, 7.5f)
            curveTo(2f, 4.42f, 4.42f, 2f, 7.5f, 2f)
            curveTo(9.24f, 2f, 10.91f, 2.81f, 12f, 4.09f)
            curveTo(13.09f, 2.81f, 14.76f, 2f, 16.5f, 2f)
            curveTo(19.58f, 2f, 22f, 4.42f, 22f, 7.5f)
            curveTo(22f, 11.28f, 18.6f, 14.36f, 13.45f, 18.99f)
            close()
            moveTo(7.5f, 4f)
            curveTo(5.52f, 4f, 4f, 5.52f, 4f, 7.5f)
            curveTo(4f, 10.4f, 6.86f, 13.02f, 12f, 17.65f)
            curveTo(17.14f, 13.02f, 20f, 10.4f, 20f, 7.5f)
            curveTo(20f, 5.52f, 18.48f, 4f, 16.5f, 4f)
            curveTo(14.96f, 4f, 13.46f, 4.99f, 12.9f, 6.36f)
            horizontalLineTo(11.1f)
            curveTo(10.54f, 4.99f, 9.04f, 4f, 7.5f, 4f)
            close()
        }
    }

/** 搜索 */
val WearBiliIcons.Search: ImageVector
    get() = wearbiliIcon("Search") {
        path(fill = SolidColor(Color.White)) {
            moveTo(10.5f, 3f)
            curveTo(6.36f, 3f, 3f, 6.36f, 3f, 10.5f)
            curveTo(3f, 14.64f, 6.36f, 18f, 10.5f, 18f)
            curveTo(12.2f, 18f, 13.77f, 17.43f, 15.03f, 16.47f)
            lineTo(19.28f, 20.72f)
            lineTo(20.72f, 19.28f)
            lineTo(16.47f, 15.03f)
            curveTo(17.43f, 13.77f, 18f, 12.2f, 18f, 10.5f)
            curveTo(18f, 6.36f, 14.64f, 3f, 10.5f, 3f)
            close()
            moveTo(10.5f, 5f)
            curveTo(13.54f, 5f, 16f, 7.46f, 16f, 10.5f)
            curveTo(16f, 13.54f, 13.54f, 16f, 10.5f, 16f)
            curveTo(7.46f, 16f, 5f, 13.54f, 5f, 10.5f)
            curveTo(5f, 7.46f, 7.46f, 5f, 10.5f, 5f)
            close()
        }
    }

/** 历史记录 */
val WearBiliIcons.History: ImageVector
    get() = wearbiliIcon("History") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 3f)
            curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
            curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
            curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
            curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
            close()
            moveTo(12f, 5f)
            curveTo(15.87f, 5f, 19f, 8.13f, 19f, 12f)
            curveTo(19f, 15.87f, 15.87f, 19f, 12f, 19f)
            curveTo(8.13f, 19f, 5f, 15.87f, 5f, 12f)
            curveTo(5f, 8.13f, 8.13f, 5f, 12f, 5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 7f)
            horizontalLineTo(12.75f)
            verticalLineTo(12.6f)
            lineTo(17f, 15.1f)
            lineTo(16.1f, 16.55f)
            lineTo(11f, 13.5f)
            close()
        }
    }

/** 缓存 / 下载 */
val WearBiliIcons.Download: ImageVector
    get() = wearbiliIcon("Download") {
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 3f)
            horizontalLineTo(13f)
            verticalLineTo(12.2f)
            lineTo(16.6f, 8.6f)
            lineTo(18f, 10f)
            lineTo(12f, 16f)
            lineTo(6f, 10f)
            lineTo(7.4f, 8.6f)
            lineTo(11f, 12.2f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 18f)
            horizontalLineTo(20f)
            verticalLineTo(20.5f)
            horizontalLineTo(4f)
            close()
        }
    }

/** 消息 */
val WearBiliIcons.Message: ImageVector
    get() = wearbiliIcon("Message") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 3.5f)
            curveTo(2.62f, 3.5f, 1.5f, 4.62f, 1.5f, 6f)
            verticalLineTo(16f)
            curveTo(1.5f, 17.38f, 2.62f, 18.5f, 4f, 18.5f)
            horizontalLineTo(7f)
            verticalLineTo(21.5f)
            lineTo(11.5f, 18.5f)
            horizontalLineTo(20f)
            curveTo(21.38f, 18.5f, 22.5f, 17.38f, 22.5f, 16f)
            verticalLineTo(6f)
            curveTo(22.5f, 4.62f, 21.38f, 3.5f, 20f, 3.5f)
            close()
            moveTo(4f, 5.5f)
            horizontalLineTo(20f)
            verticalLineTo(16.5f)
            horizontalLineTo(10.5f)
            lineTo(8.5f, 17.9f)
            verticalLineTo(16.5f)
            horizontalLineTo(4f)
            close()
        }
    }

/** 设置（齿轮） */
val WearBiliIcons.Settings: ImageVector
    get() = wearbiliIcon("Settings") {
        // 外圈：八齿齿轮轮廓（几何近似，视觉重量与 Material Symbols 接近）
        path(fill = SolidColor(Color.White)) {
            moveTo(10.6f, 2.5f)
            horizontalLineTo(13.4f)
            lineTo(13.86f, 5.06f)
            curveTo(14.42f, 5.24f, 14.95f, 5.5f, 15.43f, 5.82f)
            lineTo(17.86f, 4.78f)
            lineTo(19.26f, 7.21f)
            lineTo(17.3f, 8.84f)
            curveTo(17.36f, 9.2f, 17.4f, 9.6f, 17.4f, 10f)
            curveTo(17.4f, 10.4f, 17.36f, 10.8f, 17.3f, 11.16f)
            lineTo(19.26f, 12.79f)
            lineTo(17.86f, 15.22f)
            lineTo(15.43f, 14.18f)
            curveTo(14.95f, 14.5f, 14.42f, 14.76f, 13.86f, 14.94f)
            lineTo(13.4f, 17.5f)
            horizontalLineTo(10.6f)
            lineTo(10.14f, 14.94f)
            curveTo(9.58f, 14.76f, 9.05f, 14.5f, 8.57f, 14.18f)
            lineTo(6.14f, 15.22f)
            lineTo(4.74f, 12.79f)
            lineTo(6.7f, 11.16f)
            curveTo(6.64f, 10.8f, 6.6f, 10.4f, 6.6f, 10f)
            curveTo(6.6f, 9.6f, 6.64f, 9.2f, 6.7f, 8.84f)
            lineTo(4.74f, 7.21f)
            lineTo(6.14f, 4.78f)
            lineTo(8.57f, 5.82f)
            curveTo(9.05f, 5.5f, 9.58f, 5.24f, 10.14f, 5.06f)
            close()
        }
        // 内圈挖空：用 evenOdd 形成孔洞效果
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 1f,
            pathFillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
        ) {
            moveTo(12f, 6.6f)
            curveTo(9.02f, 6.6f, 6.6f, 9.02f, 6.6f, 12f)
            curveTo(6.6f, 14.98f, 9.02f, 17.4f, 12f, 17.4f)
            curveTo(14.98f, 17.4f, 17.4f, 14.98f, 17.4f, 12f)
            curveTo(17.4f, 9.02f, 14.98f, 6.6f, 12f, 6.6f)
            close()
            moveTo(12f, 8.6f)
            curveTo(13.88f, 8.6f, 15.4f, 10.12f, 15.4f, 12f)
            curveTo(15.4f, 13.88f, 13.88f, 15.4f, 12f, 15.4f)
            curveTo(10.12f, 15.4f, 8.6f, 13.88f, 8.6f, 12f)
            curveTo(8.6f, 10.12f, 10.12f, 8.6f, 12f, 8.6f)
            close()
        }
    }

/** 分享 */
val WearBiliIcons.Share: ImageVector
    get() = wearbiliIcon("Share") {
        path(fill = SolidColor(Color.White)) {
            moveTo(18f, 16f)
            curveTo(17.24f, 16f, 16.56f, 16.3f, 16.04f, 16.78f)
            lineTo(8.91f, 12.88f)
            curveTo(8.96f, 12.6f, 9f, 12.3f, 9f, 12f)
            curveTo(9f, 11.7f, 8.96f, 11.4f, 8.91f, 11.12f)
            lineTo(15.96f, 7.26f)
            curveTo(16.5f, 7.72f, 17.21f, 8f, 18f, 8f)
            curveTo(19.66f, 8f, 21f, 6.66f, 21f, 5f)
            curveTo(21f, 3.34f, 19.66f, 2f, 18f, 2f)
            curveTo(16.34f, 2f, 15f, 3.34f, 15f, 5f)
            curveTo(15f, 5.3f, 15.04f, 5.6f, 15.09f, 5.88f)
            lineTo(8.04f, 9.74f)
            curveTo(7.5f, 9.28f, 6.79f, 9f, 6f, 9f)
            curveTo(4.34f, 9f, 3f, 10.34f, 3f, 12f)
            curveTo(3f, 13.66f, 4.34f, 15f, 6f, 15f)
            curveTo(6.79f, 15f, 7.5f, 14.72f, 8.04f, 14.26f)
            lineTo(15.16f, 18.16f)
            curveTo(15.11f, 18.42f, 15.09f, 18.69f, 15.09f, 18.96f)
            curveTo(15.09f, 20.57f, 16.39f, 21.87f, 18f, 21.87f)
            curveTo(19.61f, 21.87f, 20.91f, 20.57f, 20.91f, 18.96f)
            curveTo(20.91f, 17.35f, 19.61f, 16f, 18f, 16f)
            close()
        }
    }

/** 点赞 */
val WearBiliIcons.Like: ImageVector
    get() = wearbiliIcon("Like") {
        path(fill = SolidColor(Color.White)) {
            moveTo(9f, 21.5f)
            horizontalLineTo(18.4f)
            curveTo(19.2f, 21.5f, 19.89f, 20.97f, 20.08f, 20.2f)
            lineTo(22.4f, 11.7f)
            curveTo(22.72f, 10.5f, 21.78f, 9.4f, 20.72f, 9.4f)
            horizontalLineTo(14.6f)
            lineTo(15.6f, 4.6f)
            curveTo(15.75f, 3.9f, 15.2f, 3.3f, 14.5f, 3.3f)
            curveTo(14.1f, 3.3f, 13.75f, 3.5f, 13.5f, 3.85f)
            lineTo(9.06f, 10.5f)
            horizontalLineTo(9f)
            close()
        }
    }

/** 评论 */
val WearBiliIcons.Comment: ImageVector
    get() = wearbiliIcon("Comment") {
        path(fill = SolidColor(Color.White)) {
            moveTo(3.5f, 4f)
            curveTo(2.67f, 4f, 2f, 4.67f, 2f, 5.5f)
            verticalLineTo(17.5f)
            curveTo(2f, 18.33f, 2.67f, 19f, 3.5f, 19f)
            horizontalLineTo(7f)
            verticalLineTo(22f)
            lineTo(11.5f, 19f)
            horizontalLineTo(20.5f)
            curveTo(21.33f, 19f, 22f, 18.33f, 22f, 17.5f)
            verticalLineTo(5.5f)
            curveTo(22f, 4.67f, 21.33f, 4f, 20.5f, 4f)
            close()
            moveTo(4f, 6f)
            horizontalLineTo(20f)
            verticalLineTo(17f)
            horizontalLineTo(10.5f)
            lineTo(9f, 18.1f)
            verticalLineTo(17f)
            horizontalLineTo(4f)
            close()
        }
    }

// ---------------------------------------------------------------------------
// 通用操作
// ---------------------------------------------------------------------------

/** 返回（左箭头） */
val WearBiliIcons.ArrowBack: ImageVector
    get() = wearbiliIcon("ArrowBack") {
        path(fill = SolidColor(Color.White)) {
            moveTo(11f, 4f)
            lineTo(12.4f, 5.4f)
            lineTo(6.8f, 11f)
            horizontalLineTo(20f)
            verticalLineTo(13f)
            horizontalLineTo(6.8f)
            lineTo(12.4f, 18.6f)
            lineTo(11f, 20f)
            lineTo(3f, 12f)
            close()
        }
    }

/** 展开（下箭头） */
val WearBiliIcons.ExpandMore: ImageVector
    get() = wearbiliIcon("ExpandMore") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 15.1f)
            lineTo(6.4f, 9.5f)
            lineTo(7.8f, 8.1f)
            lineTo(12f, 12.3f)
            lineTo(16.2f, 8.1f)
            lineTo(17.6f, 9.5f)
            close()
        }
    }

/** 关闭 */
val WearBiliIcons.Close: ImageVector
    get() = wearbiliIcon("Close") {
        path(fill = SolidColor(Color.White)) {
            moveTo(6.4f, 5f)
            lineTo(12f, 10.6f)
            lineTo(17.6f, 5f)
            lineTo(19f, 6.4f)
            lineTo(13.4f, 12f)
            lineTo(19f, 17.6f)
            lineTo(17.6f, 19f)
            lineTo(12f, 13.4f)
            lineTo(6.4f, 19f)
            lineTo(5f, 17.6f)
            lineTo(10.6f, 12f)
            lineTo(5f, 6.4f)
            close()
        }
    }

/** 勾选 */
val WearBiliIcons.Check: ImageVector
    get() = wearbiliIcon("Check") {
        path(fill = SolidColor(Color.White)) {
            moveTo(9.55f, 17.6f)
            lineTo(4f, 12.05f)
            lineTo(5.4f, 10.65f)
            lineTo(9.55f, 14.8f)
            lineTo(18.6f, 5.75f)
            lineTo(20f, 7.15f)
            close()
        }
    }

/** 更多（三点） */
val WearBiliIcons.More: ImageVector
    get() = wearbiliIcon("More") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 10f)
            curveTo(10.9f, 10f, 10f, 10.9f, 10f, 12f)
            curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f)
            curveTo(13.1f, 14f, 14f, 13.1f, 14f, 12f)
            curveTo(14f, 10.9f, 13.1f, 10f, 12f, 10f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(5f, 10f)
            curveTo(3.9f, 10f, 3f, 10.9f, 3f, 12f)
            curveTo(3f, 13.1f, 3.9f, 14f, 5f, 14f)
            curveTo(6.1f, 14f, 7f, 13.1f, 7f, 12f)
            curveTo(7f, 10.9f, 6.1f, 10f, 5f, 10f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(19f, 10f)
            curveTo(17.9f, 10f, 17f, 10.9f, 17f, 12f)
            curveTo(17f, 13.1f, 17.9f, 14f, 19f, 14f)
            curveTo(20.1f, 14f, 21f, 13.1f, 21f, 12f)
            curveTo(21f, 10.9f, 20.1f, 10f, 19f, 10f)
            close()
        }
    }

/** 刷新 */
val WearBiliIcons.Refresh: ImageVector
    get() = wearbiliIcon("Refresh") {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 4f)
            curveTo(8.69f, 4f, 5.82f, 5.99f, 4.5f, 8.85f)
            lineTo(6.32f, 9.7f)
            curveTo(7.3f, 7.5f, 9.5f, 6f, 12f, 6f)
            curveTo(13.9f, 6f, 15.6f, 6.83f, 16.8f, 8.14f)
            lineTo(13.5f, 11.5f)
            horizontalLineTo(20f)
            verticalLineTo(5f)
            lineTo(17.14f, 7.86f)
            curveTo(15.66f, 5.6f, 13.96f, 4f, 12f, 4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 20f)
            curveTo(15.31f, 20f, 18.18f, 18.01f, 19.5f, 15.15f)
            lineTo(17.68f, 14.3f)
            curveTo(16.7f, 16.5f, 14.5f, 18f, 12f, 18f)
            curveTo(10.1f, 18f, 8.4f, 17.17f, 7.2f, 15.86f)
            lineTo(10.5f, 12.5f)
            horizontalLineTo(4f)
            verticalLineTo(19f)
            lineTo(6.86f, 16.14f)
            curveTo(8.34f, 18.4f, 10.04f, 20f, 12f, 20f)
            close()
        }
    }

/** 音量 */
val WearBiliIcons.Volume: ImageVector
    get() = wearbiliIcon("Volume") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 9f)
            verticalLineTo(15f)
            horizontalLineTo(8f)
            lineTo(13f, 19f)
            verticalLineTo(5f)
            lineTo(8f, 9f)
            close()
            moveTo(6f, 11f)
            horizontalLineTo(8.5f)
            lineTo(11f, 8.9f)
            verticalLineTo(15.1f)
            lineTo(8.5f, 13f)
            horizontalLineTo(6f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(15.5f, 9.2f)
            curveTo(16.4f, 10.1f, 16.4f, 13.9f, 15.5f, 14.8f)
            lineTo(16.9f, 16.2f)
            curveTo(18.6f, 14.5f, 18.6f, 9.5f, 16.9f, 7.8f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(18.6f, 6.1f)
            curveTo(21.4f, 8.9f, 21.4f, 15.1f, 18.6f, 17.9f)
            lineTo(20f, 19.3f)
            curveTo(23.5f, 15.8f, 23.5f, 8.2f, 20f, 4.7f)
            close()
        }
    }

/** 全屏 */
val WearBiliIcons.Fullscreen: ImageVector
    get() = wearbiliIcon("Fullscreen") {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 4f)
            horizontalLineTo(9.5f)
            verticalLineTo(6f)
            horizontalLineTo(6f)
            verticalLineTo(9.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(14.5f, 4f)
            horizontalLineTo(20f)
            verticalLineTo(9.5f)
            horizontalLineTo(18f)
            verticalLineTo(6f)
            horizontalLineTo(14.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 14.5f)
            horizontalLineTo(6f)
            verticalLineTo(18f)
            horizontalLineTo(9.5f)
            verticalLineTo(20f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(18f, 14.5f)
            horizontalLineTo(20f)
            verticalLineTo(20f)
            horizontalLineTo(14.5f)
            verticalLineTo(18f)
            horizontalLineTo(18f)
            close()
        }
    }

/** 字幕 */
val WearBiliIcons.Subtitle: ImageVector
    get() = wearbiliIcon("Subtitle") {
        path(fill = SolidColor(Color.White)) {
            moveTo(3.5f, 4.5f)
            curveTo(2.67f, 4.5f, 2f, 5.17f, 2f, 6f)
            verticalLineTo(18f)
            curveTo(2f, 18.83f, 2.67f, 19.5f, 3.5f, 19.5f)
            horizontalLineTo(20.5f)
            curveTo(21.33f, 19.5f, 22f, 18.83f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 5.17f, 21.33f, 4.5f, 20.5f, 4.5f)
            close()
            moveTo(4f, 6.5f)
            horizontalLineTo(20f)
            verticalLineTo(17.5f)
            horizontalLineTo(4f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(5.5f, 12f)
            horizontalLineTo(13f)
            verticalLineTo(13.5f)
            horizontalLineTo(5.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(14.5f, 12f)
            horizontalLineTo(18.5f)
            verticalLineTo(13.5f)
            horizontalLineTo(14.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(5.5f, 15f)
            horizontalLineTo(15f)
            verticalLineTo(16.5f)
            horizontalLineTo(5.5f)
            close()
        }
    }
