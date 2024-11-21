package com.example.urvoices.ui._component.waveform

import android.view.MotionEvent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.linc.audiowaveform.model.AmplitudeType

private val MinSpikeWidthDp: Dp = 1.dp
private val MaxSpikeWidthDp: Dp = 24.dp
private val MinSpikePaddingDp: Dp = 0.dp
private val MaxSpikePaddingDp: Dp = 12.dp
private val MinSpikeRadiusDp: Dp = 0.dp
private val MaxSpikeRadiusDp: Dp = 12.dp


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioWaveformLive(
    modifier: Modifier = Modifier,
    waveformBrush: Brush = SolidColor(Color.White),
    spikeWidth: Dp = 4.dp,
    spikeRadius: Dp = 2.dp,
    spikePadding: Dp = 1.dp,
    maxColumn: Int,
    amplitudesLiveData: List<Int>,
    onProgressChange: (Float) -> Unit
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(Size.Zero) }
    val maxAmplitude = 20000

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
                    else -> false
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .height(240.dp)
            .fillMaxWidth()
            .onSizeChanged { newSize ->
                size = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val progress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(progress)
                }
            }
            .pointerInteropFilter { event ->
                touchCallback.onTouchEvent(event, size)
            }
            .border(1.dp, Color.Gray),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val spikeWidthPx = with(density) { _spikeWidth.toPx() }
        val spikePaddingPx = with(density) { _spikePadding.toPx() }
        val spikeRadiusPx = with(density) { _spikeRadius.toPx() }

        val totalWidth = maxColumn * (spikeWidthPx + spikePaddingPx) - spikePaddingPx

        // Adjust spike width and padding to fit within the canvas width
        val adjustedSpikeWidthPx = if (totalWidth > canvasWidth) {
            (canvasWidth - (maxColumn - 1) * spikePaddingPx) / maxColumn
        } else {
            spikeWidthPx
        }

        amplitudesLiveData.forEachIndexed { index, amplitude ->
            val left = index * (adjustedSpikeWidthPx + spikePaddingPx)
            val amplitudePercent = (amplitude.toFloat() / maxAmplitude).coerceIn(0f, 1f)
            val spikeHeight = amplitudePercent * canvasHeight
            val top = canvasHeight / 2f - spikeHeight / 2f

            // Ensure the spike stays within the canvas boundaries


            drawRoundRect(
                brush = waveformBrush,
                topLeft = Offset(x = left, y = top),
                size = Size(adjustedSpikeWidthPx, spikeHeight),
                cornerRadius = CornerRadius(spikeRadiusPx, spikeRadiusPx)
            )
        }
    }
}