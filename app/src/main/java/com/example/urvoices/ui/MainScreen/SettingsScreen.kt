package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.SettingComponents.SettingItem
import com.example.urvoices.ui._component.SettingComponents.SettingItemData
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    navController: NavController,
    playerViewModel: MediaPlayerVM
) {
    val TAG = "SettingsScreen"

    val constSettingList = listOf(
        SettingItemData(
            title = "Blocks",
            icon = R.drawable.ic_visibility_off,
            onClick = {
//                navController.navigate()
                //TODO navigate to Blocks
            }
        )
    )

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
        Column(
            modifier = Modifier.fillMaxSize().padding(it),
        ) {
            constSettingList.forEach { settingItem ->
                SettingItem(
                    title = settingItem.title,
                    icon = settingItem.icon,
                    switchState = settingItem.switchState,
                    onClick = settingItem.onClick
                )
            }
        }
    }
}


/*Button(onClick = {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            return@OnCompleteListener
        }

        // Get new FCM registration token
        val token = task.result

        // Log and toast
        Log.e(TAG, "Token: $token")
        Toast.makeText(
            navController.context,
            "Token: $token",
            Toast.LENGTH_SHORT
        ).show()
    })
}) {
    Text(text = "Test Noti")
}
*/

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    MyTheme {
        SettingsScreen(navController = rememberNavController(), playerViewModel = hiltViewModel())
    }
}