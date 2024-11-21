package com.example.urvoices.ui._component.SettingComponents

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.db.Entity.BlockedUser
import com.example.urvoices.presentations.theme.MyTheme

@SuppressLint("UnrememberedMutableState")
@Composable
fun BlockUserItem(
	navController: NavController,
	item: BlockedUser,
	onClick: () -> Unit
) {
	val TAG = "BlockUserItem"
	var showDialog by remember { mutableStateOf(false) }
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp)
			.background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if(showDialog){
			AlertDialog(
				onDismissRequest = {
					showDialog = false
				},
				title = {
					Text(
						text = "Unblock User ${item.username}",
						style = TextStyle(
							fontWeight = FontWeight.Bold,
							fontSize = 20.sp
						),
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				text = {
					Text(
						text = "Are you sure you want to unblock ${item.username}? They will be able to see your posts and follow you.",
						style = TextStyle(
							fontWeight = FontWeight.Normal,
							fontSize = 16.sp
						),
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				confirmButton = {
					TextButton(
						onClick = {
							onClick()
							showDialog = false
						},
						colors = ButtonColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant,
							contentColor =  MaterialTheme.colorScheme.onSurfaceVariant,
							disabledContainerColor = Color.Transparent,
							disabledContentColor = Color.Transparent
						)
					) {
						Text(
							text = "Unblock",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 16.sp
							),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				},
				dismissButton = {
					TextButton(
						onClick = {
							showDialog = false
						},
						colors = ButtonColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant,
							contentColor =  MaterialTheme.colorScheme.onSurfaceVariant,
							disabledContainerColor = Color.Transparent,
							disabledContentColor = Color.Transparent
						)
					) {
						Text(
							text = "Cancel",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 16.sp
							),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			)
		}
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(item.avatarUrl.takeIf { it.isNotEmpty() } ?: R.drawable.person)
				.crossfade(true)
				.build(),
			contentDescription = "Avatar",
			placeholder = painterResource(id = R.drawable.person),
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.size(64.dp)
				.clip(CircleShape)
				.border(2.dp, Color.Black, CircleShape)
				.clickable {
					//To Profile
					navController.navigate("profile/${item.userID}")
				}
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = item.username,
			style = TextStyle(
				fontWeight = FontWeight.Bold,
				fontSize = 16.sp
			),
			color = MaterialTheme.colorScheme.onSurface
		)
		Spacer(modifier = Modifier.weight(1f))
		TextButton(
			onClick = {
				showDialog = true
			},
			colors = ButtonColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant,
					contentColor =  MaterialTheme.colorScheme.onSurfaceVariant,
					disabledContainerColor = Color.Transparent,
					disabledContentColor = Color.Transparent
			)
		) {
			Text(
				text = "Unblock",
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp
				),
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Preview
@Composable
fun BlockUserItemPreview() {
	MyTheme {
		BlockUserItem(
			navController = NavController(LocalContext.current),
			item = BlockedUser(
				userID = "1",
				username = "User",
				avatarUrl = ""
			)
		) {}
	}
}