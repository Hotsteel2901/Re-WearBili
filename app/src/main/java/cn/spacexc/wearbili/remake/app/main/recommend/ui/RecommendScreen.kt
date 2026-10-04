package cn.spacexc.wearbili.remake.app.main.recommend.ui

import kotlinx.coroutines.launch
import cn.spacexc.wearbili.remake.common.ui.theme.AppTheme
import cn.spacexc.wearbili.remake.common.ui.theme.body1
import cn.spacexc.wearbili.remake.common.ui.theme.h1

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.TextButton
import androidx.compose.material.DismissDirection
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cn.spacexc.wearbili.common.domain.video.toShortChinese
import cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.app.Item
import cn.spacexc.wearbili.remake.app.settings.LocalConfiguration
import cn.spacexc.wearbili.remake.app.settings.SettingsManager
import cn.spacexc.wearbili.remake.app.settings.experimantal.EXPERIMENTAL_LARGE_VIDEO_CARD
import cn.spacexc.wearbili.remake.app.settings.experimantal.getActivatedExperimentalFunctions
import cn.spacexc.wearbili.remake.app.settings.toolbar.ui.StaticFunctionSlot
import cn.spacexc.wearbili.remake.app.settings.toolbar.ui.functionList
import cn.spacexc.wearbili.remake.app.settings.toolbar.ui.toFunctionDetail
import cn.spacexc.wearbili.remake.app.splash.remote.Version
import cn.spacexc.wearbili.remake.app.update.ui.UpdateCard
import cn.spacexc.wearbili.remake.app.video.info.ui.VIDEO_TYPE_AID
import cn.spacexc.wearbili.remake.app.video.info.ui.VIDEO_TYPE_BVID
import cn.spacexc.bilibilisdk.sdk.video.action.VideoAction
import cn.spacexc.wearbili.remake.common.UIState
import cn.spacexc.wearbili.remake.common.ToastUtils
import cn.spacexc.wearbili.remake.common.ui.AutoResizedText
import cn.spacexc.wearbili.remake.common.ui.BilibiliPink
import cn.spacexc.wearbili.remake.common.ui.Card
import cn.spacexc.wearbili.remake.common.ui.LoadableBox
import cn.spacexc.wearbili.remake.common.ui.LoadingTip
import cn.spacexc.wearbili.remake.common.ui.VideoCard
import cn.spacexc.wearbili.remake.common.ui.isRound
import cn.spacexc.wearbili.remake.common.ui.lazyRotateInput
import cn.spacexc.wearbili.remake.common.ui.titleBackgroundHorizontalPadding
import cn.spacexc.wearbili.remake.common.ui.wearBiliAnimateContentPlacement
import cn.spacexc.wearbili.remake.proto.settings.AppConfiguration
import cn.spacexc.wearbili.remake.proto.settings.QuickToolBarFunction
import cn.spacexc.wearbili.remake.proto.settings.QuickToolBarSlotCount
import cn.spacexc.wearbili.remake.proto.settings.RecommendSource
import cn.spacexc.wearbili.remake.proto.settings.copy

/**
 * Created by XC-Qan on 2023/4/6.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 */

data class RecommendScreenState(
    val isRefreshing: Boolean = false,
    val videoList: List<Any> = emptyList(),
    val uiState: UIState = UIState.Loading,
    val scrollState: LazyListState = LazyListState()
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel,
    navController: NavController,
    updatesResult: Version?,
    onFetch: (isRefresh: Boolean) -> Unit
) {
    val state = viewModel.screenState
    val focusRequester = remember { FocusRequester() }
    val pullRefreshState =
        rememberPullRefreshState(refreshing = state.isRefreshing, onRefresh = {
            onFetch(true)
        }, refreshThreshold = 40.dp)
    val localDensity = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLargeCard by remember {
        derivedStateOf {
            configuration.getActivatedExperimentalFunctions()
                .contains(EXPERIMENTAL_LARGE_VIDEO_CARD)
        }
    }
    // 浏览体验配置（增强功能）：过滤推广内容 / 紧凑布局
    val browsing = configuration.browsing
    val hideAds = browsing.hideAds
    val compactMode = browsing.compactMode
    // 长按菜单目标（videoIdType to videoId），非 null 时弹出快捷菜单
    var longPressTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    val scope = rememberCoroutineScope()
    LoadableBox(
        uiState = state.uiState, modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        onRetry = {
            viewModel.getRecommendVideos(true, configuration.recommendSource)
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .lazyRotateInput(focusRequester, state.scrollState),
            contentPadding = PaddingValues(
                start = titleBackgroundHorizontalPadding(),
                end = titleBackgroundHorizontalPadding(),
                bottom = 6.dp,
                top = if (isRound()) 8.dp else 0.dp
            ),
            state = state.scrollState,
            // 紧凑布局：负间距收紧卡片，让一屏容纳更多内容
            verticalArrangement = Arrangement.spacedBy(if (compactMode) (-4).dp else 0.dp)
        ) {
            //if (configuration.toolBarConfiguration.slotCount != QuickToolBarSlotCount.Zero) {
            item(key = "quickToolBar") {
                QuickToolBar(
                    configuration = configuration,
                    navController = navController,
                    density = localDensity,
                    lazyListState = state.scrollState
                )
            }
            //}
            updatesResult?.let {
                item {
                    UpdateCard(
                        updateInfo = updatesResult,
                        clickable = true,
                        navController = navController
                    )
                }
            }

            when (configuration.recommendSource) {
                null -> {}
                RecommendSource.App -> {
                    (state.videoList as List<Item>)
                        // 过滤推广：只保留真实稿件（goto == "av"）；
                        // 开启 hideAds 时进一步剔除标题明显带推广标记的项
                        .filter { if (hideAds) !it.isPromotedContent() else true }
                        .forEach { video ->
                        val videoId = video.bvid?.takeIf(String::isNotBlank)
                            ?: video.param?.takeIf(String::isNotBlank)
                        if (video.goto == "av" && !videoId.isNullOrBlank()) {
                            try {
                                item(key = video.uri ?: videoId) {
                                    VideoCard(
                                        videoName = video.title.orEmpty(),
                                        uploader = video.args?.up_name.orEmpty(),
                                        views = video.cover_left_text_2.orEmpty(),
                                        coverUrl = video.cover.orEmpty(),
                                        navController = navController,
                                        videoIdType = if (video.bvid.isNullOrBlank()) VIDEO_TYPE_AID else VIDEO_TYPE_BVID,
                                        videoId = videoId,
                                        modifier = Modifier.wearBiliAnimateContentPlacement(this),
                                        isLarge = isLargeCard,
                                        onLongClick = if (browsing.longPressMenu) {
                                            { longPressTarget = VIDEO_TYPE_BVID to videoId }
                                        } else null
                                    )
                                }
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                RecommendSource.Web -> {
                    (state.videoList as List<cn.spacexc.wearbili.remake.app.main.recommend.domain.remote.rcmd.web.Item>/* 这里真的没事的（确信 */)
                        .filter { if (hideAds) !it.isPromotedContent() else true }
                        .forEach {
                        if (it.goto == "av") {
                            try {
                                item(key = it.bvid) {
                                    VideoCard(
                                        videoName = it.title,
                                        uploader = it.owner?.name ?: "",
                                        views = it.stat?.view?.toShortChinese()
                                            ?: "",
                                        coverUrl = it.pic,
                                        navController = navController,
                                        videoId = it.bvid,
                                        videoIdType = VIDEO_TYPE_BVID,
                                        modifier = Modifier.wearBiliAnimateContentPlacement(this),
                                        isLarge = isLargeCard,
                                        onLongClick = if (browsing.longPressMenu) {
                                            { longPressTarget = VIDEO_TYPE_BVID to it.bvid }
                                        } else null
                                    )
                                }
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                RecommendSource.UNRECOGNIZED -> {
                    item {
                        Text(text = "既然你都乱改配置文件了，那就自己找视频看去吧！")
                    }
                }
            }
            item {
                LoadingTip(onRetry = {
                    viewModel.getRecommendVideos(false, configuration.recommendSource)
                })
                LaunchedEffect(key1 = Unit) {
                    onFetch(false)
                }
            }
        }
        LaunchedEffect(key1 = Unit, block = {
            focusRequester.requestFocus()
        })
        PullRefreshIndicator(
            refreshing = state.isRefreshing, state = pullRefreshState,
            modifier = Modifier.align(
                Alignment.TopCenter
            ),
        )
    }

    // 长按快捷菜单：稍后再看（不感兴趣需要 App 端风控接口，手表端降级为仅稍后再看）
    longPressTarget?.let { (idType, videoId) ->
        AlertDialog(
            onDismissRequest = { longPressTarget = null },
            title = {
                Text(
                    text = "快捷操作",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ListItem(
                        headlineContent = { Text("加入稍后再看", fontSize = 12.sp) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                scope.launch {
                                    val response = VideoAction.addToWatchLater(idType, videoId)
                                    ToastUtils.showText(
                                        if (response.code == 0) "已加入稍后再看"
                                        else "失败: ${response.message ?: response.code}"
                                    )
                                }
                                longPressTarget = null
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { longPressTarget = null }) {
                    Text("取消", fontSize = 12.sp)
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.QuickToolBar(
    configuration: AppConfiguration,
    lazyListState: LazyListState,
    navController: NavController,
    density: Density,
) {
    var isToolTipDisplaying by remember {
        mutableStateOf(!configuration.haveToolBarTipDisplayed)
    }
    val tooltipConfiguration = configuration.toolBarConfiguration
    LaunchedEffect(key1 = isToolTipDisplaying) {
        SettingsManager.updateConfiguration {
            copy {
                haveToolBarTipDisplayed = !isToolTipDisplaying
            }
        }
        println(configuration)
    }
    LaunchedEffect(key1 = configuration.haveToolBarTipDisplayed) {
        isToolTipDisplaying = !configuration.haveToolBarTipDisplayed
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isToolTipDisplaying) {
            val dismissState = rememberDismissState()
            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                isToolTipDisplaying = false
            }
            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wearBiliAnimateContentPlacement(this@QuickToolBar),
                directions = setOf(DismissDirection.EndToStart),
                dismissThresholds = { direction ->
                    FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
                },
                background = {}
            ) {
                Card(
                    innerPaddingValues = PaddingValues(
                        vertical = 10.dp,
                        horizontal = 8.dp
                    ),
                    onClick = {
                        /*context.startActivity(
                            Intent(
                                context,
                                QuickToolbarCustomizationActivity::class.java
                            )
                        )*/
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var textHeight by remember {
                            mutableStateOf(0.dp)
                        }
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = null,
                            tint = BilibiliPink,
                            modifier = Modifier.rotate(32.1f)//.size(textHeight * 0.45f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.onSizeChanged {
                            textHeight = with(density) { it.height.toDp() }
                        }) {
                            Text(
                                text = "快捷功能区",
                                //fontFamily = wearbiliFontFamily,
                                style = AppTheme.typography.h1.copy(fontSize = 12.sp)
                            )
                            AutoResizedText(
                                text = "点击设置快捷入口\n左划以隐藏此卡片\n可在“设置”中重新设置",
                                //fontFamily = wearbiliFontFamily,
                                style = AppTheme.typography.body1.copy(fontSize = 11.sp),
                                maxLines = 3,
                                modifier = Modifier.alpha(0.6f)
                            )
                        }
                    }
                }
            }
        }

        if (tooltipConfiguration.slotCount != QuickToolBarSlotCount.Zero) {
            val firstFunction = tooltipConfiguration.functionOne.toFunctionDetail ?: functionList[0]
            val secondFunction =
                tooltipConfiguration.functionTwo.toFunctionDetail ?: functionList[1]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (firstFunction.type != QuickToolBarFunction.None) {
                    StaticFunctionSlot(
                        modifier = Modifier
                            .weight(1f),
                        function = firstFunction,
                        slotCount = tooltipConfiguration.slotCount
                    ) {
                        firstFunction.action(navController)
                    }
                }
                if (secondFunction.type != QuickToolBarFunction.None) {
                    StaticFunctionSlot(
                        function = secondFunction,
                        modifier = Modifier
                            .weight(1f),
                        slotCount = tooltipConfiguration.slotCount
                    ) {
                        secondFunction.action(navController)
                    }
                }
            }
        }

        if (configuration.toolBarConfiguration.slotCount != QuickToolBarSlotCount.Zero || isToolTipDisplaying) {
            Divider(
                color = Color(48, 48, 48), modifier = Modifier
                    .clip(
                        CircleShape
                    )
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.2f)
                    .padding(vertical = 6.dp)
            )
            LaunchedEffect(key1 = Unit) {
                lazyListState.scrollToItem(0)
            }
        }
    }
}