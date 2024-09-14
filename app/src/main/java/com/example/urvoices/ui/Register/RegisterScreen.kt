package com.example.urvoices.ui.Register

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.ViewModel.RegisterViewModel
import com.example.urvoices.ui._component.TopBarBackButton


@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = hiltViewModel()
    Register(
        viewModel = viewModel,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun Register(
    viewModel: RegisterViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
){

    var passwordVisible by remember { mutableStateOf(false) }
    var retypePasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarBackButton(navController = navController)
        },
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
                    value = viewModel.retypePassword,
                    onValueChange = {viewModel.retypePassword = it},
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


            Row {
                Text(text = stringResource(id = R.string.haveaccount),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 5.dp)
                )
                Text(text = stringResource(id = R.string.signinLogin),
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
