package com.example.urvoices.app.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Navigator.Navigator
import com.example.urvoices.viewmodel.AppViewModel
import com.example.urvoices.viewmodel.AuthViewModel

@Composable
fun TheVoicesApp(finishActivity: () -> Unit) {
    val appViewModel: AppViewModel = viewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val appState = viewModel(modelClass = AppViewModel::class.java).appGlobalState.collectAsState()

    MyTheme {
        Navigator(authViewModel = authViewModel)
    }
}