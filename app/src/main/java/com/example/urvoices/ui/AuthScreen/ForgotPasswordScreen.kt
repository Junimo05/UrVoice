package com.example.urvoices.ui.AuthScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("PrivateResource")
@Composable
fun ForgotPasswordScreen(
	navController: NavController,
	authViewModel: AuthViewModel,
) {
	val scope = rememberCoroutineScope()
	var cooldown by remember { mutableStateOf(0) }
	var email by remember { mutableStateOf(TextFieldValue("")) }
	var message by remember { mutableStateOf("") }
	val isSuccess = remember {
		mutableStateOf(false)
	}

	if(!isSuccess.value){
		Column(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			// Logo
			Image(
				painter = painterResource(id = androidx.credentials.R.drawable.ic_password),
				contentDescription = "Logo",
				modifier = Modifier.size(100.dp)
			)

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				text = "Please enter your email to reset your password",
				style = TextStyle(
					color = MaterialTheme.colorScheme.onBackground,
					fontSize = 16.sp,
					fontWeight = FontWeight.Bold
				)
			)

			// Email input field
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			){
				BasicTextField(
					value = email,
					onValueChange = { email = it },
					textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp),
					modifier = Modifier
						.weight(1f)
						.padding(16.dp)
						.background(
							MaterialTheme.colorScheme.primaryContainer,
							shape = MaterialTheme.shapes.small
						)
						.padding(16.dp),
					singleLine = true
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Button to send reset email
				Button(
					onClick = {
						authViewModel.sendPasswordResetEmail(
							email.text,
							onSuccess = {
								message = "Email sent successfully"
								cooldown = 20 // Set cooldown
								scope.launch {
									while (cooldown > 0) {
										delay(1000L)
										cooldown--
									}
								}
								isSuccess.value = true
							},
							onFailure = {
								message = "Something went wrong, please try again later"
							}
						)
					},
					enabled = cooldown == 0
				) {
					Text(if (cooldown == 0) "Send" else "Wait $cooldown s")
				}
			}
			Spacer(modifier = Modifier.height(16.dp))
			// Message
			Text(
				text = message,
				color = MaterialTheme.colorScheme.error,
				fontSize = 14.sp
			)
			Spacer(modifier = Modifier.height(50.dp))
			TextButton(onClick = { navController.popBackStack() }) {
				Row(
					verticalAlignment = Alignment.CenterVertically

				){
					Icon(painter = painterResource(id = R.drawable.back_square_svgrepo_com), contentDescription = "", modifier = Modifier.size(24.dp))
					Text(text = "Back to login",
						style = TextStyle(
							color = MaterialTheme.colorScheme.onBackground,
							fontSize = 16.sp,
							fontWeight = FontWeight.Bold
						)
					)
				}
			}
		}
	} else {
		SuccessScreen(
			navController = navController,
			authViewModel = authViewModel,
			isSuccess = isSuccess
		)
	}

}

@Composable
fun SuccessScreen(
	isSuccess: MutableState<Boolean>,
	navController: NavController,
	authViewModel: AuthViewModel
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(16.dp),
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
			text = "Please check your email to reset your password!",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onBackground,
				fontSize = 16.sp,
				fontWeight = FontWeight.Bold
			)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Card(
			onClick = {
				navController.navigate(AuthScreen.LoginScreen.route)
			},
			colors = CardColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
				disabledContentColor = Color.Gray,
				disabledContainerColor = Color.Gray
			)
		) {
			Text(
				text = "Back to Sign in",
				style = TextStyle(
					color = MaterialTheme.colorScheme.onPrimary,
					fontSize = 16.sp,
					fontWeight = FontWeight.Bold
				),
				modifier = Modifier.padding(16.dp)
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		TextButton(
			onClick = {
				isSuccess.value = false
			}
		) {
			Text("Back to Forgot Password")
		}
	}
}