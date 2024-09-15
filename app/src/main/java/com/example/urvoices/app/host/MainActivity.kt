package com.example.urvoices.app.host

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import com.example.urvoices.utils.rememberImeState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val imeState = rememberImeState()
            val scrollState = rememberScrollState()
            
            LaunchedEffect(key1 = imeState.value) {
                if(imeState.value) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
            TheVoicesApp {
                finish()
            }
        }
    }
}