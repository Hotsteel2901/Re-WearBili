package cn.spacexc.wearbili.remake.app.login

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.spacexc.bilibilisdk.BilibiliSdkManager
import cn.spacexc.wearbili.common.domain.qrcode.QRCodeUtil
import cn.spacexc.wearbili.remake.app.login.domain.LoginApi
import cn.spacexc.wearbili.remake.app.login.domain.LoginCookie
import cn.spacexc.wearbili.remake.app.login.domain.LoginResultData
import cn.spacexc.wearbili.remake.common.networking.CookiesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** 登录方式 */
enum class LoginMethod(val title: String) {
    QrCode("扫码"),
    Cookie("Cookie"),
    Password("密码"),
    Sms("短信")
}

/** 登录流程状态 */
enum class LoginUiState {
    Idle,
    Loading,
    AwaitingScan,
    ScannedConfirm,
    SendingCode,
    CodeSent,
    Success,
    Failed,
    Timeout
}

/** 页面可变状态 */
data class LoginPageState(
    val method: LoginMethod = LoginMethod.QrCode,
    val uiState: LoginUiState = LoginUiState.Idle,
    val message: String? = null,
    val qrCodeBitmap: Bitmap? = null,
    val qrCodeExpireSeconds: Int = 0,
    val smsCooldownSeconds: Int = 0
)

/**
 * 统一登录 ViewModel。
 *
 * 支持四路登录（参考 PiliPlus），核心统一在 [persistLoginResult]：
 * 无论哪条路径，最终都落到「写入 Cookie 到本地库 + 记录 uid」这一个出口，
 * 避免多套落盘逻辑各自为政。
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginApi: LoginApi,
    private val qrCodeUtil: QRCodeUtil,
    private val cookiesManager: CookiesManager
) : ViewModel() {

    var state by mutableStateOf(LoginPageState())
        private set

    private var pollJob: Job? = null
    private var qrCodeExpireJob: Job? = null
    private var smsCooldownJob: Job? = null

    /** 短信登录时暂存的 captcha_key（来自发送验证码的响应） */
    private var smsCaptchaKey: String = ""

    /**
     * TV 端短信登录会话标识。
     * 发送验证码与提交验证码两步必须使用同一值，首次发码时生成。
     */
    private var smsLoginSessionId: String? = null

    /** 设备指纹，首次生成后持久化，保证 App 端接口的 device_id 稳定 */
    private var cachedDeviceId: String? = null

    private suspend fun ensureDeviceId(): String {
        cachedDeviceId?.let { return it }
        val manager = BilibiliSdkManager.dataManager
        val existing = manager.getString("login_device_id", null)
        if (existing != null) {
            cachedDeviceId = existing
            return existing
        }
        val generated = UUID.randomUUID().toString().replace("-", "").uppercase()
        manager.saveString("login_device_id", generated)
        cachedDeviceId = generated
        return generated
    }

    private suspend fun ensureBuvid(): String {
        val manager = BilibiliSdkManager.dataManager
        val existing = manager.getString("buvid", null)
        if (!existing.isNullOrBlank()) return existing
        // 兜底：生成一个符合格式的伪 buvid，避免接口因缺参直接失败
        val generated = "XY" + UUID.randomUUID().toString().replace("-", "").uppercase()
        manager.saveString("buvid", generated)
        return generated
    }

    fun selectMethod(method: LoginMethod) {
        if (state.method == method) return
        stopAllJobs()
        state = LoginPageState(method = method)
    }

    fun updateMessage(message: String?) {
        state = state.copy(message = message)
    }

    // ---------------------------------------------------------------- 扫码登录

    /** 启动 TV 端扫码登录 */
    fun startQrCodeLogin() {
        stopAllJobs()
        state = state.copy(uiState = LoginUiState.Loading, message = null)
        pollJob = viewModelScope.launch {
            val code = loginApi.requestTvQrCode()
            if (code == null) {
                state = state.copy(uiState = LoginUiState.Failed, message = "二维码获取失败，点击重试")
                return@launch
            }
            val (authCode, url) = code
            val bitmap = qrCodeUtil.createQRCodeBitmap(
                url, 512, 512,
                cn.spacexc.wearbili.common.domain.qrcode.ERROR_CORRECTION_M
            )
            state = state.copy(
                uiState = LoginUiState.AwaitingScan,
                qrCodeBitmap = bitmap,
                qrCodeExpireSeconds = 180,
                message = "请使用哔哩哔哩客户端扫码"
            )
            startQrCodeExpireCountdown()
            pollQrCode(authCode)
        }
    }

    private fun startQrCodeExpireCountdown() {
        qrCodeExpireJob?.cancel()
        qrCodeExpireJob = viewModelScope.launch {
            var left = 180
            while (left > 0) {
                delay(1000)
                left--
                state = state.copy(qrCodeExpireSeconds = left)
                if (left <= 0) {
                    state = state.copy(
                        uiState = LoginUiState.Timeout,
                        message = "二维码已过期，点击刷新"
                    )
                    stopAllJobs()
                }
            }
        }
    }

    private suspend fun pollQrCode(authCode: String) {
        while (true) {
            val result = loginApi.pollTvQrCode(authCode)
            if (result == null) {
                delay(1500)
                continue
            }
            // BUG FIX: TV 端 poll 的状态码在外层 envelope——curl 实测未扫码时
            // 直接返回 {"code":86039,"message":"二维码尚未确认","data":null}。
            // 之前误把外层 code != 0 当作失败，导致拿到二维码 1 秒内就报
            // "二维码尚未确认"并退出轮询。现取外层 code（data 为 null 时），
            // 同时兼容 Web 风格（外层 0 + data.code 表状态）。
            val statusCode = result.data?.code?.takeIf { it != 0L } ?: result.code.toLong()
            when (statusCode) {
                0L -> {
                    // 扫码成功，已确认
                    val data = result.data
                    val cookies = data?.cookieInfo?.cookies.orEmpty()
                    if (cookies.isNotEmpty()) {
                        persistLoginCookies(cookies, data?.refreshToken)
                    } else {
                        // 没有 cookie_info 时回退：用返回的 url 里的参数构造
                        val fallback = parseCookiesFromUrl(data?.url)
                        if (fallback.isNotEmpty()) {
                            persistLoginCookies(fallback, data?.refreshToken)
                        } else {
                            state = state.copy(
                                uiState = LoginUiState.Failed,
                                message = "登录响应缺少凭证"
                            )
                        }
                    }
                    return
                }

                86038L -> {
                    state = state.copy(
                        uiState = LoginUiState.Timeout,
                        message = "二维码已过期，点击刷新"
                    )
                    return
                }

                86090L -> {
                    state = state.copy(
                        uiState = LoginUiState.ScannedConfirm,
                        message = "已扫码，请在手机上确认"
                    )
                }

                86039L, 86101L -> {
                    state = state.copy(
                        uiState = LoginUiState.AwaitingScan,
                        message = "请使用哔哩哔哩客户端扫码"
                    )
                }
                // 其他状态码保持等待，180s 倒计时兜底
                else -> Unit
            }
            delay(1500)
        }
    }

    /** 从 TV 登录返回的 url 里解析 cookie（部分版本不带 cookie_info） */
    private fun parseCookiesFromUrl(url: String?): List<LoginCookie> {
        if (url.isNullOrBlank()) return emptyList()
        val query = url.substringAfter('?', "")
        if (query.isEmpty()) return emptyList()
        return query.split('&').mapNotNull { pair ->
            val idx = pair.indexOf('=')
            if (idx <= 0) return@mapNotNull null
            val name = pair.substring(0, idx)
            val value = pair.substring(idx + 1)
            if (name.isBlank() || value.isBlank()) return@mapNotNull null
            LoginCookie(name = name, value = value)
        }
    }

    // --------------------------------------------------------------- Cookie 登录

    /**
     * Cookie 登录。
     *
     * 手表上没有 WebView，这是最实用的一条路径：用户从手机/电脑复制
     * SESSDATA 等 Cookie 串粘贴进来即可。
     */
    fun loginByCookie(rawCookie: String) {
        if (rawCookie.isBlank()) {
            state = state.copy(uiState = LoginUiState.Failed, message = "Cookie 不能为空")
            return
        }
        stopAllJobs()
        state = state.copy(uiState = LoginUiState.Loading, message = "正在校验 Cookie…")
        viewModelScope.launch {
            val imported = cookiesManager.importCookies(rawCookie)
            if (imported == 0) {
                state = state.copy(
                    uiState = LoginUiState.Failed,
                    message = "Cookie 格式不正确，需形如 SESSDATA=xxx; bili_jct=yyy"
                )
                return@launch
            }
            // 校验凭证是否真的可用
            val isLoggedIn = cn.spacexc.bilibilisdk.utils.UserUtils.isUserLoggedIn()
            if (isLoggedIn) {
                state = state.copy(uiState = LoginUiState.Success, message = "登录成功")
            } else {
                state = state.copy(
                    uiState = LoginUiState.Failed,
                    message = "Cookie 无效或已过期，请重新获取"
                )
            }
        }
    }

    // --------------------------------------------------------------- 密码登录

    fun loginByPassword(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            state = state.copy(uiState = LoginUiState.Failed, message = "账号和密码不能为空")
            return
        }
        stopAllJobs()
        state = state.copy(uiState = LoginUiState.Loading, message = "正在登录…")
        viewModelScope.launch {
            val webKey = loginApi.getWebKey()
            val rsaKey = webKey?.data?.key
            val salt = webKey?.data?.hash
            if (rsaKey.isNullOrBlank() || salt.isNullOrBlank()) {
                state = state.copy(
                    uiState = LoginUiState.Failed,
                    message = "获取加密公钥失败，请稍后重试"
                )
                return@launch
            }
            val result = loginApi.loginByPassword(
                username = username,
                password = password,
                rsaKey = rsaKey,
                salt = salt,
                deviceId = ensureDeviceId(),
                buvid = ensureBuvid()
            )
            handleLoginEnvelope(result?.code, result?.message, result?.data)
        }
    }

    // --------------------------------------------------------------- 短信登录

    fun sendSmsCode(tel: String) {
        if (tel.isBlank() || tel.length < 11) {
            state = state.copy(uiState = LoginUiState.Failed, message = "请输入正确的手机号")
            return
        }
        if (state.smsCooldownSeconds > 0) return
        state = state.copy(uiState = LoginUiState.SendingCode, message = "正在发送验证码…")
        viewModelScope.launch {
            val sessionId = smsLoginSessionId
                ?: UUID.randomUUID().toString().replace("-", "").uppercase()
                    .also { smsLoginSessionId = it }
            val result = loginApi.sendSmsCode(
                tel = tel,
                buvid = ensureBuvid(),
                loginSessionId = sessionId
            )
            if (result == null || result.code != 0) {
                val hint = when (result?.code) {
                    86005, 1002 -> "手机号格式不正确"
                    86203 -> "短信发送次数已达上限，请稍后再试"
                    -412 -> "请求被风控拦截，请稍后再试"
                    else -> result?.message ?: "验证码发送失败，请稍后重试"
                }
                state = state.copy(
                    uiState = LoginUiState.Failed,
                    message = hint
                )
                return@launch
            }
            // TV 端发送成功响应应携带 data.captcha_key；若缺失则以会话标识兜底，
            // 登录失败（86205）时提示用户重新获取验证码
            smsCaptchaKey = result.data?.captchaKey.orEmpty().ifBlank { sessionId }
            state = state.copy(
                uiState = LoginUiState.CodeSent,
                message = "验证码已发送",
                smsCooldownSeconds = 60
            )
            startSmsCooldown()
        }
    }

    private fun startSmsCooldown() {
        smsCooldownJob?.cancel()
        smsCooldownJob = viewModelScope.launch {
            var left = 60
            while (left > 0) {
                delay(1000)
                left--
                state = state.copy(smsCooldownSeconds = left)
            }
        }
    }

    fun loginBySmsCode(tel: String, code: String) {
        if (tel.isBlank() || code.isBlank()) {
            state = state.copy(uiState = LoginUiState.Failed, message = "手机号和验证码不能为空")
            return
        }
        stopAllJobs()
        state = state.copy(uiState = LoginUiState.Loading, message = "正在登录…")
        viewModelScope.launch {
            val sessionId = smsLoginSessionId
                ?: UUID.randomUUID().toString().replace("-", "").uppercase()
                    .also { smsLoginSessionId = it }
            val result = loginApi.loginBySmsCode(
                tel = tel,
                code = code,
                captchaKey = smsCaptchaKey,
                buvid = ensureBuvid(),
                loginSessionId = sessionId
            )
            handleLoginEnvelope(result?.code, result?.message, result?.data)
        }
    }

    // ------------------------------------------------------------------ 公共出口

    /**
     * 统一处理登录响应。
     *
     * `status == 2` 表示触发风控需要手机号验证 —— 手表端无法走完整
     * safeCenter 流程，这里给出明确提示并引导改用其他登录方式。
     */
    private suspend fun handleLoginEnvelope(
        code: Int?,
        message: String?,
        data: LoginResultData?
    ) {
        if (code == null) {
            // NetworkResponse.Failed（HTTP 非 200 / 网络异常）——请求根本没到达业务层
            state = state.copy(
                uiState = LoginUiState.Failed,
                message = "网络请求失败，请检查网络后重试"
            )
            return
        }
        if (code != 0) {
            val hint = when (code) {
                -629 -> "账号或密码错误"
                -105 -> "该账号已开启二次验证，请使用其他方式登录"
                -400 -> "请求参数错误"
                -403 -> "账号被封禁或限制登录"
                86205 -> "验证码错误或已失效，请重新获取"
                86005 -> "手机号格式不正确"
                86038 -> "二维码已过期，请刷新后重试"
                else -> message ?: "登录失败 ($code)"
            }
            state = state.copy(uiState = LoginUiState.Failed, message = hint)
            return
        }
        if (data == null) {
            state = state.copy(uiState = LoginUiState.Failed, message = "接口未返回数据")
            return
        }
        if (data.status == 2) {
            state = state.copy(
                uiState = LoginUiState.Failed,
                message = "本次登录环境存在风险，需使用手机号验证。请改用扫码或 Cookie 登录"
            )
            return
        }
        // cookie_info 为空时回退：从返回 url 的 query 参数构造（与扫码 poll 同构）。
        // TV 端为 OAuth2 token 体系：code=0 时也可能只回 token 不回 cookie，
        // 此时把关键字段存在性透传给 UI，方便真机定位"到底缺了什么"。
        val cookies = data.cookieInfo?.cookies.orEmpty().ifEmpty { parseCookiesFromUrl(data.url) }
        if (cookies.isEmpty()) {
            if (!data.refreshToken.isNullOrBlank()) {
                // 先把 token 存档，后续接入 token 体系时可用
                BilibiliSdkManager.dataManager.saveString("refreshToken", data.refreshToken)
            }
            val diag = "登录响应缺少凭证" +
                    "(token=${if (!data.accessToken.isNullOrBlank()) "有" else "无"}" +
                    ",refresh=${if (!data.refreshToken.isNullOrBlank()) "有" else "无"}" +
                    ",url=${if (!data.url.isNullOrBlank()) "有" else "无"})"
            state = state.copy(uiState = LoginUiState.Failed, message = diag)
            return
        }
        persistLoginCookies(cookies, data.refreshToken)
    }

    /** 唯一的凭证落盘出口：写 Cookie + 记录 uid + 刷新 token */
    private suspend fun persistLoginCookies(
        cookies: List<LoginCookie>,
        refreshToken: String?
    ) {
        val raw = cookies.joinToString("; ") { "${it.name}=${it.value}" }
        val imported = cookiesManager.importCookies(raw)
        val uid = cookies.firstOrNull { it.name == "DedeUserID" }?.value?.toLongOrNull()
        if (uid != null) {
            cn.spacexc.bilibilisdk.utils.UserUtils.addUser(uid)
            cn.spacexc.bilibilisdk.utils.UserUtils.setCurrentUid(uid)
        }
        if (!refreshToken.isNullOrBlank()) {
            BilibiliSdkManager.dataManager.saveString("refreshToken", refreshToken)
        }
        if (imported > 0 && cn.spacexc.bilibilisdk.utils.UserUtils.isUserLoggedIn()) {
            state = state.copy(uiState = LoginUiState.Success, message = "登录成功")
        } else {
            state = state.copy(
                uiState = LoginUiState.Failed,
                message = "凭证写入失败，请重试"
            )
        }
    }

    private fun stopAllJobs() {
        pollJob?.cancel()
        qrCodeExpireJob?.cancel()
        smsCooldownJob?.cancel()
    }

    override fun onCleared() {
        stopAllJobs()
        super.onCleared()
    }
}
