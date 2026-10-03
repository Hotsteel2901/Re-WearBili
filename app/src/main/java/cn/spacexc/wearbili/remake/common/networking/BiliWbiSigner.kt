package cn.spacexc.wearbili.remake.common.networking

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.TreeMap

/**
 * Builds the WBI-signed request URLs used by Bilibili's current web API.
 *
 * The signature is calculated from sorted, URL-encoded parameters after removing
 * the five characters Bilibili excludes from the signing string. The original
 * values are retained in the actual request URL.
 */
@PublishedApi
internal object BiliWbiSigner {
    private val signatureFilter = Regex("[!'()*]")

    fun buildSignedUrl(
        endpoint: String,
        parameters: Map<String, String>,
        mixinKey: String,
        timestampSeconds: Long = System.currentTimeMillis() / 1_000
    ): String {
        require(mixinKey.length >= 32) { "A valid WBI mixin key is required" }
        require(!endpoint.contains('?')) { "Pass query parameters separately from the endpoint" }

        val signedParameters = TreeMap(parameters)
        signedParameters["wts"] = timestampSeconds.toString()

        val signingQuery = signedParameters.entries.joinToString("&") { (name, value) ->
            "${encodeComponent(name)}=${encodeComponent(value.replace(signatureFilter, ""))}"
        }
        val signature = md5(signingQuery + mixinKey.take(32))
        val requestQuery = signedParameters.entries.joinToString("&") { (name, value) ->
            "${encodeComponent(name)}=${encodeComponent(value)}"
        }
        val separator = if (endpoint.endsWith("?")) "" else "?"
        return "$endpoint$separator${requestQuery}&w_rid=$signature"
    }

    private fun encodeComponent(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
            .replace("%7E", "~")

    private fun md5(value: String): String = MessageDigest.getInstance("MD5")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
