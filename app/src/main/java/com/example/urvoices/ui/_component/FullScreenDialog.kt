package com.example.urvoices.ui._component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreenDialog(text: String, onDismiss: () -> Unit) {
	Dialog(onDismissRequest = onDismiss,
		properties = DialogProperties(
			usePlatformDefaultWidth = true
		)
	) {
		Card(
			modifier = Modifier
				.clickable { /* Do nothing to prevent dismiss */ },
			colors = CardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
				disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
				disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
			)
		) {
			Text(
				text = text,
				modifier = Modifier.padding(16.dp),
				style = MaterialTheme.typography.bodyLarge
			)
		}
	}
}
