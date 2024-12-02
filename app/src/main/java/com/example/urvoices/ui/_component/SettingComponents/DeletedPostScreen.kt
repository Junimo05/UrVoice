package com.example.urvoices.ui._component.SettingComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.urvoices.R
import com.example.urvoices.ui._component.SavedItems
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.SettingState
import com.example.urvoices.viewmodel.SettingViewModel

@Composable
fun DeletedPostScreen(
	navController: NavController,
	settingVM: SettingViewModel,
	mediaPlayerVM: MediaPlayerVM,
	profileVM: ProfileViewModel
){
	val TAG = "DeletedPostScreen"

	LaunchedEffect(Unit) {
		settingVM.refreshDeletedPosts()
	}

	val deletedPostList = settingVM.deletedPosts.collectAsLazyPagingItems()
	val uiState by settingVM.state.collectAsState()

	Scaffold(
		topBar = {
			TopBarBackButton(
				title = "Deleted Posts",
				navController = navController
			)
		}
	) {
		Box(
			modifier = Modifier
				.padding(it)
				.fillMaxSize()
		){
			if(uiState == SettingState.Loading){
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				){
					CircularProgressIndicator()
				}
			} else {
				if (deletedPostList.itemCount > 0){
					SettingList(
						itemContent = {item ->
							DeletedPostItem(
								navController = navController, settingVM = settingVM, deletedPost = item)
						},
						itemPaging = deletedPostList,
					)
				} else {
					Column(
						modifier = Modifier.fillMaxSize(),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					){
						Image(
							painterResource(id = R.drawable.trash_03_svgrepo_com),
							contentDescription = "No deleted posts",
							modifier = Modifier.size(100.dp)
						)
						Text(
							text = "No deleted posts",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 18.sp
							)
						)
					}
				}
			}
		}
	}


}