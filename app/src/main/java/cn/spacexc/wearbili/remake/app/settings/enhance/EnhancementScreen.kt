package cn.spacexc.wearbili.remake.app.settings.enhance

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
import androidx.compose.runtime.mutableIntStateOf
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
import cn.spacexc.wearbili.remake.proto.settings.copy
import kotlinx.coroutines.launch

/**
 * 增强功能设置页（2026 改版新增）。
 *
 * 汇总播放增强与浏览体验两组开关。这些配置都有真实消费点：
 * - playback.* 在播放器 / 音频服务中被读取
 * - browsing.* 在推荐 / 搜索 / 动态列表渲染中被读取
 */
@kotlinx.serialization.Serializable
object EnhancementScreen

@Composable
fun EnhancementScreen(
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
        title = "增强功能",
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
            // ================= 播放增强 =================
            item { SectionTitle("播放增强") }

            item {
                ToggleRow(
                    title = "记忆播放进度",
                    subtitle = "退出后下次从上次位置继续播放",
                    checked = configuration.playback.rememberProgress,
                    onCheckedChange = { v ->
                        saveBlock { playback = playback.copy { rememberProgress = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "自动连播",
                    subtitle = "播放结束后自动播放分P或推荐视频",
                    checked = configuration.playback.autoPlayNext,
                    onCheckedChange = { v ->
                        saveBlock { playback = playback.copy { autoPlayNext = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "后台播放音频",
                    subtitle = "离开应用后继续播放声音",
                    checked = configuration.playback.backgroundAudio,
                    onCheckedChange = { v ->
                        saveBlock { playback = playback.copy { backgroundAudio = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "自动跳过片头片尾",
                    subtitle = "按下方设定秒数自动跳过",
                    checked = configuration.playback.skipOpening,
                    onCheckedChange = { v ->
                        saveBlock { playback = playback.copy { skipOpening = v } }
                    }
                )
            }
            item {
                IntStepperRow(
                    title = "片头跳过",
                    value = configuration.playback.skipOpeningSeconds,
                    range = 0..180,
                    step = 5,
                    unit = "秒",
                    enabled = configuration.playback.skipOpening,
                    onChange = { v -> saveBlock { playback = playback.copy { skipOpeningSeconds = v } } }
                )
            }
            item {
                IntStepperRow(
                    title = "片尾跳过",
                    value = configuration.playback.skipEndingSeconds,
                    range = 0..180,
                    step = 5,
                    unit = "秒",
                    enabled = configuration.playback.skipOpening,
                    onChange = { v -> saveBlock { playback = playback.copy { skipEndingSeconds = v } } }
                )
            }
            item {
                FloatSliderRow(
                    title = "默认倍速",
                    value = configuration.playback.playbackSpeed.takeIf { it > 0f } ?: 1f,
                    range = 0.5f..3f,
                    valueText = String.format("%.2fx", configuration.playback.playbackSpeed.takeIf { it > 0f } ?: 1f),
                    steps = 9,
                    onValueChangeFinished = { v ->
                        saveBlock { playback = playback.copy { playbackSpeed = v } }
                    }
                )
            }

            // ================= 浏览体验 =================
            item { SectionTitle("浏览体验") }

            item {
                ToggleRow(
                    title = "过滤推广内容",
                    subtitle = "隐藏推荐流中的广告卡片",
                    checked = configuration.browsing.hideAds,
                    onCheckedChange = { v ->
                        saveBlock { browsing = browsing.copy { hideAds = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "显示视频数据",
                    subtitle = "卡片上展示播放量与弹幕数",
                    checked = configuration.browsing.showVideoStats,
                    onCheckedChange = { v ->
                        saveBlock { browsing = browsing.copy { showVideoStats = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "长按快捷菜单",
                    subtitle = "长按卡片直接弹出操作菜单",
                    checked = configuration.browsing.longPressMenu,
                    onCheckedChange = { v ->
                        saveBlock { browsing = browsing.copy { longPressMenu = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "紧凑布局",
                    subtitle = "缩小卡片间距，一屏看到更多内容",
                    checked = configuration.browsing.compactMode,
                    onCheckedChange = { v ->
                        saveBlock { browsing = browsing.copy { compactMode = v } }
                    }
                )
            }
            item {
                ToggleRow(
                    title = "允许自动旋转",
                    subtitle = "横屏时自动旋转界面",
                    checked = configuration.browsing.autoRotate,
                    onCheckedChange = { v ->
                        saveBlock { browsing = browsing.copy { autoRotate = v } }
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
 * 整数步进行，用于秒数这类离散值。
 *
 * 手表上拖滑块选精确秒数很难操作，改用 +/- 按钮更可靠。
 */
@Composable
private fun IntStepperRow(
    title: String,
    value: Int,
    range: IntRange,
    step: Int,
    unit: String,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    var local by remember(value) { mutableIntStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.5f else 0.25f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = wearbiliFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .alpha(if (enabled) 1f else 0.5f)
        )
        StepButton(
            label = "−",
            enabled = enabled && local > range.first,
            onClick = {
                local = (local - step).coerceAtLeast(range.first)
                onChange(local)
            }
        )
        Text(
            text = "$local$unit",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = wearbiliFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(56.dp)
                .alpha(if (enabled) 1f else 0.5f)
        )
        StepButton(
            label = "+",
            enabled = enabled && local < range.last,
            onClick = {
                local = (local + step).coerceAtMost(range.last)
                onChange(local)
            }
        )
    }
}

@Composable
private fun StepButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

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
            onValueChange = {
                localValue = it
                displayText = String.format("%.2fx", it)
            },
            onValueChangeFinished = { onValueChangeFinished(localValue) },
            valueRange = range,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}
