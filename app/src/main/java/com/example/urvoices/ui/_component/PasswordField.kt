package com.example.urvoices.ui._component

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.utils.Auth.isPasswordStrong

@SuppressLint("UnrememberedMutableState")
@Composable
fun PasswordField(
	modifier: Modifier = Modifier,
	label: String,
	passwordValue: MutableState<String>,
	validateEnable: Boolean = false,
){
	var passwordVisible by rememberSaveable { mutableStateOf(false) }
	TextField(
		value = passwordValue.value,
		onValueChange = {value ->
			passwordValue.value = value
		},
		maxLines = 1,
		isError = validateEnable && !isPasswordStrong(passwordValue.value),
		label = { Text(text = label) },
		colors = TextFieldDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
			unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
		),
		placeholder = { Text(text = "Password") },
		visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
		keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
		trailingIcon = {
			val image = if (passwordVisible)
				painterResource(id = R.drawable.ic_visibility_on)
			else painterResource(id = R.drawable.ic_visibility_off)

			// Please provide localized description for accessibility services
			val description = if (passwordVisible) "Hide password" else "Show password"
			IconButton(
				onClick = {
//					Log.e("PasswordField", "passwordVisible: ${passwordVisible}")
					passwordVisible = !passwordVisible
//					Log.e("PasswordField", "passwordVisible: ${passwordVisible}")
			    },
				modifier = Modifier
					.size(24.dp)
					.padding(4.dp)
			){
				Icon(painter = image, contentDescription = description)
			}
		},
		modifier = Modifier.then(modifier)
	)
}