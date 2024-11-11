package com.example.urvoices.ui._component.waveform

import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment
import kotlin.random.Random

private val MinSpikeWidthDp: Dp = 1.dp
private val MaxSpikeWidthDp: Dp = 24.dp
private val MinSpikePaddingDp: Dp = 0.dp
private val MaxSpikePaddingDp: Dp = 12.dp
private val MinSpikeRadiusDp: Dp = 0.dp
private val MaxSpikeRadiusDp: Dp = 12.dp

private const val MinProgress: Float = 0F
private const val MaxProgress: Float = 1F

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    loading: MutableState<Boolean>,
    style: DrawStyle = Fill,
    waveformBrush: Brush = SolidColor(Color.White),
    progressBrush: Brush = SolidColor(Color.Blue),
    waveformAlignment: WaveformAlignment = WaveformAlignment.Center,
    amplitudeType: AmplitudeType = AmplitudeType.Avg,
    onProgressChangeFinished: (() -> Unit)? = null,
    spikeAnimationSpec: AnimationSpec<Float> = tween(500),
    spikeWidth: Dp = 4.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
    progress: Float = 0F,
    amplitudes: List<Int>,
    onProgressChange: (Float) -> Unit,
    onLoadingComplete: (() -> Unit)
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(Size.Zero) }

    val animatedLoading by animateFloatAsState(
        targetValue = if (loading.value) 0.5f else 1f,
        animationSpec = tween(500)
    )

    val _progress by rememberUpdatedState(progress.coerceIn(MinProgress, MaxProgress))
    val _spikeWidth by remember { mutableStateOf(spikeWidth.coerceIn(MinSpikeWidthDp, MaxSpikeWidthDp)) }
    val _spikePadding by remember { mutableStateOf(spikePadding.coerceIn(MinSpikePaddingDp, MaxSpikePaddingDp)) }
    val _spikeRadius by remember { mutableStateOf(spikeRadius.coerceIn(MinSpikeRadiusDp, MaxSpikeRadiusDp)) }

    val touchCallback = remember {
        object : TouchCallback {
            override fun onTouchEvent(event: MotionEvent, canvasSize: Size): Boolean {
                return when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        if (event.x in 0f..canvasSize.width) {
                            onProgressChange(event.x / canvasSize.width)
                            true
                        } else false
                    }
                    MotionEvent.ACTION_UP -> {
                        onProgressChangeFinished?.invoke()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    LaunchedEffect(amplitudes){
//        Log.e("AudioWaveform", "Amplitudes This Track: $amplitudes")
    }

    val spikes = with(density) {
        (size.width / (_spikeWidth + _spikePadding).toPx()).toInt()
    }

    val spikesAmplitudes by remember(amplitudes, spikes, amplitudeType, size.height) {
        derivedStateOf {
            amplitudes.toDrawableAmplitudes(
                amplitudeType = amplitudeType,
                spikes = spikes,
                minHeight = 1f,
                maxHeight = size.height
            )
        }
    }

    val animatedAmplitudes = spikesAmplitudes.map { amplitude ->
        animateFloatAsState(
            targetValue = amplitude,
            animationSpec = spikeAnimationSpec
        ).value
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { newSize ->
                size = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val progress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(progress)
                    onProgressChangeFinished?.invoke()
                }
            }
            .pointerInteropFilter { event ->
                touchCallback.onTouchEvent(event, size)
            }
            .graphicsLayer { this.alpha = animatedLoading }

    ) {
        val spikeWidthPx = with(density) { _spikeWidth.toPx() }
        val spikePaddingPx = with(density) { _spikePadding.toPx() }
        val spikeRadiusPx = with(density) { _spikeRadius.toPx() }

        animatedAmplitudes.forEachIndexed { index, amplitude ->
            val left = index * (spikeWidthPx + spikePaddingPx)
            val top = when (waveformAlignment) {
                WaveformAlignment.Top -> 0f
                WaveformAlignment.Bottom -> size.height - amplitude
                WaveformAlignment.Center -> size.height / 2f - amplitude / 2f
            }

            drawRoundRect(
                brush = if (left <= (_progress * size.width) ) progressBrush else waveformBrush,
                topLeft = Offset(left, top),
                size = Size(spikeWidthPx, amplitude),
                cornerRadius = CornerRadius(spikeRadiusPx, spikeRadiusPx)
            )
        }
        onLoadingComplete.invoke()
    }
}

interface TouchCallback {
    fun onTouchEvent(event: MotionEvent, canvasSize: Size): Boolean
}


private fun List<Int>.toDrawableAmplitudes(
    amplitudeType: AmplitudeType,
    spikes: Int,
    minHeight: Float,
    maxHeight: Float
): List<Float> {
    val amplitudes = map(Int::toFloat)
    if(amplitudes.isEmpty() || spikes == 0) {
        return List(spikes) { minHeight }
    }
    val transform = { data: List<Float> ->
        when(amplitudeType) {
            AmplitudeType.Avg -> data.average()
            AmplitudeType.Max -> data.max()
            AmplitudeType.Min -> data.min()
        }.toFloat().coerceIn(minHeight, maxHeight)
    }
    return when {
        spikes > amplitudes.count() -> amplitudes.fillToSize(spikes, transform)
        else -> amplitudes.chunkToSize(spikes, transform)
    }.normalize(minHeight, maxHeight)
}