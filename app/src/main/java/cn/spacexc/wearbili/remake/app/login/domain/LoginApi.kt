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
 * 端点与参数完全对齐 PiliPlus 的 `lib/http/login.dart`：
 *  密码登录 : POST /x/passport-login/oauth2/login   (appSign)
 *  短信发送 : POST /x/passport-login/sms/send       (appSign)
 *  短信登录 : POST /x/passport-login/login/sms      (appSign)
 *  公钥获取 : GET  /x/passport-login/web/key
 *  扫码申请 : POST /x/passport-tv-login/qrcode/auth_code  (appSign, TV 端)
 *  扫码轮询 : POST /x/passport-tv-login/qrcode/poll       (appSign, TV 端)
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
     * 发送短信验证码（App 端）。
     *
     * @param cid 图形验证码的 captcha_key，需要先调 [getCaptchaKey] 获取
     */
    suspend fun sendSmsCode(
        tel: String,
        cid: String,
        buvid: String,
        deviceId: String
    ): SmsSendEnvelope? {
        val timestamp = System.currentTimeMillis()
        val loginSessionId = BiliAppSigner.md5(buvid + timestamp.toString())
        val response: NetworkResponse<SmsSendEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-login/sms/send",
            params = mapOf(
                "build" to "2001100",
                "buvid" to buvid,
                "c_locale" to "zh_CN",
                "channel" to "master",
                "cid" to cid,
                "disable_rcmd" to "0",
                "local_id" to buvid,
                "login_session_id" to loginSessionId,
                "mobi_app" to "android_hd",
                "platform" to "android",
                "s_locale" to "zh_CN",
                "statistics" to BiliAppSigner.STATISTICS,
                "tel" to tel,
                "ts" to (timestamp / 1000).toString()
            )
        )
        return response.data
    }

    /**
     * 短信验证码登录（App 端）。
     *
     * @param captchaKey 来自 [sendSmsCode] 返回的 `captcha_key`
     */
    suspend fun loginBySmsCode(
        tel: String,
        code: String,
        captchaKey: String,
        cid: String,
        buvid: String,
        deviceId: String
    ): SmsLoginEnvelope? {
        val response: NetworkResponse<SmsLoginEnvelope> = networkUtils.postFormWithAppSign(
            url = "$PASSPORT/x/passport-login/login/sms",
            params = mapOf(
                "bili_local_id" to deviceId,
                "build" to "2001100",
                "buvid" to buvid,
                "c_locale" to "zh_CN",
                "captcha_key" to captchaKey,
                "channel" to "master",
                "cid" to cid,
                "code" to code,
                "device" to "phone",
                "device_id" to deviceId,
                "device_name" to "vivo",
                "device_platform" to "Android14vivo",
                "disable_rcmd" to "0",
                "from_pv" to "main.my-information.my-login.0.click",
                "from_url" to "bilibili://user_center/mine",
                "local_id" to buvid,
                "mobi_app" to "android_hd",
                "platform" to "android",
                "s_locale" to "zh_CN",
                "statistics" to BiliAppSigner.STATISTICS,
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
