package cn.spacexc.wearbili.remake.app.main.dynamic.domain.paging

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal object DynamicApiUrl {
    private const val API_BASE = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed"
    private const val FEATURES = "itemOpusStyle,listOnlyfans,onlyfansQaCard"

    fun all(page: Int, offset: String?): String = build(
        endpoint = "$API_BASE/all",
        page = page,
        offset = offset,
    )

    fun space(mid: Long, page: Int, offset: String?): String = build(
        endpoint = "$API_BASE/space",
        page = page,
        offset = offset,
        hostMid = mid,
    )

    private fun build(
        endpoint: String,
        page: Int,
        offset: String?,
        hostMid: Long? = null,
    ): String {
        val parameters = linkedMapOf(
            "timezone_offset" to "-480",
            "type" to "all",
            "page" to page.toString(),
            "features" to FEATURES,
            "platform" to "web",
            "web_location" to "333.1365",
        )
        hostMid?.let { parameters["host_mid"] = it.toString() }
        offset?.takeIf(String::isNotBlank)?.let { parameters["offset"] = it }

        val query = parameters.entries.joinToString("&") { (name, value) ->
            "${name.encodeQueryComponent()}=${value.encodeQueryComponent()}"
        }
        return "$endpoint?$query"
    }

    private fun String.encodeQueryComponent(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
