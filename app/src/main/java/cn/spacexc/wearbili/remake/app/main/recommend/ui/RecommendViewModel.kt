package cn.spacexc.wearbili.remake.app.main.recommend.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.app.Item as AppRecommendItem
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.app.RecommendVideo as AppRecommendVideo
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.web.Item as WebRecommendItem
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.web.WebRecommendVideo
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.common.UIState
import cn.spacexc.wearbili.remake.common.networking.KtorNetworkUtils
import cn.spacexc.wearbili.remake.proto.settings.RecommendSource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.request.header
import io.ktor.client.request.userAgent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val networkUtils: KtorNetworkUtils,
) : ViewModel() {
    var screenState by mutableStateOf(RecommendScreenState())
        private set

    private var activeSource: RecommendSource? = null
    private var nextFreshIndex = 0
    private var isFetching = false

    init {
        getRecommendVideos(true, SettingsManager.getConfiguration().recommendSource)
    }

    fun getRecommendVideos(isRefresh: Boolean, recommendSource: RecommendSource) {
        if (isFetching) return

        if (activeSource != recommendSource) {
            activeSource = recommendSource
            nextFreshIndex = 0
            screenState = screenState.copy(
                uiState = UIState.Loading,
                videoList = emptyList(),
                isRefreshing = false,
            )
        } else if (isRefresh) {
            nextFreshIndex = 0
        }

        val requestedIndex = nextFreshIndex
        isFetching = true
        viewModelScope.launch {
            if (screenState.videoList.isEmpty()) {
                screenState = screenState.copy(uiState = UIState.Loading)
            }
            if (isRefresh) {
                screenState = screenState.copy(isRefreshing = true)
            }

            try {
                val page = when (recommendSource) {
                    RecommendSource.App -> fetchAppRecommendations(requestedIndex)
                    RecommendSource.Web -> fetchWebRecommendations(requestedIndex)
                    else -> RecommendationPage.Failure(-1)
                }

                if (page.errorCode != null) {
                    screenState = screenState.copy(
                        uiState = if (screenState.videoList.isEmpty()) {
                            UIState.Failed(page.errorCode)
                        } else {
                            UIState.Success
                        },
                        isRefreshing = false,
                    )
                } else {
                    val videos = if (isRefresh) {
                        page.videos
                    } else {
                        appendUnique(screenState.videoList, page.videos)
                    }
                    screenState = screenState.copy(
                        uiState = UIState.Success,
                        videoList = videos,
                        isRefreshing = false,
                    )
                    nextFreshIndex = requestedIndex + 1
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                screenState = screenState.copy(
                    uiState = if (screenState.videoList.isEmpty()) {
                        UIState.Failed(-1)
                    } else {
                        UIState.Success
                    },
                    isRefreshing = false,
                )
            } finally {
                isFetching = false
            }
        }
    }

    private suspend fun fetchWebRecommendations(freshIndex: Int): RecommendationPage {
        val response = networkUtils.getWithWbiSignature<WebRecommendVideo>(
            endpoint = "https://api.bilibili.com/x/web-interface/wbi/index/top/feed/rcmd",
            parameters = mapOf(
                "version" to "1",
                "feed_version" to "V8",
                "homepage_ver" to "1",
                "ps" to "20",
                "fresh_idx" to freshIndex.toString(),
                "brush" to freshIndex.toString(),
                "fresh_type" to "4",
            ),
        )
        if (response.code != 0) return RecommendationPage.Failure(response.code)
        val videos = response.data?.data?.item.orEmpty()
            .filter { item -> item.goto == "av" && item.owner != null }
        return RecommendationPage.Success(videos)
    }

    private suspend fun fetchAppRecommendations(freshIndex: Int): RecommendationPage {
        val parameters = linkedMapOf(
            "build" to "8430300",
            "c_locale" to "zh_CN",
            "channel" to "master",
            "column" to "2",
            "device" to "phone",
            "device_name" to "android",
            "device_type" to "0",
            "disable_rcmd" to "0",
            "flush" to "8",
            "fnval" to "976",
            "fnver" to "0",
            "force_host" to "2",
            "fourk" to "1",
            "guidance" to "1",
            "https_url_req" to "1",
            "idx" to freshIndex.toString(),
            "mobi_app" to "android_i",
            "network" to "wifi",
            "platform" to "android",
            "player_net" to "1",
            "pull" to (freshIndex == 0).toString(),
            "qn" to "32",
            "recsys_mode" to "0",
            "s_locale" to "zh_CN",
            "splash_id" to "",
            "statistics" to "{\"appId\":1,\"platform\":3,\"version\":\"8.43.0\",\"abtest\":\"\"}",
            "voice_balance" to "0",
        )
        val query = parameters.entries.joinToString("&") { (key, value) ->
            "${key.encodeQueryComponent()}=${value.encodeQueryComponent()}"
        }
        val response = networkUtils.get<AppRecommendVideo>(
            "https://app.bilibili.com/x/v2/feed/index?$query"
        ) {
            userAgent(APP_USER_AGENT)
            header("app-key", "android_hd")
            header("env", "prod")
            header("x-bili-trace-id", APP_TRACE_ID)
            header("bili-http-engine", "cronet")
        }
        if (response.code != 0) return RecommendationPage.Failure(response.code)
        val apiCode = response.data?.code ?: response.code
        if (apiCode != 0) return RecommendationPage.Failure(apiCode)
        val videos = response.data?.data?.items.orEmpty()
            .filter { item ->
                item.goto == "av" &&
                    item.card_goto != "ad_av" &&
                    item.card_goto != "ad_web_s" &&
                    item.ad_info == null &&
                    item.can_play == 1
            }
        return RecommendationPage.Success(videos)
    }

    private fun appendUnique(existing: List<Any>, incoming: List<Any>): List<Any> {
        val keys = existing.mapNotNull(::videoKey).toMutableSet()
        return existing + incoming.filter { item ->
            val key = videoKey(item)
            key == null || keys.add(key)
        }
    }

    private fun videoKey(item: Any): String? = when (item) {
        is AppRecommendItem -> item.bvid?.takeIf(String::isNotBlank)
            ?: item.param?.takeIf(String::isNotBlank)
            ?: item.uri
        is WebRecommendItem -> item.bvid.takeIf(String::isNotBlank)
        else -> null
    }

    private fun String.encodeQueryComponent(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name()).replace("+", "%20")

    private data class RecommendationPage(
        val videos: List<Any>,
        val errorCode: Int?,
    ) {
        companion object {
            fun Success(videos: List<Any>) = RecommendationPage(videos, null)
            fun Failure(code: Int) = RecommendationPage(emptyList(), code)
        }
    }

    private companion object {
        const val APP_TRACE_ID = "11111111111111111111111111111111:1111111111111111:0:0"
        const val APP_USER_AGENT =
            "Mozilla/5.0 BiliDroid/8.43.0 (bbcallen@gmail.com) os/android model/android mobi_app/android build/8430300 channel/master innerVer/8430300 osVer/15 network/2"
    }
}
