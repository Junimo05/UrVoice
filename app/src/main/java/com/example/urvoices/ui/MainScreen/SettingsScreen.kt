package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urvoices.presentations.theme.MyTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Text(
                    text = "Settings",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 35.dp)
                )
            }
        }
    ) {

    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    MyTheme {
        SettingsScreen()
    }
}