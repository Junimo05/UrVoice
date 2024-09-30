package com.example.urvoices.ui._component.PostComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.urvoices.utils.waveform.AudioWaveform


@Composable
fun TestWaveForm() {
    var stateList = rememberLazyListState()

    val amplitudes = remember {
        (0 until 100).map { it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = stateList,
        userScrollEnabled = true
    ) {
        items(20){
            AudioWaveformItem(percentPlayed = 0.5f, isPlaying = true, duration = "4.12")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TestWaveFormPreview() {
    TestWaveForm()
}