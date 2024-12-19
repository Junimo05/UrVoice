package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui._component.TagInputField
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.deleteOldImageFile
import com.example.urvoices.utils.generateUniqueFileName
import com.example.urvoices.viewmodel.EditPostVM
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditPostScreen(
	navController: NavController,
	editPostVM: EditPostVM,
	mediaPlayerVM: MediaPlayerVM,
	post: Post,
	onUpdate:(Post) -> Unit,
){
	val TAG = "EditPostScreen"
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val editPostState by editPostVM.editPostState.observeAsState()
	var imgUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }
	var currentAvatar by rememberSaveable { mutableStateOf("") }
	var audioName by rememberSaveable { mutableStateOf(post.audioName) }
	var audioDes by rememberSaveable { mutableStateOf(post.description) }
	var tag by rememberSaveable { mutableStateOf(post._tags) }

	LaunchedEffect(post.ID) {
		currentAvatar = editPostVM.getImgUrl(post.ID!!)
	}

	//


	//Image Picker
	val imageCropper = rememberImageCropper()
	val cropState = imageCropper.cropState
	val imagePicker = rememberImagePicker(onImage = { uri ->
		scope.launch {
			deleteOldImageFile(imgUri)
			val result = imageCropper.crop(uri = uri, context = context)
			when (result) {
				CropResult.Cancelled -> {
					Log.d(TAG, "Crop Cancelled")
				}
				is CropResult.Success -> {
					val croppedBitmap = result.bitmap.asAndroidBitmap()
					val fileName = generateUniqueFileName()
					val file = File(context.cacheDir, fileName)
					FileOutputStream(file).use { out ->
						croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
					}
					imgUri = Uri.fromFile(file)
				}
				CropError.LoadingError -> {
					Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
				}
				CropError.SavingError -> {
					Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
				}
			}
		}
	})

	LaunchedEffect(editPostState) {

	}

	Scaffold(
		topBar = { TopBarBackButton(navController = navController, title = "Edit Post") },
	) {
		Column(
			modifier = Modifier
				.padding(it)
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background),
		) {
			if(cropState != null) {
				ImageCropperDialog(state = cropState)
			}
			Column(
				modifier = Modifier
					.padding(16.dp)
					.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(16.dp)
			){
				AsyncImage(
					model = when {
						imgUri != Uri.EMPTY -> {
							Log.e(TAG, "imgUri: $imgUri")
							imgUri
						}
						currentAvatar.isNotEmpty() -> {
							Log.e(TAG, "currentAvatar: $currentAvatar")
							ImageRequest.Builder(LocalContext.current)
								.data(currentAvatar)
								.crossfade(true)
								.build()
						}
						else -> {
							R.drawable.person
						}
					},
					contentDescription = "Avatar",
					placeholder = painterResource(id = R.drawable.add_photo_svgrepo_com),
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.size(120.dp)
						.clip(RectangleShape)
						.border(2.dp, Color.Black, RectangleShape)
						.padding(2.dp)
						.clickable {
							imagePicker.pick(
								"image/*"
							)
						}
				)
				Text(
					text = "Click to change image",
					style = TextStyle(
						color = MaterialTheme.colorScheme.onBackground,
						fontSize = 12.sp,
						fontWeight = FontWeight.Bold
					),
					modifier = Modifier.clickable {
						imagePicker.pick(
							"image/*"
						)
					}
				)
			}

			audioName?.let { it1 ->
				TextField(
					value = it1,
					onValueChange = { audioName = it },
					label = { Text("Urvoice's Name") },
					modifier = Modifier.fillMaxWidth()
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			TextField(
				value = audioDes,
				onValueChange = { audioDes = it },
				label = { Text("About this urvoice") },
				minLines = 3,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(16.dp))

			tag?.let { it1 ->
				TagInputField(
					value = it1,
					onValueChange = {tag = it},
					modifier = Modifier.fillMaxWidth()
				)
			}

			Row(
				modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			){
				Button(
					onClick = {
						scope.launch {
							val result = editPostVM.updatePost(
								mapOf(
									"ID" to post.ID,
									"imgUrl" to imgUri,
									"audioName" to audioName,
									"description" to audioDes,
									"tags" to tag
								),
								post
							)
							if (result){
								// show success dialog
								Toast.makeText(context, "Update success, Redirecting...", Toast.LENGTH_SHORT).show()
								delay(2000)
								onUpdate(post)
							} else {
								// show error dialog
								Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
							}
						}
					},
				) {
					Text("Update")
				}
			}
		}
	}
}