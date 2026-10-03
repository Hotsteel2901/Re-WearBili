package cn.spacexc.wearbili.remake.app.search.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cn.spacexc.wearbili.common.exception.PagingDataLoadFailedException
import cn.spacexc.wearbili.remake.app.search.domain.remote.result.Search
import cn.spacexc.wearbili.remake.app.search.domain.remote.result.mediaft.SearchedMediaFt
import cn.spacexc.wearbili.remake.app.search.domain.remote.result.user.SearchedUser
import cn.spacexc.wearbili.remake.app.search.domain.remote.result.video.SearchedVideo
import cn.spacexc.wearbili.remake.common.networking.KtorNetworkUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchPagingSource(
    private val networkUtils: KtorNetworkUtils,
    private val keyword: String,
) : PagingSource<Int, SearchObject>() {
    private val gson = Gson()

    override fun getRefreshKey(state: PagingState<Int, SearchObject>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchObject> {
        val page = params.key ?: 1
        return try {
            val response = networkUtils.getWithWbiSignature<Search>(
                endpoint = "https://api.bilibili.com/x/web-interface/wbi/search/all/v2",
                parameters = mapOf(
                    "keyword" to keyword,
                    "page" to page.toString(),
                    "page_size" to "20",
                    "platform" to "pc",
                    "web_location" to "1430654",
                ),
            )
            if (response.code != 0) {
                return LoadResult.Error(
                    PagingDataLoadFailedException(
                        apiUrl = response.apiUrl,
                        code = response.code,
                    ),
                )
            }

            val data = response.data?.data
            val results = data?.result.orEmpty()
            val maxPage = data?.numPages ?: 0
            if (page == 1) {
                val users = decodeList<SearchedUser>(
                    results.firstOrNull { it.resultType == "bili_user" }?.data,
                ).map { SearchObject("bili_user", it) }
                val media = decodeList<SearchedMediaFt>(
                    results.firstOrNull { it.resultType == "media_ft" }?.data,
                ).map { SearchObject("media_ft", it) }
                val bangumi = decodeList<SearchedMediaFt>(
                    results.firstOrNull { it.resultType == "media_bangumi" }?.data,
                ).map { SearchObject("media_bangumi", it) }
                val videos = decodeList<SearchedVideo>(
                    results.firstOrNull { it.resultType == "video" }?.data,
                ).map { SearchObject("video", it) }

                LoadResult.Page(
                    data = users + bangumi + media + videos,
                    prevKey = null,
                    nextKey = if (page >= maxPage) null else page + 1,
                )
            } else {
                val videos = decodeList<SearchedVideo>(
                    results.firstOrNull { it.resultType == "video" }?.data,
                ).map { SearchObject("video", it) }
                LoadResult.Page(
                    data = videos,
                    prevKey = page - 1,
                    nextKey = if (page >= maxPage) null else page + 1,
                )
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private inline fun <reified T> decodeList(rawData: Any?): List<T> {
        if (rawData == null) return emptyList()
        val listType = object : TypeToken<List<T>>() {}.type
        return gson.fromJson<List<T>>(gson.toJsonTree(rawData), listType).orEmpty()
    }
}
