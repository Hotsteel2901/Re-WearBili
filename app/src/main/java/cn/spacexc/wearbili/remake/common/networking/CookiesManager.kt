package cn.spacexc.wearbili.remake.common.networking

import cn.spacexc.bilibilisdk.data.CookiesManager
import cn.spacexc.bilibilisdk.utils.UserUtils
import cn.spacexc.wearbili.common.CryptoManager
import cn.spacexc.wearbili.remake.common.networking.db.CookieEntity
import cn.spacexc.wearbili.remake.common.networking.db.CookiesRepository
import cn.spacexc.wearbili.remake.common.networking.db.toKtorCookie
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Locale

class CookiesManager(
    val repository: CookiesRepository,
    private val cryptoManager: CryptoManager
) : CookiesManager {
    private val mutex = Mutex()
    override suspend fun getCookieByName(name: String): Cookie? {
        return repository.dao.getCookieByName(name, UserUtils.mid())?.toKtorCookie(cryptoManager)
    }

    override suspend fun deleteAllCookies() {
        UserUtils.mid()?.let { repository.dao.deleteAll(it) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name.isEmpty()) return
        mutex.withLock {
            val uid = UserUtils.mid()
            repository.dao.addCookie(
                CookieEntity(
                    name = cookie.name,
                    value = cookie.value,
                    encoding = cookie.encoding,
                    maxAge = cookie.maxAge,
                    expires = cookie.expires?.timestamp,
                    domain = cookie.domain,
                    path = cookie.path,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly,
                    extensions = cookie.extensions,
                    uid = uid
                )
            )
        }
    }

    /**
     * 手动导入一整串 Cookie（形如 "SESSDATA=xxx; bili_jct=yyy; DedeUserID=123"）。
     *
     * 这是「Cookie 登录」的落盘入口：App 端登录接口返回的是 set-cookie 响应头，
     * 而用户手动粘贴的是裸 Cookie 串，两者的持久化路径需要统一。
     *
     * @return 成功写入的 Cookie 数量；0 表示没有任何有效项
     */
    suspend fun importCookies(rawCookies: String): Int {
        val parsed = rawCookies
            .split(";")
            .mapNotNull { segment ->
                val trimmed = segment.trim()
                // 只处理 name=value 形式，且值里允许出现 '='（base64 常见）
                val separatorIndex = trimmed.indexOf('=')
                if (separatorIndex <= 0) return@mapNotNull null
                val name = trimmed.substring(0, separatorIndex).trim()
                val value = trimmed.substring(separatorIndex + 1).trim()
                if (name.isEmpty() || value.isEmpty()) return@mapNotNull null
                CookieEntity(
                    name = name,
                    value = value,
                    encoding = CookieEncoding.RAW,
                    domain = ".bilibili.com",
                    path = "/",
                    uid = null
                )
            }
        if (parsed.isEmpty()) return 0

        val loginUid = parsed
            .firstOrNull { it.name == "DedeUserID" }
            ?.value
            ?.toLongOrNull()

        mutex.withLock {
            // uid 先写 null，再统一回填，避免 DedeUserID 排在串尾时前面的条目拿不到 uid
            parsed.forEach { repository.dao.addCookie(it) }
            if (loginUid != null) {
                UserUtils.addUser(loginUid)
                UserUtils.setCurrentUid(loginUid)
                val noUidCookies = repository.dao.getCookiesWithNoUid()
                noUidCookies.forEach { repository.dao.updateCookie(it.copy(uid = loginUid)) }
            }
        }
        return parsed.size
    }

    override suspend fun HttpResponse.interceptAndSaveCookies() {
        val cookies = buildList {
            headers.getAll("set-cookie")?.forEach {
                add(parseCookie(it))
            }
        }.filter { it.name.isNotEmpty() }
        if (cookies.isEmpty()) return
        val uid = cookies.find { it.name == "DedeUserID" }?.value?.toLongOrNull()
            ?: UserUtils.mid()
        if (uid != null) {
            cookies.forEach { it.uid = uid }
            UserUtils.addUser(uid)
            UserUtils.setCurrentUid(uid)
            cookies.forEach {
                repository.dao.addCookie(it)
            }
        }
    }

    private fun parseCookie(str: String): CookieEntity {
        // 同时兼容 "a=b; c=d" 与 name 本身为空串的脏数据
        val items = str.split(";")
        val (name, value) = run {
            val head = items[0].trim()
            val separatorIndex = head.indexOf('=')
            if (separatorIndex <= 0) {
                "" to ""
            } else {
                head.substring(0, separatorIndex).trim() to
                        head.substring(separatorIndex + 1).trim()
            }
        }
        val cookieEntity =
            CookieEntity(name = name, value = value, encoding = CookieEncoding.RAW, uid = null)
        for (index in 1..<items.size) {
            val currentItem = items[index].trim()
            if (currentItem.isEmpty()) continue
            if (currentItem.contains("=")) {
                val separatorIndex = currentItem.indexOf('=')
                val key = currentItem.substring(0, separatorIndex).trim()
                val itemValue = currentItem.substring(separatorIndex + 1).trim()
                when (key.lowercase()) {
                    "domain" -> cookieEntity.domain = itemValue
                    "path" -> cookieEntity.path = itemValue
                    "max-age" -> cookieEntity.maxAge = itemValue.toIntOrNull() ?: 0
                    "expires" -> {
                        // BUG FIX: 原实现把日期硬编码成 "Sat, 04 Jan 2025 03:41:20 GMT"，
                        // 完全忽略了服务端返回的真实 expires，导致所有 Cookie 的过期时间都是错的。
                        cookieEntity.expires = parseHttpDate(itemValue)
                    }
                }
            } else {
                when (currentItem.lowercase()) {
                    "httponly" -> cookieEntity.httpOnly = true
                    "secure" -> cookieEntity.secure = true
                }
            }
        }
        return cookieEntity
    }

    /** 解析 HTTP 日期（RFC 1123），失败返回 null 表示会话级 Cookie。 */
    private fun parseHttpDate(raw: String): Long? {
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            "EEEE, dd-MMM-yy HH:mm:ss z",
            "EEE MMM d HH:mm:ss yyyy"
        )
        for (pattern in patterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                format.isLenient = false
                format.timeZone = java.util.TimeZone.getTimeZone("GMT")
                format.parse(raw)?.let { return it.time }
            } catch (_: Exception) {
                // 换下一个格式继续尝试
            }
        }
        return null
    }

    override fun close() {}

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val list =
            repository.dao.getCookieByUid(UserUtils.mid()).map { it.toKtorCookie(cryptoManager) }
        println(list)
        return list
    }
}