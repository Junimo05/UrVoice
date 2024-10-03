package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.PlaylistItem
import com.example.urvoices.ui._component.PostComponent.ProfilePostItem
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.UserPreferences
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    userId: String = "",
    navController: NavController,
    playerViewModel: MediaPlayerViewModel,
) {
    val TAG = "ProfileScreen"
    val profileViewModel = hiltViewModel<ProfileViewModel>()
    profileViewModel.loadData(userId)
    val scope = CoroutineScope(Dispatchers.Main)
    val isUser = profileViewModel.isCurrentUser
    var tab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    //TODO: Change text var
                    Text(
                        text = "lucasmorrison",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IconButton(
                        onClick = {
                                  /*TODO*/
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Change user",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
                IconButton(onClick = { /*TODO*/ },
                    modifier = Modifier.size(24.dp)

                ){
                    Icon(
                        painter = painterResource(id = if(isUser) R.drawable.ic_actions_add else R.drawable.ic_actions_more_1),
                        contentDescription = "ActionMore",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(top = 8.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User handle and name
            UserInfo(
                postsCount = 10,
                followingCount = 10,
                followersCount = 10
            )
            Spacer(modifier = Modifier.height(4.dp))

            //Posts/Playlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                             tab = 0
                        }
                ) {
                    Text(
                        text = "Posts",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(fraction = 0.5f)
                        .background(if (tab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
                Column(
                    modifier = Modifier
                        .clickable {
                            tab = 1
                        }
                ) {
                    Text(
                        text = "Playlists",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .background(if (tab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                } 
            }
            Spacer(modifier = Modifier.height(8.dp))



            // Grid of thumbnails
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                if(tab == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp),

                    ) {
                        items(10) {
                            ProfilePostItem(
                                title = "Title",
                                starsCount = 10,
                                commentsCount = 10,
                                onPlayClick = { /*TODO*/ },
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(10){
                            PlaylistItem(
                                title = "Title",
                                number = 10
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserInfo(
    postsCount: Int,
    followingCount: Int,
    followersCount: Int
){
    val infoList = listOf(
        "Posts" to postsCount,
        "Following" to followingCount,
        "Followers" to followersCount
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            ,
        verticalAlignment = Alignment.CenterVertically,
    ){
        // User Avatar/Name/Link
        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .fillMaxHeight()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lucas Morrison",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "@lucasmorrison",
                style = TextStyle(
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .size(128.dp, 32.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "link",
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    //Link tag
                    text = "google.com",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Account Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .padding(top = 10.dp, end = 10.dp, bottom = 10.dp, start = 0.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                infoList.forEach { (title, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = count.toString(),
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                        )
                        Text(
                            text = title,
                            style = TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 12.sp
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .clickable {

                    }
                    ,

            ){
                Text(
                    text = "Watchaboutme",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(15.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            /*TODO*/
                        }
                ) {
                    Text(
                        text = "Follow",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .padding(4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { /*TODO*/ }
                ) {
                    Text(
                        text = "Message",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}


@Preview(
    name = "LightMode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)

//@Composable
//fun ProfileItemPreview() {
//    MyTheme {
//        UserInfo(postsCount = 10, followingCount = 10, followersCount = 10)
//    }
//}

@Preview(
    name = "LightMode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    MyTheme {
        ProfileScreen(
            userId = "lucasmorrison",
            navController = navController,
            playerViewModel = hiltViewModel()
        )
    }
}
