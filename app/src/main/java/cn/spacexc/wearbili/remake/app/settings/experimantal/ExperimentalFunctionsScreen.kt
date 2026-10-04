package cn.spacexc.wearbili.remake.app.settings.experimantal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cn.spacexc.wearbili.remake.common.ui.Switch
import cn.spacexc.wearbili.remake.common.ui.TitleBackground
import cn.spacexc.wearbili.remake.common.ui.clickVfx
import cn.spacexc.wearbili.remake.common.ui.icon.Experimental
import cn.spacexc.wearbili.remake.common.ui.icon.WearBiliIcons
import cn.spacexc.wearbili.remake.common.ui.theme.LocalLayoutSpec
import cn.spacexc.wearbili.remake.common.ui.theme.wearbiliFontFamily
import cn.spacexc.wearbili.remake.common.ui.titleBackgroundHorizontalPadding
import cn.spacexc.wearbili.remake.proto.settings.AppConfiguration
import cn.spacexc.wearbili.remake.proto.settings.copy

const val EXPERIMENTAL_MULTI_ACCOUNTS = "experimental.multi.account"
const val EXPERIMENTAL_LARGE_VIDEO_CARD = "experimental.large.video.card"
const val EXPERIMENTAL_FLOATING_SUBTITLE = "experimental.global.floating.subtitle"
const val EXPERIMENTAL_FADE_SUBTITLE = "experimental.fade.subtitle.animation"

/**
 * 实验性功能页（2026 MD3E 改版）。
 *
 * 旧版问题：全部文字硬编码 Color.White（亮色主题下不可见）、
 * Card 圆角写 RoundedCornerShape(13) 漏了 dp 单位（13 像素）、
 * 空标题导致 TitleBackground 渲染异常。
 *
 * 新版：文字/图标/容器全走 MaterialTheme；卡片按 LocalLayoutSpec
 * 取圆角；开/关在容器色与描边上做可感知差异；功能 ID 与落盘逻辑不变。
 */
@Composable
fun ExperimentalFunctionsScreen(navController: NavController) {
    val layoutSpec = LocalLayoutSpec.current
    TitleBackground(
        navController = navController,
        title = "实验性功能",
        onRetry = { },
        onBack = navController::navigateUp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = titleBackgroundHorizontalPadding(),
                end = titleBackgroundHorizontalPadding(),
                bottom = if (layoutSpec.isWatch) 32.dp else 20.dp,
                top = 4.dp
            ),
            verticalArrangement = Arrangement.spacedBy(layoutSpec.listSpacing)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = WearBiliIcons.Experimental,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = "前沿体验，稳定性自负\n随时可能移除或调整",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        modifier = Modifier.alpha(0.85f),
                        fontFamily = wearbiliFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                ExperimentalFunctionCard(
                    name = "好多账号！",
                    description = "允许切换多个不同账户",
                    functionId = EXPERIMENTAL_MULTI_ACCOUNTS
                )
            }
            item {
                ExperimentalFunctionCard(
                    name = "是大卡片！",
                    description = "推荐页由列表更换为卡片",
                    functionId = EXPERIMENTAL_LARGE_VIDEO_CARD
                )
            }
            item {
                ExperimentalFunctionCard(
                    name = "浮动字幕",
                    description = "音频模式下开启全局浮动字幕",
                    functionId = EXPERIMENTAL_FLOATING_SUBTITLE
                )
            }
            item {
                ExperimentalFunctionCard(
                    name = "逐字字幕",
                    description = "音频模式下，字幕逐字淡入显示",
                    functionId = EXPERIMENTAL_FADE_SUBTITLE
                )
            }
        }
    }
}

@kotlinx.serialization.Serializable
object ExperimentalFunctionsScreen

/**
 * 实验功能开关卡片（MD3E）。
 *
 * 开 = primaryContainer 容器 + primary 描边（跟随 Monet）；
 * 关 = surfaceContainerLow 容器 + outlineVariant 描边。
 * 整卡可点切换（比只点开关命中区大，适配手表）。
 */
@Composable
fun ExperimentalFunctionCard(
    name: String,
    description: String,
    functionId: String
) {
    val layoutSpec = LocalLayoutSpec.current
    val configuration = LocalConfiguration.current
    var isOn by remember {
        mutableStateOf(configuration.getActivatedExperimentalFunctions().contains(functionId))
    }
    LaunchedEffect(key1 = isOn) {
        val tempList = configuration.getActivatedExperimentalFunctions()
            .filter { it.isNotBlank() }
            .toMutableList()
        if (isOn) {
            if (!configuration.getActivatedExperimentalFunctions().contains(functionId))
                tempList.add(functionId)
        } else {
            tempList.remove(functionId)
        }
        SettingsManager.updateConfiguration {
            copy {
                activatedExperimentFunctions = tempList.joinToString(",")
            }
        }
    }
    val containerColor = if (isOn) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = if (isOn) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val shape = RoundedCornerShape(layoutSpec.cardCorner)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickVfx { isOn = !isOn }
            .background(containerColor, shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .heightIn(min = layoutSpec.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = WearBiliIcons.Experimental,
            contentDescription = null,
            tint = if (isOn) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(22.dp)
                .padding(end = 0.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontFamily = wearbiliFontFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontFamily = wearbiliFontFamily,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(0.85f)
                    .padding(top = 2.dp)
            )
        }
        Switch(isOn = isOn) {
            isOn = it
        }
    }
}

fun AppConfiguration.getActivatedExperimentalFunctions(): List<String> =
    if (activatedExperimentFunctions.isBlank()) emptyList()
    else activatedExperimentFunctions.split(",")
