package cn.spacexc.wearbili.remake.app.login

import java.net.URLEncoder
import java.security.MessageDigest

/**
 * B 站 App 端接口签名（appSign）。
 *
 * 密码登录、短信登录等 App 端 passport 接口都要求携带 `appkey` + `ts` + `sign`，
 * 其中 sign = MD5(字典序拼装的 query + appsec)。
 *
 * 算法与 PiliPlus `lib/utils/app_sign.dart` 的 AppSign.appSign 保持一致：
 *  1. 注入 appkey、ts（秒级时间戳）
 *  2. 按 key 字典序排序
 *  3. 逐项 `Uri.encodeComponent(key)=Uri.encodeComponent(value)` 用 `&` 连接
 *  4. 末尾直接拼接 appsec（appsec 不参与编码）后取 MD5 十六进制小写
 *
 * 注意：空字符串 value 只输出 key（不输出 `=`），这与 Dart 端
 * `if (value != null && value.isNotEmpty)` 的行为对齐。
 */
object BiliAppSigner {

    /** App 端 key/secret（B 站 Android 客户端公开常量，与 PiliPlus 一致） */
    const val APP_KEY = "dfca71928277209b"
    const val APP_SEC = "b5475a8825547a4fc26c7d518eaaa02e"

    /** 移动端 App UA（android_hd 版） */
    const val USER_AGENT =
        "Mozilla/5.0 BiliDroid/2.0.1 (bbcallen@gmail.com) os/android model/android_hd " +
                "mobi_app/android_hd build/2001100 channel/master innerVer/2001100 osVer/15 network/2"

    const val STATISTICS = """{"appId":5,"platform":3,"version":"2.0.1","abtest":""}"""

    /** 与 PiliPlus 保持一致的公共请求头 */
    val BASE_HEADERS: Map<String, String> = mapOf(
        "env" to "prod",
        "app-key" to "android64",
        "x-bili-aurora-zone" to "sh001"
    )

    /**
     * 对参数做 appSign，返回**新的**不可变 Map（已包含 appkey / ts / sign）。
     *
     * 不修改入参，避免调用方复用 Map 时被意外污染。
     */
    fun sign(
        params: Map<String, String?>,
        appKey: String = APP_KEY,
        appSec: String = APP_SEC
    ): Map<String, String> {
        val ts = (System.currentTimeMillis() / 1000).toString()
        val merged = LinkedHashMap<String, String?>(params.size + 3)
        merged.putAll(params)
        merged["appkey"] = appKey
        merged["ts"] = ts

        val query = merged.entries
            .sortedBy { it.key }
            .joinToString(separator = "&") { (key, value) ->
                val encodedKey = urlEncode(key)
                if (value.isNullOrEmpty()) {
                    encodedKey
                } else {
                    "$encodedKey=${urlEncode(value)}"
                }
            }

        val sign = md5(query + appSec)
        val result = LinkedHashMap<String, String>(merged.size)
        merged.forEach { (key, value) -> result[key] = value.orEmpty() }
        result["sign"] = sign
        return result
    }

    /**
     * URL 编码，与 Dart 的 `Uri.encodeComponent` 语义对齐：
     * 空格编码为 `%20`（而非 `+`），且不转义 `-_.!~*'()`。
     */
    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")

    fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
