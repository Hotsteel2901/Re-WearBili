package cn.spacexc.wearbili.remake.common.networking

import org.junit.Assert.assertEquals
import org.junit.Test

class BiliWbiSignerTest {
    @Test
    fun signsSortedParametersUsingBilibiliSanitizationRules() {
        val signedUrl = BiliWbiSigner.buildSignedUrl(
            endpoint = "https://api.bilibili.com/x/test",
            parameters = mapOf("foo" to "bar!", "baz" to "x*()"),
            mixinKey = "abcdefghijklmnopqrstuvwxyz123456",
            timestampSeconds = 1_700_000_000,
        )

        assertEquals(
            "https://api.bilibili.com/x/test?baz=x*%28%29&foo=bar%21&wts=1700000000&w_rid=07b1a1d1e3891b6746a6a7d3e822a154",
            signedUrl,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnInvalidMixinKey() {
        BiliWbiSigner.buildSignedUrl(
            endpoint = "https://api.bilibili.com/x/test",
            parameters = emptyMap(),
            mixinKey = "short",
            timestampSeconds = 1_700_000_000,
        )
    }
}
