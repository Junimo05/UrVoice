package com.example.urvoices.ui._component.SettingComponents

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.db.Entity.DeletedPost
import com.example.urvoices.utils.getDayTime
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.SettingViewModel

@Composable
fun DeletedPostItem(
	navController: NavController,
	settingVM: SettingViewModel,
	deletedPost: DeletedPost,
){
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.primaryContainer)
	){
		Row(
			verticalAlignment = Alignment.CenterVertically,
		){
			Column(
				modifier = Modifier.weight(1f)

			) {
				//UserInfo
//				Row(
//					modifier = Modifier
//						.padding(8.dp),
//					horizontalArrangement = Arrangement.Start,
//					verticalAlignment = Alignment.CenterVertically
//				){
//					AsyncImage(
//						model = ImageRequest.Builder(LocalContext.current)
//							.data(deletedPost.avatarUrl)
//							.crossfade(true)
//							.build(),
//						contentDescription = "Avatar",
//						placeholder = painterResource(id = R.drawable.person),
//						contentScale = ContentScale.Crop,
//						modifier = Modifier
//							.size(32.dp)
//							.clip(CircleShape)
//							.border(2.dp, Color.Black, CircleShape)
//					)
//					Text(
//						text = deletedPost.username,
//						fontWeight = FontWeight.Bold,
//						style = MaterialTheme.typography.titleMedium,
//						overflow = TextOverflow.Clip,
//						maxLines = 1,
//						modifier = Modifier.padding(8.dp),
//						color = MaterialTheme.colorScheme.onPrimaryContainer
//					)
//				}

				//Post name and image
				Row(
					modifier = Modifier
						.padding(8.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				){
					AsyncImage(
						model = ImageRequest.Builder(LocalContext.current)
							.data(
								if(deletedPost.imgUrl.isEmpty()){
									R.drawable.music_play_svgrepo_com
								} else {
									deletedPost.imgUrl
								}
							)
							.crossfade(true)
							.build(),
						contentDescription = "Avatar",
						placeholder = painterResource(id = R.drawable.ic_media_note),
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.size(64.dp)
							.clip(RectangleShape)
							.border(2.dp, Color.Black, RectangleShape)
					)
					Text(
						text = deletedPost.audioName,
						fontWeight = FontWeight.Bold,
						style = MaterialTheme.typography.titleLarge,
						overflow = TextOverflow.Clip,
						maxLines = 1,
						modifier = Modifier,
						color = MaterialTheme.colorScheme.onPrimaryContainer
					)
				}
				Spacer(modifier = Modifier.size(8.dp))
				//Time Delete Text
				Text(
					text = getDayTime(deletedPost.deletedAt),
					style = TextStyle(
						fontWeight = FontWeight.Normal,
						fontSize = 12.sp
					),
					color = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			TextButton(onClick = {
				settingVM.restoreDeletedPost(deletedPost)
			}) {
				Text(
					text = "Restore",
					style = TextStyle(
						fontWeight = FontWeight.Bold,
						fontSize = 14.sp
					),
				)
			}
		}
	}

}