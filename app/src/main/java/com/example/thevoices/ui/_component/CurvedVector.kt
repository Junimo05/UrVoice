package com.example.thevoices.ui._component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CurvedVector() {
    val color = Color.Red

    Canvas(modifier = Modifier.size(200.dp)) {
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset.Zero,
            size = this.size,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurvedVectorPreview() {
    CurvedVector()
}