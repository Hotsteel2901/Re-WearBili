package cn.spacexc.wearbili.remake.app.login.domain

import com.google.gson.annotations.SerializedName

/**
 * App 端登录相关接口的响应模型。
 *
 * 字段命名严格对齐 B 站 passport 接口返回的 snake_case JSON，
 * 用 @SerializedName 显式映射，避免依赖 Gson 的命名策略。
 */

/** `/x/passport-login/web/key` —— 密码登录用的 RSA 公钥与 salt */
data class WebKeyData(
    val hash: String? = null,
    val key: String? = null
)

/** App 端接口通用的 code/message 信封 */
open class LoginEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("ttl") val ttl: Int = 1
)

/**
 * 登录成功后的账号数据。
 *
 * 注意：`cookie_info.cookies` 才是真正的凭证来源，
 * `access_token` / `refresh_token` 用于 token 刷新。
 */
data class LoginResultData(
    @SerializedName("status") val status: Int = 0,
    @SerializedName("message") val message: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("mid") val mid: Long = 0,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long = 0,
    @SerializedName("cookie_info") val cookieInfo: CookieInfo? = null,
    @SerializedName("sso") val sso: List<String>? = null,
    @SerializedName("token_info") val tokenInfo: TokenInfo? = null
)

data class CookieInfo(
    @SerializedName("cookies") val cookies: List<LoginCookie> = emptyList(),
    @SerializedName("domains") val domains: List<String> = emptyList()
)

data class LoginCookie(
    @SerializedName("name") val name: String = "",
    @SerializedName("value") val value: String = "",
    @SerializedName("http_only") val httpOnly: Int = 0,
    @SerializedName("expires") val expires: Long = 0,
    @SerializedName("secure") val secure: Int = 0
)

data class TokenInfo(
    @SerializedName("mid") val mid: Long = 0,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long = 0
)

/** `/x/passport-login/sms/send` —— 短信验证码发送结果 */
data class SmsSendData(
    @SerializedName("captcha_key") val captchaKey: String? = null,
    @SerializedName("recaptcha_url") val recaptchaUrl: String? = null
)

/** `/x/passport-login/sms/send` 的外层信封 */
data class SmsSendEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SmsSendData? = null
)

/** `/x/passport-login/login/sms` 的外层信封 */
data class SmsLoginEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LoginResultData? = null
)

/** `/x/passport-login/oauth2/login`（密码登录）的外层信封 */
data class PasswordLoginEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LoginResultData? = null
)

/** 通用信封（含 data） */
data class GenericEnvelope<T>(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

/**
 * 密码登录时风控要求手机验证（safeCenter）用的数据结构。
 *
 * 对应 `passport.bilibili.com/x/safecenter/user/info`。
 */
data class SafeCenterData(
    @SerializedName("account_info") val accountInfo: SafeCenterAccountInfo? = null
)

data class SafeCenterAccountInfo(
    @SerializedName("hide_tel") val hideTel: String? = null,
    @SerializedName("hide_mail") val hideMail: String? = null,
    @SerializedName("bind_mail") val bindMail: Boolean = false,
    @SerializedName("bind_tel") val bindTel: Boolean = false,
    @SerializedName("tel_verify") val telVerify: Boolean = false,
    @SerializedName("mid") val mid: Long = 0
)

/** TV 端二维码申请结果 */
data class TvQrCodeData(
    @SerializedName("auth_code") val authCode: String? = null,
    @SerializedName("url") val url: String? = null
)

data class TvQrCodeEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TvQrCodeData? = null
)

/** TV 端二维码轮询结果 */
data class TvQrCodePollData(
    @SerializedName("url") val url: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0,
    @SerializedName("code") val code: Long = 0,
    @SerializedName("message") val message: String? = null,
    @SerializedName("cookie_info") val cookieInfo: CookieInfo? = null
)

data class TvQrCodePollEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TvQrCodePollData? = null
)

/** 图形验证码 envelope */
data class CaptchaData(
    @SerializedName("token") val token: String? = null,
    @SerializedName("geetest") val geetest: CaptchaGeetest? = null
)

data class CaptchaGeetest(
    @SerializedName("gt") val gt: String? = null,
    @SerializedName("challenge") val challenge: String? = null
)

data class CaptchaEnvelope(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CaptchaData? = null
)
