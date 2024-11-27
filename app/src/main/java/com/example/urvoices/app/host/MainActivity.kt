package com.example.urvoices.app.host

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.urvoices.utils.rememberImeState
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )

        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
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
        handleDeeplink(intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeeplink(intent)
    }

    private fun handleDeeplink(intent: Intent){
        val data: Uri? = intent.data
        if (data != null) {
            // Xử lý dữ liệu từ deeplink
            val pathSegment = data.pathSegments
            // Thực hiện điều gì đó với pathSegment
        }
    }
}