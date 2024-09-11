package com.example.thevoices.ui.Register

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.thevoices.R
import com.example.thevoices.ViewModel.RegisterViewModel


@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = hiltViewModel()
    Register(
        viewModel = viewModel,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Register(
    viewModel: RegisterViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    Scaffold(
        topBar = {

        },
        modifier = Modifier.fillMaxSize(),
    ){paddingvalues ->
        Box(
            modifier = Modifier
                .padding(paddingvalues)
                .clip(CircleShape)
                .clickable {

                }
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 150.dp)
            ){

                Spacer(modifier = Modifier.padding(10.dp))
                Image(
                    painter = painterResource(id = R.drawable.jerry2),
                    modifier = Modifier.size(100.dp),
                    contentDescription = null
                )
                Text(text = stringResource(id = R.string.registerRegister),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Column {
                TextField(
                    value = viewModel.username,
                    onValueChange = {viewModel.username = it},
                    label = { Text(text = stringResource(id = R.string.usernameRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value = viewModel.email,
                    onValueChange = {viewModel.email = it},
                    label = { Text(text = stringResource(id = R.string.emailRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value = viewModel.password,
                    onValueChange = {viewModel.password = it},
                    label = { Text(text = stringResource(id = R.string.passwordRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value = viewModel.retypePassword,
                    onValueChange = {viewModel.retypePassword = it},
                    label = { Text(text = stringResource(id = R.string.retypePasswordRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Button(onClick = {
//                if (viewModel.onLoginClicked()) {
//                    navController?.navigate("home")
//                }
            },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(300.dp, 60.dp)
            ) {
                Text(text = stringResource(id = R.string.registerRegister))
            }

            Row {
                Text(text = stringResource(id = R.string.haveaccount),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 5.dp)
                )
                Text(text = stringResource(id = R.string.loginLogin),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp)
                        .clickable {
                            navController.navigate("login")
                        }
                )
            }
        }
    }
}
