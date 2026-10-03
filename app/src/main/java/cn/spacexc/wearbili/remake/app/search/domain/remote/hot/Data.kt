package cn.spacexc.wearbili.remake.app.search.domain.remote.hot

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("list")
    val list: List<TrendingWord>?,
    @SerializedName("top_list")
    val topList: List<TrendingWord>?,
)
