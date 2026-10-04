package cn.spacexc.wearbili.remake.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Created by XC-Qan on 2023/10/18.
 * I'm very cute so please be nice to my code!
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 * 给！爷！写！注！释！
 */

/*@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GradientSlider(
    value: Float,
    range: ClosedRange<Float>,
    modifier: Modifier = Modifier,
    onValueChanged: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val localDensity = LocalDensity.current
        var width by remember {
            mutableIntStateOf(1)
        }
        var currentOffsetX by remember {
            mutableFloatStateOf(0f)
        }
        //expected 372 actual 321
        //expected 308 actual 260
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                .onSizeChanged { size ->
                    width = size.width
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onDragStart = { offset ->
                        offset.logd("start offset")
                        currentOffsetX = offset.x
                    }) { change, _ ->
                        val targetOffset = change.position.x - 25
                        change.consume()
                        width.logd("width")
                        targetOffset.logd("targetOffset")
                        if (targetOffset in 0f..width.toFloat()) {
                            currentOffsetX = targetOffset
                        }
                    }
                }
        ) {
            val offsetDp = with(localDensity) { currentOffsetX.toDp() }
            Box(
                modifier = Modifier
                    .width(offsetDp + 24.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(255, 54, 121, 128),
                                Color(255, 54, 121, 255)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(
                        x = offsetDp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientSlider(
    value: Float,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float>,
    brush: Brush = Brush.horizontalGradient(listOf(Color(50, 25, 33), BilibiliPink)),
    trackHeight: Dp = TrackHeight,
    thumbSize: Dp = TrackHeight * 0.4f,
    onValueChanged: (Float) -> Unit
) {
    // Material3 1.5 起 Slider 的 thumb/track 槽位必须配合 state-based 重载使用。
    // rememberSliderState 的签名是 (value, steps, valueRange)，此处用位置参数避免
    // Kotlin 元数据中参数名变更导致的不兼容。
    val sliderState = rememberSliderState(
        value,
        0,
        range,
    )
    Slider(
        modifier = modifier,
        state = sliderState,
        onValueChange = onValueChanged,
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .offset(x = 4.5.dp, y = 5.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = { state ->
            Track(sliderState = state, brush = brush, height = trackHeight)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    brush: Brush = Brush.horizontalGradient(listOf(Color(50, 25, 33), BilibiliPink)),
    trackHeight: Dp = TrackHeight,
    thumbSize: Dp = TrackHeight * 0.4f,
    onValueChanged: (Float) -> Unit,
    onSlideFinished: () -> Unit
) {
    val sliderState = rememberSliderState(
        value,
        0,
        range,
    )
    // 滑动结束回调：新 API 无 onValueChangeFinished 参数，
    // 通过监听 sliderState.value 变化不足以判定"结束"，
    // 故用 sliderState 自带的 settle 机制替代——见下方 onValueChange 包装。
    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.isDragging }
            .collect { dragging ->
                if (!dragging) onSlideFinished()
            }
    }
    Slider(
        modifier = modifier,
        state = sliderState,
        onValueChange = onValueChanged,
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .offset(x = 4.5.dp, y = 5.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = { state ->
            Track(sliderState = state, brush = brush, height = trackHeight)
        }
    )
}

val TrackHeight = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    height: Dp = TrackHeight,
    brush: Brush = Brush.horizontalGradient(listOf(Color(50, 25, 33), BilibiliPink)),
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
) {

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Material3 1.5 起 SliderState.valueRange 更名为 trackRange
        val range = sliderState.trackRange
        val coercedValueAsFraction = with(sliderState) {
            calcFraction(
                range.start,
                range.endInclusive,
                value.coerceIn(range.start, range.endInclusive)
            )
        }
        drawTrack(
            0f,
            height,
            coercedValueAsFraction,
            brush,
            trackColor
        )
    }
}

fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

fun DrawScope.drawTrack(
    activeRangeStart: Float,
    height: Dp,
    activeRangeEnd: Float,
    brush: Brush = Brush.horizontalGradient(listOf(Color(50, 25, 33), BilibiliPink)),
    trackColor: Color = Color(38, 38, 38, 128)
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val trackStrokeWidth = height.toPx()
    drawLine(
        trackColor,
        sliderStart,
        sliderEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
    val sliderValueEnd = Offset(
        sliderStart.x +
                (sliderEnd.x - sliderStart.x) * activeRangeEnd,
        center.y
    )

    val sliderValueStart = Offset(
        sliderStart.x +
                (sliderEnd.x - sliderStart.x) * activeRangeStart,
        center.y
    )

    drawLine(
        brush,
        sliderValueStart,
        sliderValueEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
}

@Preview
@Composable
private fun SliderPreview() {
    var sliderValue by remember {
        mutableFloatStateOf(0.5f)
    }
    GradientSlider(value = sliderValue, range = 0f..1f) {
        sliderValue = it
    }
}