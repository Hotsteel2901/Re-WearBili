package cn.spacexc.wearbili.remake.app.video.info.comment.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cn.spacexc.bilibilisdk.utils.UserUtils
import cn.spacexc.wearbili.common.exception.PagingDataLoadFailedException
import cn.spacexc.wearbili.remake.app.video.info.comment.domain.CommentContentData
import cn.spacexc.wearbili.remake.app.video.info.comment.domain.VideoComment
import cn.spacexc.wearbili.remake.common.networking.KtorNetworkUtils
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** Cursor key for both the page-based logged-in API and the offset-based guest API. */
data class CommentPageKey(
    val page: Int,
    val offset: String? = null,
)

class CommentPagingSource(
    private val networkUtils: KtorNetworkUtils,
    private val oid: String,
) : PagingSource<CommentPageKey, CommentContentData>() {
    private val gson = Gson()

    override fun getRefreshKey(state: PagingState<CommentPageKey, CommentContentData>): CommentPageKey? = null

    override suspend fun load(
        params: LoadParams<CommentPageKey>,
    ): LoadResult<CommentPageKey, CommentContentData> {
        val pageKey = params.key ?: CommentPageKey(page = 1)
        val loggedIn = UserUtils.isUserLoggedIn()
        val url = if (loggedIn) {
            "https://api.bilibili.com/x/v2/reply?oid=${oid.encodeQueryComponent()}&type=1&pn=${pageKey.page}&ps=20&sort=1"
        } else {
            val pagination = gson.toJson(mapOf("offset" to pageKey.offset.orEmpty()))
                .encodeQueryComponent()
            "https://api.bilibili.com/x/v2/reply/main?oid=${oid.encodeQueryComponent()}&type=1&mode=3&pagination_str=$pagination"
        }

        return try {
            val response = networkUtils.get<VideoComment>(url)
            if (response.code != 0) {
                return LoadResult.Error(
                    PagingDataLoadFailedException(
                        apiUrl = response.apiUrl,
                        code = response.code,
                    ),
                )
            }

            val commentData = response.data?.data ?: return LoadResult.Error(
                PagingDataLoadFailedException(response.apiUrl, -1),
            )
            val comments = commentData.replies.orEmpty()
            val topComment = commentData.top?.upper?.apply { is_top = true }
            val pageComments = if (pageKey.page == 1 && topComment != null) {
                listOf(topComment) + comments
            } else {
                comments
            }

            val nextKey = when {
                commentData.cursor.is_end -> null
                loggedIn -> CommentPageKey(page = pageKey.page + 1)
                else -> commentData.cursor.pagination_reply?.next_offset
                    ?.takeIf { it.isNotBlank() && it != pageKey.offset }
                    ?.let { CommentPageKey(page = pageKey.page + 1, offset = it) }
            }
            LoadResult.Page(
                data = pageComments,
                prevKey = if (loggedIn && pageKey.page > 1) {
                    CommentPageKey(page = pageKey.page - 1)
                } else {
                    null
                },
                nextKey = nextKey,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun String.encodeQueryComponent(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
