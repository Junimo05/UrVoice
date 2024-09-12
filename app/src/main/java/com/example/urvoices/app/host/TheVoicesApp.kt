package com.example.urvoices.app.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urvoices.ViewModel.AppViewModel
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Navigator.Navigator

@Composable
fun TheVoicesApp(finishActivity: () -> Unit) {
    val appViewModel: AppViewModel = viewModel()
    val appState = viewModel(modelClass = AppViewModel::class.java).appGlobalState.collectAsState()

    MyTheme {
        Navigator()
    }
}