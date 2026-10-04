package cn.spacexc.wearbili.remake.app.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import cn.spacexc.bilibilisdk.sdk.user.webi.WebiSignature
import cn.spacexc.bilibilisdk.utils.EncryptUtils
import cn.spacexc.bilibilisdk.utils.UserUtils
import cn.spacexc.wearbili.common.domain.data.DataStoreManager
import cn.spacexc.wearbili.common.isZeroOrNull
import cn.spacexc.wearbili.remake.app.login.LoginScreen
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.app.welcome.screens.DeviceLayoutChooserScreen
import cn.spacexc.wearbili.remake.app.main.ui.HomeScreen
import cn.spacexc.wearbili.remake.app.settings.user.SwitchUserScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 启动初始化。
 *
 * 2026 二改版变更：
 *  - 移除原项目遗留的「更新检查」——那是原作者反馈服务器的接口，二改版不依赖它，
 *    服务器不可达时还会在每次启动弹「更新检查失败」的 Toast。
 *  - 首次启动（未选择过设备布局）时，第一屏就是双端布局选择页（手表/手机），
 *    选择完成后再按登录状态分流；已选过布局的按原逻辑进主页或登录页。
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    fun initApp(
        navController: NavController
    ) {
        viewModelScope.launch {
            WebiSignature.getWebiSignature()    //保存新的webi签名
            if (dataStoreManager.getString("buvid").isNullOrEmpty()) {
                dataStoreManager.saveString("buvid", EncryptUtils.generateBuvid())
            }
            if (!SettingsManager.getConfiguration().customization.hasChosenDeviceLayout) {
                // 首次启动：第一屏就是设备布局选择（手表/手机），
                // 选择完成后由选择页按登录状态分流（登录页或主页）
                navController.navigate(DeviceLayoutChooserScreen) {
                    popUpTo(0)
                }
            } else if (UserUtils.isUserLoggedIn()) {
                navController.navigate(HomeScreen(null)) {
                    popUpTo(0)
                }
            } else if (UserUtils.mid().isZeroOrNull() && UserUtils.getUsers().isNotEmpty()) {
                navController.navigate(SwitchUserScreen)
            } else {
                navController.navigate(LoginScreen) {
                    popUpTo(0)
                }
            }
        }
    }
}
