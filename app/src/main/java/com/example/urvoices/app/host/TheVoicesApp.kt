package com.example.urvoices.app.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Navigator.Navigator
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM

@Composable
fun TheVoicesApp(finishActivity: () -> Unit) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val mediaPlayerVM: MediaPlayerVM = hiltViewModel()
    MyTheme {
        Navigator(
            authViewModel = authViewModel,
            playerViewModel = mediaPlayerVM,
        )
    }
}