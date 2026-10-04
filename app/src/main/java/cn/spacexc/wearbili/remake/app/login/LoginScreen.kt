package cn.spacexc.wearbili.remake.app.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cn.spacexc.wearbili.remake.app.splash.ui.SplashScreen
import cn.spacexc.wearbili.remake.common.ui.TitleBackground
import cn.spacexc.wearbili.remake.common.ui.glass.GlassLevel
import cn.spacexc.wearbili.remake.common.ui.glass.wearBiliGlass
import cn.spacexc.wearbili.remake.common.ui.icon.WearBiliIcons
import cn.spacexc.wearbili.remake.common.ui.icon.Refresh
import cn.spacexc.wearbili.remake.common.ui.rememberMutableInteractionSource

/**
 * 多方式登录页。
 *
 * 提供四条路径（对齐 PiliPlus）：
 *  - 扫码：TV 端二维码，手机扫码确认
 *  - Cookie：从手机/电脑复制 Cookie 串粘贴（手表端最实用）
 *  - 密码：App 端账号密码登录（RSA 加密）
 *  - 短信：手机号 + 验证码登录
 *
 * 手表屏幕小，方法切换用顶部横向 Tab；表单控件全部加高到 44dp 以上保证可点。
 */
@kotlinx.serialization.Serializable
object LoginScreen

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state

    LaunchedEffect(state.uiState) {
        if (state.uiState == LoginUiState.Success) {
            navController.navigate(SplashScreen) { popUpTo(0) }
        }
    }

    TitleBackground(
        navController = navController,
        title = "登录",
        onRetry = { },
        onBack = navController::navigateUp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoginMethodTabs(
                selected = state.method,
                onSelect = viewModel::selectMethod
            )

            when (state.method) {
                LoginMethod.QrCode -> QrCodeSection(state, viewModel::startQrCodeLogin)
                LoginMethod.Cookie -> CookieSection(state, viewModel::loginByCookie)
                LoginMethod.Password -> PasswordSection(state, viewModel::loginByPassword)
                LoginMethod.Sms -> SmsSection(state, viewModel)
            }

            state.message?.let { hint ->
                Text(
                    text = hint,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = if (state.uiState == LoginUiState.Failed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wearBiliGlass(level = GlassLevel.UltraThin, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/** 顶部方式切换 Tab */
@Composable
private fun LoginMethodTabs(
    selected: LoginMethod,
    onSelect: (LoginMethod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .wearBiliGlass(level = GlassLevel.Thin, shape = RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        LoginMethod.entries.forEach { method ->
            val isSelected = method == selected
            val alpha by animateFloatAsState(if (isSelected) 1f else 0.55f, label = "tabAlpha")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        else Color.Transparent
                    )
                    // 手表上每个 Tab 至少 34dp 高，整块可点
                    .clickable(
                        interactionSource = rememberMutableInteractionSource(),
                        indication = null
                    ) { onSelect(method) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = method.title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
            }
        }
    }
}

@Composable
private fun QrCodeSection(
    state: LoginPageState,
    onRefresh: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (state.uiState == LoginUiState.Idle) onRefresh()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state.uiState) {
            LoginUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LoginUiState.Failed, LoginUiState.Timeout -> Icon(
                imageVector = WearBiliIcons.Refresh,
                contentDescription = "刷新",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
            )

            else -> state.qrCodeBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "登录二维码",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    if (state.uiState == LoginUiState.AwaitingScan ||
        state.uiState == LoginUiState.ScannedConfirm
    ) {
        Text(
            text = "剩余 ${state.qrCodeExpireSeconds}s",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    TextButton(onClick = onRefresh) {
        Icon(WearBiliIcons.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(4.dp))
        Text("刷新二维码", fontSize = 11.sp)
    }
}

@Composable
private fun CookieSection(
    state: LoginPageState,
    onLogin: (String) -> Unit
) {
    var cookie by rememberSaveable { mutableStateOf("") }
    Text(
        text = "从手机或电脑浏览器复制 Cookie 串，需包含 SESSDATA 字段",
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = cookie,
        onValueChange = { cookie = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        textStyle = MaterialTheme.typography.bodySmall,
        placeholder = {
            Text("SESSDATA=xxx; bili_jct=yyy; DedeUserID=123", fontSize = 10.sp)
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
    LoginPrimaryButton(
        text = "登录",
        enabled = cookie.isNotBlank() && state.uiState != LoginUiState.Loading,
        loading = state.uiState == LoginUiState.Loading,
        onClick = { onLogin(cookie) }
    )
}

@Composable
private fun PasswordSection(
    state: LoginPageState,
    onLogin: (String, String) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    LoginTextField(
        value = username,
        onValueChange = { username = it },
        placeholder = "手机号 / 邮箱 / 用户名"
    )
    LoginTextField(
        value = password,
        onValueChange = { password = it },
        placeholder = "密码",
        isPassword = true
    )
    LoginPrimaryButton(
        text = "登录",
        enabled = username.isNotBlank() && password.isNotBlank() &&
                state.uiState != LoginUiState.Loading,
        loading = state.uiState == LoginUiState.Loading,
        onClick = { onLogin(username, password) }
    )
    Text(
        text = "如提示环境存在风险，请改用扫码或 Cookie 登录",
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SmsSection(
    state: LoginPageState,
    viewModel: LoginViewModel
) {
    var tel by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    LoginTextField(
        value = tel,
        onValueChange = { tel = it },
        placeholder = "手机号"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LoginTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = "验证码"
            )
        }
        TextButton(
            onClick = { viewModel.sendSmsCode(tel) },
            enabled = state.smsCooldownSeconds == 0 &&
                    state.uiState != LoginUiState.SendingCode
        ) {
            Text(
                text = if (state.smsCooldownSeconds > 0) "${state.smsCooldownSeconds}s"
                else if (state.uiState == LoginUiState.SendingCode) "发送中" else "获取",
                fontSize = 11.sp
            )
        }
    }
    LoginPrimaryButton(
        text = "登录",
        enabled = tel.isNotBlank() && code.isNotBlank() &&
                state.uiState != LoginUiState.Loading,
        loading = state.uiState == LoginUiState.Loading,
        onClick = { viewModel.loginBySmsCode(tel, code) }
    )
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else androidx.compose.ui.text.input.VisualTransformation.None,
        placeholder = { Text(placeholder, fontSize = 11.sp) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun LoginPrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(42.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            if (loading) Spacer(Modifier.size(6.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
