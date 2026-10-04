package cn.spacexc.wearbili.remake.app.login.domain

import android.util.Base64
import cn.spacexc.wearbili.remake.app.login.BiliAppSigner
import cn.spacexc.wearbili.remake.common.networking.KtorNetworkUtils
import cn.spacexc.wearbili.remake.common.networking.NetworkResponse
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录相关接口封装。
 *
 * 端点与参数对齐 PiliPlus / 黑盒实测（2026-10）：
 *  密码登录 : POST /x/passport-login/oauth2/login          (appSign, App 端)
 *  短信发送 : POST /x/passport-tv-login/sms/send           (appSign, TV 端，无极验)
 *  短信登录 : POST /x/passport-tv-login/login/sms          (appSign, TV 端，无极验)
 *  公钥获取 : GET  /x/passport-login/web/key
 *  扫码申请 : POST /x/passport-tv-login/qrcode/auth_code   (appSign, TV 端)
 *  扫码轮询 : POST /x/passport-tv-login/qrcode/poll        (appSign, TV 端)
 *
 * 注：App 端短信接口 /x/passport-login/sms/send 强制极验（gee_validate
 * 空/假值均被 -105 拒绝），手表端无法完成极验，故短信走 TV 端通道。
 */
@Singleton
class LoginApi @Inject constructor(
    private val networkUtils: KtorNetworkUtils
) {
    companion object {
        private const val PASSPORT = "https://passport.bilibili.com"
    }

    /** 获取密码加密用的 RSA 公钥（key = PEM 公钥，hash = salt） */
    suspend fun getWebKey(): GenericEnvelope<WebKeyData>? {
        val response: NetworkResponse<GenericEnvelope<WebKeyData>> =
            networkUtils.get("$PASSPORT/x/passport-login/web/key")
        return response.data
    }

    /**
     * 申请 TV 端二维码。
     *
     * 返回的 authCode 用于轮询，url 用于生成二维码图片。
     * TV 端接口比 web 端更稳定，且不依赖浏览器 UA。
     */
    suspend fun requestTvQrCode(): Pair<String, String>? {
        val response: NetworkResponse<TvQrCodeEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-tv-login/qrcode/auth_code",
            params = mapOf(
                "local_id" to "0",
                "platform" to "android",
                "mobi_app" to "android_hd"
            )
        )
        val data = response.data?.data ?: return null
        if (response.data?.code != 0) return null
        val authCode = data.authCode ?: return null
        val url = data.url ?: return null
        return authCode to url
    }

    /** 轮询二维码扫描状态 */
    suspend fun pollTvQrCode(authCode: String): TvQrCodePollEnvelope? {
        val response: NetworkResponse<TvQrCodePollEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-tv-login/qrcode/poll",
            params = mapOf(
                "auth_code" to authCode,
                "local_id" to "0"
            )
        )
        return response.data
    }

    /**
     * 密码登录（App 端）。
     *
     * 密码需要 RSA 加密后再 base64：`encrypt(salt + password)`。
     * salt 来自 [getWebKey] 的 `hash` 字段。
     */
    suspend fun loginByPassword(
        username: String,
        password: String,
        rsaKey: String,
        salt: String,
        deviceId: String,
        buvid: String
    ): PasswordLoginEnvelope? {
        val encryptedPassword = try {
            rsaEncrypt(rsaKey, salt + password)
        } catch (e: Exception) {
            return PasswordLoginEnvelope(code = -1, message = "密码加密失败: ${e.message}")
        } ?: return PasswordLoginEnvelope(code = -1, message = "密码加密失败")

        val randomString = (1..16)
            .map { "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }
            .joinToString("")
        val dt = try {
            rsaEncrypt(rsaKey, randomString)
        } catch (e: Exception) {
            null
        }

        val response: NetworkResponse<PasswordLoginEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-login/oauth2/login",
            params = mapOf(
                "bili_local_id" to deviceId,
                "build" to "2001100",
                "buvid" to buvid,
                "c_locale" to "zh_CN",
                "channel" to "master",
                "device" to "phone",
                "device_id" to deviceId,
                "device_name" to "vivo",
                "device_platform" to "Android14vivo",
                "disable_rcmd" to "0",
                "dt" to dt,
                "from_pv" to "main.homepage.avatar-nologin.all.click",
                "from_url" to "bilibili://pegasus/promo",
                "local_id" to buvid,
                "mobi_app" to "android_hd",
                "password" to encryptedPassword,
                "permission" to "ALL",
                "platform" to "android",
                "s_locale" to "zh_CN",
                "statistics" to BiliAppSigner.STATISTICS,
                "username" to username
            )
        )
        return response.data
    }

    /**
     * 发送短信验证码（TV 端）。
     *
     * 黑盒实测（2026-10）：App 端 `/x/passport-login/sms/send` 强制极验，
     * 缺少/伪造 gee_validate 均被拒（-105），手表端无法完成极验交互；
     * 而 TV 端 `/x/passport-tv-login/sms/send` 不需要极验，
     * 只需 tel + cid(国际冠字码) + login_session_id + 基础设备参数（appSign 签名）。
     *
     * cid 固定 "86"（中国大陆区号）。
     * [loginSessionId] 由调用方生成，发送与登录两步必须使用同一值。
     */
    suspend fun sendSmsCode(
        tel: String,
        buvid: String,
        loginSessionId: String
    ): SmsSendEnvelope? {
        val response: NetworkResponse<SmsSendEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-tv-login/sms/send",
            params = mapOf(
                "build" to "2001100",
                "buvid" to buvid,
                "cid" to "86",
                "login_session_id" to loginSessionId,
                "local_id" to buvid,
                "mobi_app" to "android_hd",
                "platform" to "android",
                "tel" to tel
            )
        )
        return response.data
    }

    /**
     * 短信验证码登录（TV 端）。
     *
     * @param captchaKey 来自 [sendSmsCode] 成功响应的 `data.captcha_key`
     * @param loginSessionId 必须与 [sendSmsCode] 传同一值
     */
    suspend fun loginBySmsCode(
        tel: String,
        code: String,
        captchaKey: String,
        buvid: String,
        loginSessionId: String
    ): SmsLoginEnvelope? {
        val response: NetworkResponse<SmsLoginEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-tv-login/login/sms",
            params = mapOf(
                "build" to "2001100",
                "buvid" to buvid,
                "captcha_key" to captchaKey,
                "cid" to "86",
                "code" to code,
                "login_session_id" to loginSessionId,
                "local_id" to buvid,
                "mobi_app" to "android_hd",
                "platform" to "android",
                "tel" to tel
            )
        )
        return response.data
    }

    /**
     * 获取图形验证码 key（`/x/passport-login/captcha?source=main_web`）。
     *
     * 返回的 geetest.gt / geetest.challenge 用于第三方极验 SDK 验证。
     * 手表端没有 WebView，这里只取 captcha_key（cid）走无图形码的降级路径。
     */
    suspend fun getCaptchaKey(): Pair<String, String>? {
        val response: NetworkResponse<CaptchaEnvelope> =
            networkUtils.get("$PASSPORT/x/passport-login/captcha?source=main_web")
        val data = response.data?.data ?: return null
        return (data.geetest?.gt ?: "") to (data.token ?: "")
    }

    /**
     * RSA 公钥加密并 base64（NO_WRAP）。
     *
     * 公钥格式为 PEM（含 BEGIN/END 头），先剥离头尾与换行再 base64 解码成
     * X509EncodedKeySpec。
     */
    private fun rsaEncrypt(pemKey: String, plainText: String): String? {
        return try {
            val normalized = pemKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\n", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim()
            val keyBytes = Base64.decode(normalized, Base64.DEFAULT)
            val publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(keyBytes))
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
