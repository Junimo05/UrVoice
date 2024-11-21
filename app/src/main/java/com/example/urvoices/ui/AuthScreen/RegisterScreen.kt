package com.example.urvoices.ui.AuthScreen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.urvoices.utils.Auth.GoogleSignIn
import com.example.urvoices.utils.Auth.isPasswordStrong
import com.example.urvoices.utils.Auth.isValidEmail
import com.example.urvoices.utils.Auth.isValidUsername
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.AuthState
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.SignupState
import kotlinx.coroutines.delay
import org.w3c.dom.Text


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
    val coroutineScope = rememberCoroutineScope()
    val authState = viewModel.authState.observeAsState()
    val signUpState = viewModel.signUpState.observeAsState()

    val signUp = mutableStateOf(false)
    val sentEmail = mutableStateOf(false)
    val emailVerified = viewModel.emailVerificationStatus.observeAsState()

    var username by remember { mutableStateOf("") }
    var validateUsername by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var validateEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var validatePassword by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var isMatchPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var retypePasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()


    LaunchedEffect(authState.value) {
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

    LaunchedEffect(signUpState.value){
        when(signUpState.value){
            is SignupState.Error -> {
                Toast.makeText(context, (signUpState.value as SignupState.Error).message, Toast.LENGTH_SHORT).show()
                signUp.value = false
                sentEmail.value = false
            }
            SignupState.Loading -> {
                //
            }
            SignupState.SendEmail -> {
                Toast.makeText(context, "Email Verification Sent", Toast.LENGTH_SHORT).show()
                delay(2000)
                sentEmail.value = true

            }
            SignupState.SignUp -> {
                signUp.value = true
                Toast.makeText(context, "Signing Up...", Toast.LENGTH_SHORT).show()
                delay(2000)
                viewModel.sendEmailVerification()
            }
            SignupState.SuccessAuth -> {
                Toast.makeText(context, "Email Verified... We are going to create your Portal...", Toast.LENGTH_SHORT).show()
                delay(2000)
                if(emailVerified.value!!){
                    viewModel.createInfo(username)
                }
            }
            SignupState.Complete -> {
                Toast.makeText(context, "Portal are created. Here we go", Toast.LENGTH_SHORT).show()
                signUp.value = false
                sentEmail.value = false
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ){paddingvalues ->
        if(!sentEmail.value){
            Column(
                modifier = Modifier
                    .padding(paddingvalues)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,

                ) {
                Column(
                    modifier = Modifier
                        .padding(top = 80.dp, bottom = 50.dp)
                ){
                    Spacer(modifier = Modifier.padding(10.dp))
                    Text(
                        text = stringResource(id = R.string.signUp),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                ) {
                    Card {
                        TextField(
                            value = username,
                            onValueChange = {
                                username = it
                                validateUsername = if(it.isNotEmpty()){
                                    if(isValidUsername(it)) "" else "Username is not valid"
                                } else {
                                    "Username is required"
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.usernameRegister)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if(validateUsername.isNotEmpty()){
                        NotValid(message = validateUsername)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card{
                        TextField(
                            value = email,
                            onValueChange = {
                                email = it
                                validateEmail = if(it.isNotEmpty()){
                                    if(isValidEmail(it)) "" else "Email is not valid"
                                } else {
                                    "Email is required"
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.emailRegister)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if(validateEmail.isNotEmpty()){
                        NotValid(message = validateEmail)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card {
                        TextField(
                            value = password,
                            onValueChange = {
                                password = it
                                validatePassword = if(it.isNotEmpty()){
                                    if(isPasswordStrong(it)) "" else "Password is not strong"
                                } else {
                                    "Password is required"
                                }
                            },
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
                            interactionSource = interactionSource,
                            modifier = Modifier.fillMaxWidth(),

                            )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if(validatePassword.isNotEmpty() && !isFocused){
                        NotValid(message = validatePassword)
                    } else {
                        if(isFocused && password.isEmpty()) {
                            Text(
                                text = "Password must be at least 8 characters, include an uppercase letter, a number, and a special character (!@#$%&*).",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                ),
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Card{
                        TextField(
                            value =  retypePassword,
                            onValueChange = {
                                retypePassword = it
                                isMatchPassword = if(it.isNotEmpty()){
                                    if(it == password) "" else "Password does not match"
                                } else {
                                    "Re-type Password is required"
                                }
                            },
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        //Sign Up Email & Password
                        if (validateUsername.isNotEmpty()) {
                            Toast.makeText(context, validateUsername, Toast.LENGTH_SHORT).show()
                        } else if (validateEmail.isNotEmpty()) {
                            Toast.makeText(context, validateEmail, Toast.LENGTH_SHORT).show()
                        } else if (validatePassword.isNotEmpty()) {
                            Toast.makeText(context, validatePassword, Toast.LENGTH_SHORT).show()
                        } else if (isMatchPassword.isNotEmpty()) {
                            Toast.makeText(context, isMatchPassword, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.signUpEmailPassword(email, password)
                        }
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
                                    GoogleSignIn(context, coroutineScope) { credential ->
                                        viewModel.signInWithGoogle(credential)
                                    }
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
                        /*Card(
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
						}*/
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
        } else {
            EmailSentCheck(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingvalues)
            )
        }
        if(signUpState.value == SignupState.Loading){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun EmailSentCheck(
    navController: NavController,
    viewModel: AuthViewModel,
    modifier: Modifier
){
    val signupState = viewModel.signUpState.observeAsState()
    val emailVerified = viewModel.emailVerificationStatus.observeAsState()

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.email_open_phone_svgrepo_com),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Please check your email to verify your account!",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            onClick = {
                viewModel.checkVerifyEmail()
            },
            colors = CardColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(300.dp, 60.dp)
        ){
            Text(
                text = "Already Verified? Click Here!",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if(emailVerified.value != null){
            if(emailVerified.value!!){
                Text(
                    text = "Email Verified!",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Text(
                    text = "Email Not Verified! Please Verify Your Email",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                TextButton(onClick = {
                    viewModel.sendEmailVerification()
                }) {
                    Text(
                        text = "Resend Email Verification",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NotValid(message: String){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = painterResource(id = R.drawable.error_16_svgrepo_com),
            contentDescription ="",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}
