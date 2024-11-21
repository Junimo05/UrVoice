package com.example.urvoices.ui._component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.viewmodel.UploadState

@Composable
fun CustomSnackBar(
	data: SnackbarData,
	state: State<UploadState?>,
){
	val icon: @Composable (() -> Unit)? = when(state.value){
		is UploadState.Loading -> {
			@Composable { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
		}
		is UploadState.Success -> {
			@Composable { Icon(painter = painterResource(id = R.drawable.check_circle_svgrepo_com), contentDescription = "checked", modifier = Modifier.size(24.dp)) }
		}
		is UploadState.Error -> {
			@Composable { Icon(painter = painterResource(id = R.drawable.error_16_svgrepo_com), contentDescription = "checked", modifier = Modifier.size(24.dp)) }
		}
		else -> null
	}

	Snackbar(
		modifier = Modifier.padding(16.dp),
		containerColor = MaterialTheme.colorScheme.inverseSurface,
		action = { data.visuals.actionLabel?.let { actionLabel ->
			TextButton(onClick = { data.performAction() }) { Text(actionLabel) } }
        },
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically

		){
			//Change icon based on upload state
			icon?.invoke()
			Spacer(modifier = Modifier.width(2.dp))
			Text(
				text = data.visuals.message,
				style = TextStyle(
					color = MaterialTheme.colorScheme.inverseOnSurface,
					fontSize = MaterialTheme.typography.labelSmall.fontSize
				),
				modifier = Modifier.padding(start = 8.dp)
			)
		}
	}
}