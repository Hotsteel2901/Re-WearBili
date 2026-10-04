package cn.spacexc.wearbili.remake.common.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import cn.spacexc.wearbili.common.ifNullOrZero
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.proto.settings.DeviceLayout

@Composable
fun ProvideLocalDensity(
    content: @Composable () -> Unit
) {
    val scale =
        LocalConfiguration.current.screenDisplayScaleFactor.ifNullOrZero { 1f }.toFloat() //用户自行设置的
    val fontScale = LocalDensity.current.fontScale
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels.toFloat()
    val density: Float = when (LocalConfiguration.current.customization.deviceLayout) {
        DeviceLayout.DevicePhone -> {
            // 手机布局：使用设备物理密度（乘用户微调），不套手表基准放大。
            // 手机屏幕本就大，按 372px 手表基准放大反而会让字号与触控目标虚胖。
            LocalDensity.current.density * scale
        }

        else -> {
            // 手表布局：按 Oppo Watch 基准（372px）全局放大，保持既有观感
            val scaleFactor = widthPixels / 372.0f
            2f * scaleFactor * scale
        }
    }
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density,
            fontScale = fontScale
        )
    ) {
        Box {
            content()
        }
    }
}
