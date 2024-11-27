package com.example.urvoices.ui._component.SettingComponents

import android.annotation.SuppressLint
import android.util.Log
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
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.viewmodel.SettingState
import com.example.urvoices.viewmodel.SettingViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun BlockScreen(
	navController: NavController,
	settingVM: SettingViewModel
) {
	Scaffold(
		topBar = {
			TopBarBackButton(
				title = "Blocks",
				navController = navController
			)
		}
	) {
		val TAG = "BlockScreen"

		val blockList = settingVM.blockedUsers.collectAsLazyPagingItems()
		val uiState by settingVM.state.collectAsState()

		LaunchedEffect(blockList.itemCount) {
			Log.e(TAG, "BlockList: ${blockList.itemCount}")
		}

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
				if(blockList.itemCount > 0){
					SettingList(
						itemPaging = blockList,
						itemContent = {blockUser ->
							BlockUserItem(
								navController = navController,
								item = blockUser,
								onClick = {
									settingVM.unblockUser(blockUser)
								}
							)
						}
					)
				} else {
					Column(
						modifier = Modifier.fillMaxSize(),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					){
						Image(
							painterResource(id = R.drawable.ic_visibility_off),
							contentDescription = "No blocked users",
							modifier = Modifier.size(100.dp)
						)
						Text(
							text = "No blocked users",
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