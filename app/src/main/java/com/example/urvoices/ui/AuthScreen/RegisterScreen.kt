package com.example.urvoices.ui.AuthScreen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.AuthState
import com.example.urvoices.viewmodel.AuthViewModel


@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {

    Register(
        viewModel = authViewModel,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun Register(
    viewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val authState = viewModel.authState.observeAsState()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var retypePasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate(MainScreen.HomeScreen.route)
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ){paddingvalues ->
        Column(
            modifier = Modifier
                .padding(paddingvalues)
                .fillMaxWidth()

            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 100.dp, bottom = 50.dp)
            ){

                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = stringResource(id = R.string.signUp),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Column {
                TextField(
                    value = username,
                    onValueChange = {username = it},
                    label = { Text(text = stringResource(id = R.string.usernameRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value = email,
                    onValueChange = {email = it},
                    label = { Text(text = stringResource(id = R.string.emailRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value = password,
                    onValueChange = {password = it},
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
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                        ){
                            Icon(painter = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.size(400.dp, 60.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    value =  retypePassword,
                    onValueChange = {retypePassword = it},
                    label = { Text(text = stringResource(id = R.string.retypePasswordRegister)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    placeholder = {Text(text = "Re-type Password")},
                    visualTransformation = if(retypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (retypePasswordVisible)
                            painterResource(id = R.drawable.ic_visibility_on)
                        else painterResource(id = R.drawable.ic_visibility_off)

                        // Please provide localized description for accessibility services
                        val description = if (retypePasswordVisible) "Hide password" else "Show password"
                        IconButton(
                            onClick = {retypePasswordVisible = !retypePasswordVisible},
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                        ){
                            Icon(painter = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.size(400.dp, 60.dp)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    //Sign Up Email & Password
                    viewModel.signUpEmailPassword(email, password, username, retypePassword)
                },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(300.dp, 60.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.signUp),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                Text(
                    text = stringResource(id = R.string.orLogin),
                    modifier = Modifier.padding(top = 10.dp),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Card(
                        modifier = Modifier
                            .size(200.dp, 60.dp)
                            .clickable {
                                //TODO: Implement Google Sign In
                            },
                    ){
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.googleLogin),
                                modifier = Modifier.padding(start = 10.dp),
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .size(200.dp, 60.dp)
                            .clickable {
                                //TODO: Implement Facebook Login
                            },
                    ){
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = "Facebook",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.facebookLogin),
                                modifier = Modifier.padding(start = 10.dp),
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                }
            }


            Row(
                modifier = Modifier.clickable{
                    navController.navigate(AuthScreen.LoginScreen.route)
                }
            ) {
                Text(text = stringResource(id = R.string.haveaccount),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 5.dp)
                )
                Text(text = stringResource(id = R.string.signinLogin),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp)
                )
            }
        }
    }
}

fun validatePassword(password: String): Boolean {
    val passwordRegex = """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#${'$'}%^&+=])(?=\S+${'$'}).{8,}${'$'}""".toRegex()
    return passwordRegex.matches(password)
}