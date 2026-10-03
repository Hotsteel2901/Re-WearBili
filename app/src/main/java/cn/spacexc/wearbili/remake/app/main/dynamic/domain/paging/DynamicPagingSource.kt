package cn.spacexc.wearbili.remake.app.main.dynamic.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cn.spacexc.wearbili.common.exception.PagingDataLoadFailedException
import cn.spacexc.wearbili.remake.app.main.dynamic.domain.remote.list.DynamicItem
import cn.spacexc.wearbili.remake.app.main.dynamic.domain.remote.list.DynamicList
import cn.spacexc.wearbili.remake.common.networking.KtorNetworkUtils

class DynamicPagingSource(
    private val networkUtils: KtorNetworkUtils,
) : PagingSource<Pair<Int, String?>, DynamicItem>() {
    private val requestedOffsets = mutableMapOf<Int, String?>()

    override fun getRefreshKey(state: PagingState<Pair<Int, String?>, DynamicItem>): Pair<Int, String?>? = null

    override suspend fun load(
        params: LoadParams<Pair<Int, String?>>,
    ): LoadResult<Pair<Int, String?>, DynamicItem> {
        val currentPage = params.key?.first ?: 1
        val offset = params.key?.second
        requestedOffsets[currentPage] = offset

        return try {
            val response = networkUtils.get<DynamicList>(
                DynamicApiUrl.all(currentPage, offset),
            )
            if (response.code != 0) {
                return LoadResult.Error(
                    PagingDataLoadFailedException(
                        apiUrl = response.apiUrl,
                        code = response.code,
                    ),
                )
            }

            val feed = response.data?.data ?: return LoadResult.Error(
                PagingDataLoadFailedException(response.apiUrl, -1),
            )
            val nextOffset = feed.offset.takeIf {
                feed.hasMore && it.isNotBlank() && it != offset
            }
            LoadResult.Page(
                data = feed.items,
                prevKey = if (currentPage <= 1) {
                    null
                } else {
                    Pair(currentPage - 1, requestedOffsets[currentPage - 1])
                },
                nextKey = nextOffset?.let { Pair(currentPage + 1, it) },
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}
