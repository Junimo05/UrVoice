package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.ui._component.SettingComponents.SettingItem
import com.example.urvoices.ui._component.SettingComponents.SettingItemData
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.AuthState
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.SettingViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    navController: NavController,
    playerViewModel: MediaPlayerVM,
    authViewModel: AuthViewModel,
    settingVM: SettingViewModel
) {
    val TAG = "SettingsScreen"
    val scope = rememberCoroutineScope()
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(Unit) {
        settingVM.getSetting()
    }

    val constSettingList = listOf(
        SettingItemData(
            title = "Security",
            icon = R.drawable.cogs_svgrepo_com,
            onClick = {
                navController.navigate(MainScreen.SettingsScreen.SecurityScreen.route)
            }
        ),
        SettingItemData(
            title = "Blocks",
            icon = R.drawable.ic_visibility_off,
            onClick = {
                navController.navigate(MainScreen.SettingsScreen.BlockedUsersScreen.route)
            }
        ),
        SettingItemData(
            title = "Deleted Posts",
            icon = R.drawable.trash_03_svgrepo_com,
            onClick = {
                navController.navigate(MainScreen.SettingsScreen.DeletePostScreen.route)
            }
        ),
        SettingItemData(
            title = "Saved Posts",
            icon = R.drawable.newspaper_svgrepo_com,
            onClick = {
                navController.navigate(MainScreen.SettingsScreen.SavedPostsScreen.route)
            }
        ),
        SettingItemData(
            title = "Share Saved Posts",
            icon = R.drawable.paper_roll_svgrepo_com,
            switchState = settingVM.isShareLoving,
            onSwitch = {
                settingVM.shareLovingChange(it)
            }
        ),
        SettingItemData(
            title = "Private Account",
            icon = R.drawable.globe_private_svgrepo_com,
            switchState = settingVM.isPrivate,
            onSwitch = {
                settingVM.privateAccountChange(it)
            }
        ),
//        SettingItemData(
//            title = "Delete Account",
//            icon = R.drawable.ic_emoji_crying,
//            onClick = {
//                navController.navigate(MainScreen.SettingsScreen.DeleteAccount.route)
//            }
//        ),
        SettingItemData(
            title = "Sign out",
            icon = R.drawable.ic_actions_log_out,
            onClick = {
                scope.launch {
                    authViewModel.signOut()
                }
            }
        )
    )

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> {
                navController.navigate(AuthScreen.LoginScreen.route)
            }
            else -> Unit
        }
    }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            constSettingList.forEach { settingItem ->
                SettingItem(
                    title = settingItem.title,
                    icon = settingItem.icon,
                    switchState = settingItem.switchState,
                    onSwitch = settingItem.onSwitch,
                    onClick = settingItem.onClick
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
