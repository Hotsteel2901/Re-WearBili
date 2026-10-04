package cn.spacexc.wearbili.remake.app.settings.personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cn.spacexc.wearbili.remake.R
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.app.settings.ui.SettingsItemV2
import cn.spacexc.wearbili.remake.common.ui.TitleBackground
import cn.spacexc.wearbili.remake.common.ui.isRound
import cn.spacexc.wearbili.remake.common.ui.theme.wearbiliFontFamily
import cn.spacexc.wearbili.remake.common.ui.titleBackgroundHorizontalPadding
import cn.spacexc.wearbili.remake.common.ui.wearBiliAnimateColorAsState
import cn.spacexc.wearbili.remake.common.ui.wearBiliAnimateFloatAsState
import cn.spacexc.wearbili.remake.proto.settings.AnimationLevel
import cn.spacexc.wearbili.remake.proto.settings.Appearance
import cn.spacexc.wearbili.remake.proto.settings.Customization
import cn.spacexc.wearbili.remake.proto.settings.CustomizationKt
import cn.spacexc.wearbili.remake.proto.settings.copy
import kotlinx.coroutines.launch

/**
 * 个性化设置页。
 *
 * 2026 改版新增：将主题相关能力（明暗模式 / Monet / 液态玻璃 / 动画档位 / 主色）
 * 从"只存在于 proto、无 UI 可改"变为真正可操作的界面。
 *
 * 所有改动即时写入 DataStore，并通过 LocalConfiguration 触发全局重组。
 */
@kotlinx.serialization.Serializable
object PersonalizationScreen

/** 预设装扮：一套默认粉色 + 若干可选主色 */
private data class PresetTheme(
    val name: String,
    val colorHex: String,
    val color: Color,
)

private val presetThemes = listOf(
    PresetTheme("B站粉（默认）", "", Color(0xFFFE679A)),
    PresetTheme("樱花粉", "#FFB7C5", Color(0xFFFFB7C5)),
    PresetTheme("罗兰紫", "#9C6ADE", Color(0xFF9C6ADE)),
    PresetTheme("天空蓝", "#4A9EFF", Color(0xFF4A9EFF)),
    PresetTheme("薄荷绿", "#3ECF8E", Color(0xFF3ECF8E)),
    PresetTheme("落日橙", "#FF8A3D", Color(0xFFFF8A3D)),
    PresetTheme("玫瑰红", "#E5484D", Color(0xFFE5484D)),
    PresetTheme("石墨灰", "#8B8D98", Color(0xFF8B8D98)),
)

@Composable
fun PersonalizationScreen(
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val customization = configuration.customization
    val scope = rememberCoroutineScope()

    // 本地即时状态，避免每次点击都等待 DataStore 回写才更新 UI
    var appearance by remember(customization.appearance) { mutableStateOf(customization.appearance) }
    var monetEnabled by remember(customization.monetEnabled) { mutableStateOf(customization.monetEnabled) }
    var glassEnabled by remember(customization.glassEnabled) { mutableStateOf(customization.glassEnabled) }
    var animationLevel by remember(customization.animationLevel) { mutableStateOf(customization.animationLevel) }
    var themeColorHex by remember(customization.themeColorHex) { mutableStateOf(customization.themeColorHex) }

    /**
     * 写入单条个性化配置。
     *
     * 注意：protobuf 生成的 `copy` 是 builder DSL 扩展函数
     * （`Customization.copy { this.appearance = ... }`），
     * 不是 data class 的 `copy(named = ...)`，因此这里传入 Dsl 风格的 lambda。
     */
    fun save(block: CustomizationKt.Dsl.() -> Unit) {
        scope.launch {
            SettingsManager.updateConfiguration {
                val updated = customization.copy(block)
                copy { this.customization = updated }
            }
        }
    }

    TitleBackground(
        navController = navController,
        title = "个性化",
        onBack = navController::navigateUp,
        onRetry = {}
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = titleBackgroundHorizontalPadding(),
                end = titleBackgroundHorizontalPadding(),
                bottom = if (isRound()) 32.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---------- 明暗模式 ----------
            item {
                SectionTitle("明暗模式")
                SegmentSelector(
                    options = listOf(
                        Appearance.FollowSystem to "跟随系统",
                        Appearance.AlwaysDark to "深色",
                        Appearance.AlwaysLight to "浅色",
                        Appearance.PureBlack to "纯黑"
                    ),
                    selected = appearance,
                    onSelect = {
                        appearance = it
                        save { this.appearance = it }
                    }
                )
                if (appearance == Appearance.PureBlack) {
                    HintText("纯黑模式使用纯黑背景，OLED 屏幕更省电")
                }
            }

            // ---------- 动态取色 ----------
            item {
                SectionTitle("配色")
                ToggleRow(
                    title = "莫奈动态取色",
                    subtitle = "从壁纸提取配色（需 Android 12 及以上）",
                    checked = monetEnabled,
                    enabled = appearance != Appearance.PureBlack,
                    onCheckedChange = {
                        monetEnabled = it
                        save { this.monetEnabled = it }
                    }
                )
            }

            // ---------- 主色装扮（Monet 关闭时才有意义）----------
            if (!monetEnabled && appearance != Appearance.PureBlack) {
                item {
                    Text(
                        text = "选择装扮",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = wearbiliFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
                item {
                    PresetThemeGrid(
                        selectedHex = themeColorHex,
                        onSelect = { hex ->
                            themeColorHex = hex
                            save { this.themeColorHex = hex }
                        }
                    )
                }
            }

            // ---------- 液态玻璃 ----------
            item {
                SectionTitle("外观效果")
                ToggleRow(
                    title = "液态玻璃",
                    subtitle = "标题栏与卡片使用模糊玻璃质感，低配手表建议关闭",
                    checked = glassEnabled,
                    enabled = true,
                    onCheckedChange = {
                        glassEnabled = it
                        save { this.glassEnabled = it }
                    }
                )
            }

            // ---------- 动画档位 ----------
            item {
                SectionTitle("动画")
                SegmentSelector(
                    options = listOf(
                        AnimationLevel.Off to "关闭",
                        AnimationLevel.Minimal to "精简",
                        AnimationLevel.Standard to "标准",
                        AnimationLevel.Full to "拉满"
                    ),
                    selected = animationLevel,
                    onSelect = {
                        animationLevel = it
                        save { this.animationLevel = it }
                    }
                )
                HintText(
                    when (animationLevel) {
                        AnimationLevel.Off -> "无动画，最省电，适合极低配设备"
                        AnimationLevel.Minimal -> "仅保留必要过渡"
                        AnimationLevel.Standard -> "标准动效（推荐）"
                        AnimationLevel.Full -> "Expressive 弹簧动效全开，最流畅"
                        else -> ""
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

/**
 * 分段选择器。
 *
 * 手表屏幕窄，横向排列超过 3 项会溢出，因此按数量自适应：
 * <= 2 项一行，> 2 项按两行平铺。
 */
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
                    val containerColor by wearBiliAnimateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    val contentColor by wearBiliAnimateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val scale by wearBiliAnimateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.97f
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(containerColor)
                            .clickable { onSelect(value) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = contentColor,
                            fontFamily = wearbiliFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(scale)
                        )
                    }
                }
                // 补齐空位，保证最后一行宽度一致
                repeat(2 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
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
                    .alpha(if (enabled) 0.8f else 0.4f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            enabled = enabled,
            modifier = Modifier
                .size(width = 44.dp, height = 26.dp)
                .alpha(if (enabled) 1f else 0.4f)
        )
    }
}

/** 装扮选择：色块网格，选中项带主题色描边 */
@Composable
private fun PresetThemeGrid(
    selectedHex: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presetThemes.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { preset ->
                    val isSelected = preset.colorHex == selectedHex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(preset.colorHex) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(preset.color, preset.color.copy(alpha = 0.6f))
                                    )
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = preset.name,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = wearbiliFontFamily,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
