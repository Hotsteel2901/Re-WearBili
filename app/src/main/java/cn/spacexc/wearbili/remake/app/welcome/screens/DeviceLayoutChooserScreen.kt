package cn.spacexc.wearbili.remake.app.welcome.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cn.spacexc.wearbili.remake.R
import cn.spacexc.wearbili.remake.app.main.ui.HomeScreen
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.common.ui.icon.Phone
import cn.spacexc.wearbili.remake.common.ui.icon.Watch
import cn.spacexc.wearbili.remake.common.ui.icon.WearBiliIcons
import cn.spacexc.wearbili.remake.common.ui.theme.WatchLayoutSpec
import cn.spacexc.wearbili.remake.common.ui.clickVfx
import cn.spacexc.wearbili.remake.proto.settings.copy
import kotlinx.coroutines.launch

/**
 * 首次启动布局选择页。
 *
 * 首次安装（hasChosenDeviceLayout == false）时在 Splash 之后出现，
 * 让用户选择「手表使用」或「手机使用」，选择结果落盘后进入主页。
 * 后续可在 设置 -> 个性化 里随时切换。
 *
 * 注意：选择页本身按手表观感渲染（未选择前全局密度就是手表基准），
 * 两个选项卡片是大触控目标，手表上也能轻松点按。
 */
@kotlinx.serialization.Serializable
object DeviceLayoutChooserScreen

@Composable
fun DeviceLayoutChooserScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<cn.spacexc.wearbili.remake.proto.settings.DeviceLayout?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_app_main_reverse),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.28f)
                    .padding(bottom = 10.dp)
            )
            Text(
                text = "你在哪种设备上使用？",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "选择适合你的界面布局，之后可以在设置中修改",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LayoutOptionCard(
                    modifier = Modifier.weight(1f),
                    icon = WearBiliIcons.Watch,
                    title = "手表使用",
                    description = "为大圆屏优化\n大按钮 · 单列内容",
                    isSelected = selected == cn.spacexc.wearbili.remake.proto.settings.DeviceLayout.DeviceWatch
                ) {
                    selected = cn.spacexc.wearbili.remake.proto.settings.DeviceLayout.DeviceWatch
                }
                LayoutOptionCard(
                    modifier = Modifier.weight(1f),
                    icon = WearBiliIcons.Phone,
                    title = "手机使用",
                    description = "更高信息密度\n双列内容 · 标准按钮",
                    isSelected = selected == cn.spacexc.wearbili.remake.proto.settings.DeviceLayout.DevicePhone
                ) {
                    selected = cn.spacexc.wearbili.remake.proto.settings.DeviceLayout.DevicePhone
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            androidx.compose.material3.Button(
                onClick = {
                    val chosen = selected ?: return@Button
                    scope.launch {
                        SettingsManager.updateConfiguration {
                            val updated = customization.copy {
                                this.deviceLayout = chosen
                                this.hasChosenDeviceLayout = true
                            }
                            copy { this.customization = updated }
                        }
                        navController.navigate(HomeScreen(null)) {
                            popUpTo(0)
                        }
                    }
                },
                enabled = selected != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(44.dp)
            ) {
                Text("开始使用", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun LayoutOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(WatchLayoutSpec.cardCorner))
            .clickVfx { onClick() },
        shape = RoundedCornerShape(WatchLayoutSpec.cardCorner),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = description,
                fontSize = 9.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
