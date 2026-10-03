package cn.spacexc.wearbili.remake.common.networking

import cn.spacexc.bilibilisdk.sdk.video.info.remote.info.web.WebVideoInfo
import cn.spacexc.bilibilisdk.sdk.video.info.remote.playerinfo.PlayerInfo
import com.google.gson.annotations.SerializedName

/** Bilibili web endpoints whose current versions require a WBI signature. */
internal object BilibiliApi {
    private const val API_BASE = "https://api.bilibili.com"

    suspend fun videoInfo(
        networkUtils: KtorNetworkUtils,
        videoIdType: String,
        videoId: String,
    ): NetworkResponse<WebVideoInfo> = networkUtils.getWithWbiSignature(
        endpoint = "$API_BASE/x/web-interface/wbi/view",
        parameters = mapOf(videoIdType to videoId),
    )

    suspend fun playerInfo(
        networkUtils: KtorNetworkUtils,
        videoIdType: String,
        videoId: String,
        videoCid: Long,
    ): NetworkResponse<PlayerInfo> = networkUtils.getWithWbiSignature(
        endpoint = "$API_BASE/x/player/wbi/v2",
        parameters = mapOf(
            videoIdType to videoId,
            "cid" to videoCid.toString(),
        ),
    )

    /** Requests a progressive low-resolution stream for the legacy IJK player. */
    suspend fun videoDurl(
        networkUtils: KtorNetworkUtils,
        videoIdType: String,
        videoId: String,
        videoCid: Long,
    ): NetworkResponse<VideoDurlResponse> = networkUtils.getWithWbiSignature(
        endpoint = "$API_BASE/x/player/wbi/playurl",
        parameters = mapOf(
            videoIdType to videoId,
            "cid" to videoCid.toString(),
            "qn" to "16",
            "fnver" to "0",
            "fnval" to "0",
            "fourk" to "0",
            "platform" to "html5",
            "try_look" to "1",
        ),
    )

    suspend fun pgcVideoDurl(
        networkUtils: KtorNetworkUtils,
        videoCid: Long,
        bvid: String? = null,
        episodeId: Long? = null,
        seasonId: Long? = null,
    ): NetworkResponse<PgcVideoDurlResponse> {
        val parameters = mutableMapOf(
            "cid" to videoCid.toString(),
            "qn" to "16",
            "fnver" to "0",
            "fnval" to "0",
            "fourk" to "0",
        )
        bvid?.takeIf(String::isNotBlank)?.let { parameters["bvid"] = it }
        episodeId?.let { parameters["ep_id"] = it.toString() }
        seasonId?.let { parameters["season_id"] = it.toString() }
        return networkUtils.getWithWbiSignature(
            endpoint = "$API_BASE/pgc/player/web/v2/playurl",
            parameters = parameters,
        )
    }
}

data class VideoDurlResponse(
    val code: Int,
    val data: VideoDurlData?,
    val message: String?,
    val ttl: Int?,
)

data class VideoDurlData(
    val durl: List<VideoDurl>?,
    val timelength: Long?,
    @SerializedName("last_play_time")
    val lastPlayTime: Long?,
)

data class VideoDurl(
    val url: String?,
    @SerializedName("backup_url")
    val backupUrls: List<String>?,
)

data class PgcVideoDurlResponse(
    val code: Int,
    val result: PgcVideoDurlResult?,
    val message: String?,
    val ttl: Int?,
)

data class PgcVideoDurlResult(
    @SerializedName("video_info")
    val videoInfo: VideoDurlData?,
)
