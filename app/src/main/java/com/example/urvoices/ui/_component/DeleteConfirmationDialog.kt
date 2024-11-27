package com.example.urvoices.ui._component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun DeleteConfirmationDialog(
	showDialog: MutableState<Boolean>,
	onConfirm: () -> Unit,
	onCancel: () -> Unit
) {
	if (showDialog.value) {
		AlertDialog(
			onDismissRequest = { showDialog.value = false },
			title = { Text("Delete") },
			text = { Text("Are you sure you want to delete this ?") },
			confirmButton = {
				TextButton(onClick = {
					onConfirm()
					showDialog.value = false
				}) {
					Text("Delete")
				}
			},
			dismissButton = {
				TextButton(onClick = {
					onCancel()
					showDialog.value = false
				}) {
					Text("Cancel")
				}
			}
		)
	}
}