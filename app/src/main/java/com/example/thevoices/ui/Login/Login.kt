package com.example.thevoices.ui.Login

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thevoices.R
import com.example.thevoices.ViewModel.LoginViewModel
import com.example.thevoices.utils.Navigator.AuthScreen
import com.example.thevoices.utils.Navigator.MainScreen

@Composable
fun LoginScreen(
    navController: NavController
){
    val viewModel: LoginViewModel = hiltViewModel()
    Login(
        navController = navController,
        username = viewModel.username,
        onUsernameChange = { viewModel.onUsernameChange(it)},
        password = viewModel.password,
        onPasswordChange = { viewModel.onPasswordChange(it)},
        onLoginClicked = { viewModel.onLoginClicked() }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Login(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClicked: () -> Boolean,
    navController: NavController? = null,
    modifier: Modifier = Modifier
){

    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {

        }
    ) { paddingvalues ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(paddingvalues),
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 200.dp)
                ){

                    Spacer(modifier = Modifier.padding(10.dp))
                    Image(
                        painter = painterResource(id = R.drawable.jerry2),
                        modifier = Modifier.size(100.dp),
                        contentDescription = null
                    )
                    Text(text = "Sign in",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Column {
                    TextField(
                        value = username,
                        onValueChange = {onUsernameChange(it)},
                        label = { Text(text = stringResource(id = R.string.usernameLogin)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier.size(400.dp, 60.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    TextField(
                        value = password,
                        onValueChange = {onPasswordChange(it)},
                        label = { Text(text = stringResource(id = R.string.passwordLogin)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        placeholder = {Text(text = "Password")},
                        visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                painterResource(id = R.drawable.ic_visibility_on)
                            else painterResource(id = R.drawable.ic_visibility_off)

                            // Please provide localized description for accessibility services
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(
                                onClick = {passwordVisible = !passwordVisible},
                                modifier = Modifier.size(24.dp).padding(4.dp)
                            ){
                                Icon(painter = image, contentDescription = description)
                            }
                        },
                        modifier = Modifier.size(400.dp, 60.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(id = R.string.forgotpasswordLogin),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 10.dp)
                            .clickable(
                                onClick = {
//                                    navController?.navigate(AuthScreen.ForgotPasswordScreen.route)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.padding(20.dp))
                Button(onClick = {
                    if(onLoginClicked()){
                        navController?.navigate(MainScreen.HomeScreen.route)
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(300.dp, 60.dp)
                ) {
                    Text(text = stringResource(id = R.string.loginLogin))
                }

                Row {
                    Text(text = stringResource(id = R.string.donthaveaccount),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 5.dp)
                    )
                    Text(text = stringResource(id = R.string.register),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 5.dp)
                            .clickable {
                                navController?.navigate(AuthScreen.RegisterScreen.route)
                            }
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LoginPreview(){
    val loginViewModel: LoginViewModel = viewModel()
    Login(
        username = loginViewModel.username,
        onUsernameChange = { loginViewModel.onUsernameChange(it)},
        password = loginViewModel.password,
        onPasswordChange = { loginViewModel.onPasswordChange(it)},
        onLoginClicked = { loginViewModel.onLoginClicked() }
    )
}