package cn.spacexc.wearbili.remake.common.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.IntSize

/**
 * Created by XC-Qan on 2023/4/16.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 */

fun Modifier.wearBiliAnimatedContentSize(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(),
    finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null
): Modifier = composed {
    if (isAnimationEnabled) {
        animateContentSize(
            animationSpec, finishedListener
        )
    } else Modifier
}

/**
 * 列表项位移动画。
 *
 * 原实现用的 animateItemPlacement() 已在新版 Compose 中移除，
 * 替代品是 LazyItemScope.animateItem(fadeInSpec, placementSpec, fadeOutSpec)。
 * 参数顺序：淡入 / 位移 / 淡出。
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.wearBiliAnimateContentPlacement(scope: LazyItemScope): Modifier = composed {
    if (isAnimationEnabled) {
        with(scope) {
            animateItem(
                placementSpec = animationSpecOffset,
            )
        }
    } else Modifier
}
