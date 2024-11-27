package com.example.urvoices.ui._component.SettingComponents

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.ui._component.PasswordField
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.Auth.isValidEmail
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.SettingViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SecurityScreen(
	navController: NavController,
	authViewModel: AuthViewModel,
	settingVM: SettingViewModel
) {
	val scope = rememberCoroutineScope()
	val dialogSuccess = remember { mutableStateOf(false) }
	var screen by remember {
		mutableStateOf(SecurityScreenRoute.MAIN_SECURITY_SCREEN)
	}
	val screenState = remember { MutableTransitionState(screen == SecurityScreenRoute.MAIN_SECURITY_SCREEN) }

	val listSecurity = listOf(
		SettingItemData(
			title = "Change Email",
			icon = R.drawable.envelope_svgrepo_com,
			onClick = {
				screen = SecurityScreenRoute.CHANGE_EMAIL_SCREEN
			}
		),
		SettingItemData(
			title = "Change Password",
			icon = R.drawable.lock_svgrepo_com,
			onClick = {
				screen = SecurityScreenRoute.CHANGE_PASSWORD_SCREEN
			}
		)
	)

	Scaffold(
		topBar = {
			TopBarBackButton(navController = navController, title = "Security", backButtonAction = {
				if (screen == SecurityScreenRoute.MAIN_SECURITY_SCREEN) {
					navController.popBackStack()
				} else {
					screen = SecurityScreenRoute.MAIN_SECURITY_SCREEN
				}
			})
		}
	) {
		if(dialogSuccess.value){
			Dialog(
				onDismissRequest = {
					scope.launch {
						authViewModel.signOut()
						navController.navigate(MainScreen.HomeScreen.route)
					}

				}
			) {
				Column(
					modifier = Modifier.padding(20.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Text(
						text = "Email Changed",
						style = TextStyle(
							color = MaterialTheme.colorScheme.onPrimaryContainer,
							fontSize = 20.sp,
							fontWeight = FontWeight.Bold
						)
					)
					Spacer(modifier = Modifier.height(20.dp))
					Text(
						text = "Your email has been changed successfully. Please login again to continue.",
						style = TextStyle(
							color = MaterialTheme.colorScheme.onPrimaryContainer,
							fontSize = 16.sp
						)
					)
					Spacer(modifier = Modifier.height(20.dp))
					Button(onClick = {
						scope.launch {
							authViewModel.signOut()
							navController.navigate(MainScreen.HomeScreen.route)
						}
					}) {
						Text(text = "Login")
					}
				}
			}
		}

		when(screen){
			SecurityScreenRoute.MAIN_SECURITY_SCREEN -> {
				screenState.targetState = true
				AnimatedVisibility(
					visibleState = screenState,
					enter = fadeIn(),
					exit = fadeOut()
				) {
					SettingScreen(
						listSetting = listSecurity,
						modifier = Modifier.padding(it)
					)
				}
			}
			SecurityScreenRoute.CHANGE_EMAIL_SCREEN -> {
				screenState.targetState = true
				AnimatedVisibility(
					visibleState = screenState,
					enter = fadeIn(),
					exit = fadeOut()
				) {
					ChangeEmailScreen(
						navController = navController,
						authViewModel = authViewModel,
						onAction = {
							authViewModel.resetEmailState()
							//Display Dialog request user to login again
							dialogSuccess.value = true
						},
						modifier = Modifier.padding(it)
					)
				}
			}
			SecurityScreenRoute.CHANGE_PASSWORD_SCREEN -> {
				screenState.targetState = true
				AnimatedVisibility(
					visibleState = screenState,
					enter = fadeIn(),
					exit = fadeOut()
				) {
					ChangePasswordScreen(
						navController = navController,
						authViewModel = authViewModel,
						onAction = {

						},
						modifier = Modifier.padding(it)
					)
				}
			}
		}
	}

}

@Composable
fun SettingScreen(
	listSetting: List<SettingItemData>,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.background)
			.fillMaxSize()
			.then(modifier),
	) {
		listSetting.forEach { settingItem ->
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

@Composable
fun ChangeEmailScreen(
	navController: NavController,
	authViewModel: AuthViewModel,
	onAction: () -> Unit,
	modifier: Modifier = Modifier
) {
	val TAG = "ChangeEmailScreen"
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val newEmail = remember { mutableStateOf("") }
	val password = remember { mutableStateOf("") }
	val retypePass = remember { mutableStateOf("") }
	val currentUser = authViewModel.getCurrentUser()

	val isVerified = authViewModel.emailVerificationStatus.observeAsState()
	val emailSent = authViewModel.emailSent.observeAsState()

	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.background)
			.fillMaxSize()
			.then(modifier),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			text = "Change Email",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				fontSize = 26.sp,
				fontWeight = FontWeight.Bold
			),
		)
		Spacer(modifier = Modifier.height(20.dp))
		Text(
			text = "Enter your new email address below and we'll send you a confirmation email.",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				fontSize = 20.sp
			),
			modifier = Modifier.padding(20.dp)
		)
		TextField(
			value = newEmail.value,
			onValueChange = {
				newEmail.value = it
			},
			label = { Text("New Email") },
			maxLines = 1,
		)
		if(!isValidEmail(newEmail.value) && newEmail.value.isNotEmpty()){
			Text(
				text = "Invalid Email",
				style = TextStyle(
					color = MaterialTheme.colorScheme.error,
					fontSize = 14.sp
				),
				modifier = Modifier.padding(4.dp)
			)
		}
		Spacer(modifier = Modifier.height(20.dp))
		PasswordField(
			label = "Password",
			passwordValue = password,
			modifier = Modifier.padding(bottom = 5.dp)
		)
		Spacer(modifier = Modifier.height(2.dp))
		PasswordField(
			label = "Retype Password",
			passwordValue = retypePass,
			modifier = Modifier.padding(bottom = 5.dp)
		)
		Button(onClick = {
			if (currentUser != null) {
				if (emailSent.value == true) {
					Log.e(TAG, "Verifying email")
					authViewModel.checkVerifyEmail()
					if (isVerified.value == true) {
						Toast.makeText(context, "Email changed successfully", Toast.LENGTH_SHORT).show()
						onAction()
					} else {
						Toast.makeText(context, "Failed to change email. Please verify your email", Toast.LENGTH_SHORT).show()
					}
				} else {
					if (password.value == retypePass.value) {
						authViewModel.reAuthenticateUser(
							currentUser.email!!,
							password.value,
							onSuccess = {
								Log.e(TAG, "Re-authenticated")
								Log.e(TAG, "Email Sent: ${emailSent.value}")
								if (emailSent.value == null || emailSent.value == false) {
									Log.e(TAG, "Sending email")
									authViewModel.updateEmail(email = newEmail.value)
								}
							},
							onFailure = { errorMessage ->
								Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
								if (errorMessage.contains("Please sign in again")) {
									scope.launch {
										authViewModel.signOut()
										navController.navigate(MainScreen.HomeScreen.route)
									}
								}
							}
						)
					} else {
						Toast.makeText(context, "Password doesn't match", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}) {
			Text(
				text = if (emailSent.value == null) {
					"Change Email"
				} else {
					if (emailSent.value == true) {
						"Already Verified? Click here"
					} else {
						"Resend Email"
					}
				}
			)
		}
	}
}

@Composable
fun ChangePasswordScreen(
	navController: NavController,
	authViewModel: AuthViewModel,
	onAction: () -> Unit,
	modifier: Modifier = Modifier
) {
	val TAG = "ChangePasswordScreen"
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val oldPassword = remember { mutableStateOf("") }
	val newPassword = remember { mutableStateOf("") }
	val retypePass = remember { mutableStateOf("") }
	val currentUser = authViewModel.getCurrentUser()

	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.background)
			.fillMaxSize()
			.then(modifier),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			text = "Change Password",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				fontSize = 26.sp,
				fontWeight = FontWeight.Bold
			),
		)
		Spacer(modifier = Modifier.height(20.dp))
		PasswordField(
			label = "Old Password",
			passwordValue = oldPassword,
			modifier = Modifier.padding(bottom = 5.dp)
		)
		Spacer(modifier = Modifier.height(2.dp))
		PasswordField(
			label = "New Password",
			passwordValue = newPassword,
			modifier = Modifier.padding(bottom = 5.dp)
		)
		Spacer(modifier = Modifier.height(2.dp))
		PasswordField(
			label = "Retype Password",
			passwordValue = retypePass,
			modifier = Modifier.padding(bottom = 5.dp)
		)
		Button(onClick = {
			if (currentUser != null) {
				if (newPassword.value == retypePass.value) {
					authViewModel.reAuthenticateUser(
						currentUser.email!!,
						oldPassword.value,
						onSuccess = {
							Log.e(TAG, "Re-authenticated")
							authViewModel.updatePassword(newPassword.value,
								onSuccess = {
									navController.navigate(MainScreen.SettingsScreen.MainSettingsScreen.route)
								},
								onFailure = { errorMessage ->
									Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
								})
							Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
						},
						onFailure = { errorMessage ->
							Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
							if (errorMessage.contains("Please sign in again")) {
								scope.launch {
									authViewModel.signOut()
									navController.navigate(MainScreen.HomeScreen.route)
								}
							}
						}
					)
				} else {
					Toast.makeText(context, "Password doesn't match", Toast.LENGTH_SHORT).show()
				}
			}
		}){
			Text(text = "Change Password")
		}
	}
}

object SecurityScreenRoute {
	val MAIN_SECURITY_SCREEN = "MainSecurityScreen"
	val CHANGE_EMAIL_SCREEN = "ChangeEmailScreen"
	val CHANGE_PASSWORD_SCREEN = "ChangePasswordScreen"
}