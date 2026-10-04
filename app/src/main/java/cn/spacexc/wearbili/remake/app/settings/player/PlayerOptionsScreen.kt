package cn.spacexc.wearbili.remake.app.settings.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.common.ui.TitleBackground
import cn.spacexc.wearbili.remake.common.ui.isRound
import cn.spacexc.wearbili.remake.common.ui.theme.wearbiliFontFamily
import cn.spacexc.wearbili.remake.common.ui.titleBackgroundHorizontalPadding
import cn.spacexc.wearbili.remake.proto.settings.AppConfigurationKt
import cn.spacexc.wearbili.remake.proto.settings.Player
import cn.spacexc.wearbili.remake.proto.settings.VideoDecoder
import cn.spacexc.wearbili.remake.proto.settings.VideoDisplaySurface
import cn.spacexc.wearbili.remake.proto.settings.copy
import kotlinx.coroutines.launch

/**
 * 播放选项设置页。
 *
 * 原项目中「播放选项」入口是个空壳（点击无反应），
 * 但 proto 里 player / videoDecoder / videoDisplaySurface / danmakuPlayerSettings
 * 这些字段早已被播放器代码真实消费（见 IjkVideoPlayerViewModel）。
 *
 * 本页把这些"存在但无法配置"的开关全部暴露出来，属 2026 改版新增功能之一。
 */
@kotlinx.serialization.Serializable
object PlayerOptionsScreen

@Composable
fun PlayerOptionsScreen(
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    fun saveBlock(block: AppConfigurationKt.Dsl.() -> Unit) {
        scope.launch {
            SettingsManager.updateConfiguration { copy(block) }
        }
    }

    TitleBackground(
        navController = navController,
        title = "播放选项",
        onBack = navController::navigateUp,
        onRetry = {}
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = titleBackgroundHorizontalPadding(),
                end = titleBackgroundHorizontalPadding(),
                bottom = if (isRound()) 32.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---------- 默认播放器 ----------
            item {
                SectionTitle("默认播放器")
                SegmentSelector(
                    options = listOf(Player.VideoPlayer to "视频", Player.AudioPlayer to "音频"),
                    selected = configuration.defaultPlayer,
                    onSelect = { saveBlock { defaultPlayer = it } }
                )
                HintText("音频模式仅播放声音，适合手表外放听视频")
            }

            // ---------- 解码器 ----------
            item {
                SectionTitle("视频解码器")
                SegmentSelector(
                    options = listOf(
                        VideoDecoder.Hardware to "硬解",
                        VideoDecoder.Software to "软解"
                    ),
                    selected = configuration.videoDecoder,
                    onSelect = { saveBlock { videoDecoder = it } }
                )
                HintText("硬解更省电，但部分老设备兼容性差；解码异常时改用软解")
            }

            // ---------- 显示模式 ----------
            item {
                SectionTitle("渲染方式")
                SegmentSelector(
                    options = listOf(
                        VideoDisplaySurface.SurfaceView to "SurfaceView",
                        VideoDisplaySurface.TextureView to "TextureView"
                    ),
                    selected = configuration.videoDisplaySurface,
                    onSelect = { saveBlock { videoDisplaySurface = it } }
                )
                HintText("SurfaceView 性能更好；TextureView 支持动画变换")
            }

            // ---------- 低性能模式 ----------
            item {
                SectionTitle("性能")
                ToggleRow(
                    title = "视频低性能模式",
                    subtitle = "降低弹幕密度与渲染精度，改善卡顿",
                    checked = configuration.isVideoLowPerformance,
                    onCheckedChange = { saveBlock { isVideoLowPerformance = it } }
                )
            }

            // ---------- 弹幕设置 ----------
            item {
                SectionTitle("弹幕")
            }
            item {
                ToggleRow(
                    title = "普通弹幕",
                    subtitle = "显示滚动弹幕",
                    checked = configuration.danmakuPlayerSettings.isNormalDanmakuEnabled,
                    onCheckedChange = {
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { isNormalDanmakuEnabled = it }
                        }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "高级弹幕",
                    subtitle = "显示底部/顶部定位弹幕",
                    checked = configuration.danmakuPlayerSettings.isAdvanceDanmakuEnabled,
                    onCheckedChange = {
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { isAdvanceDanmakuEnabled = it }
                        }
                    }
                )
            }
            item {
                FloatSliderRow(
                    title = "不透明度",
                    value = configuration.danmakuPlayerSettings.alpha,
                    range = 0.1f..1f,
                    valueText = "${(configuration.danmakuPlayerSettings.alpha * 100).toInt()}%",
                    onValueChangeFinished = { v ->
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { alpha = v }
                        }
                    }
                )
            }
            item {
                FloatSliderRow(
                    title = "显示区域",
                    value = configuration.danmakuPlayerSettings.displayArea,
                    range = 0.1f..1f,
                    valueText = "${(configuration.danmakuPlayerSettings.displayArea * 100).toInt()}%",
                    onValueChangeFinished = { v ->
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { displayArea = v }
                        }
                    }
                )
            }
            item {
                FloatSliderRow(
                    title = "字体缩放",
                    value = configuration.danmakuPlayerSettings.fontScale,
                    range = 0.5f..2f,
                    valueText = String.format("%.1fx", configuration.danmakuPlayerSettings.fontScale),
                    onValueChangeFinished = { v ->
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { fontScale = v }
                        }
                    }
                )
            }
            item {
                FloatSliderRow(
                    title = "屏蔽等级",
                    value = configuration.danmakuPlayerSettings.blockLevel.toFloat(),
                    range = 0f..10f,
                    valueText = configuration.danmakuPlayerSettings.blockLevel.toString(),
                    steps = 9,
                    onValueChangeFinished = { v ->
                        saveBlock {
                            danmakuPlayerSettings = danmakuPlayerSettings.copy { blockLevel = v.toInt() }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontFamily = wearbiliFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        textAlign = if (isRound()) TextAlign.Center else TextAlign.Start
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = wearbiliFontFamily,
        fontSize = 10.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .alpha(0.8f),
        textAlign = if (isRound()) TextAlign.Center else TextAlign.Start
    )
}

@Composable
private fun <T> SegmentSelector(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { (value, label) ->
                    val isSelected = value == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onSelect(value) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = wearbiliFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                repeat(2 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = wearbiliFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = wearbiliFontFamily,
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .alpha(0.8f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 带实时数值显示的滑块行。
 *
 * 拖动过程只更新本地状态（避免高频写 DataStore），
 * 松手后才落盘 —— 手表存储 IO 较慢，这点对流畅度影响明显。
 */
@Composable
private fun FloatSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    steps: Int = 0,
    onValueChangeFinished: (Float) -> Unit
) {
    var localValue by remember(value) { mutableFloatStateOf(value) }
    var displayText by remember(value) { mutableStateOf(valueText) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = wearbiliFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = displayText,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = wearbiliFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = localValue,
            onValueChange = { localValue = it },
            onValueChangeFinished = { onValueChangeFinished(localValue) },
            valueRange = range,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}
