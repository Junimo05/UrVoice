package com.example.thevoices.ui.Profile

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.thevoices.R
import com.example.thevoices.presentations.components.InteractionRow
import com.example.thevoices.presentations.components.PostComponent.AudioWaveform
import com.example.thevoices.presentations.components.TopBarBackButton
import com.example.thevoices.presentations.theme.MyTheme
import com.example.thevoices.utils.Post_Interactions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopBarBackButton(navController = navController, title = "Profile")
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceDim),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User handle and name
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
                    .padding(6.dp)
            )
            Text(text = "@rene.ui", fontSize = 14.sp)
            Text(text = "Jonathan Crowe", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Videomaker and Photographer", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "132", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Following", fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "5456", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Followers", fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "9445", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Stars", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .width(120.dp)
                ) {
                    Text(
                        text = "Follow",
                        color = Color.White
                    )
                }
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .width(120.dp)
                ) {
                    Text(
                        text = "Message",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of thumbnails
            LazyColumn {
                items(10){
                    //Todo: Change
                    Post_Profile_Item()
                }
            }
        }
    }

}

@Composable
fun Post_Profile_Item(

){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 3.dp)
            .border(1.dp, Color.Gray)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 18.dp, bottom = 4.dp, start = 10.dp, end = 2.dp)
    )
    {
        Row{
            Column(
                modifier = Modifier.weight(1f),
            ){
                Text(
                    text = "My story of moving to Japan",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .background(Color.Black)
                        .fillMaxWidth()
                )
                Text(
                    text = "blulbaosidjawoidjaowidjawoidjoawidjawoidhawiobcawdawdawdhawuidhwa" +
                            "dawiudhawiudhawiudahwiudhwaiudhwaiudhwaiudhwiuad" +
                            "diauwhdwaiudhwaudwaiudhwaiudhwaiudhwa",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_actions_more_2),
                contentDescription = "ActionMore",
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        AudioWaveform(duration = "4:12", isPlaying = true, percentPlayed = 0.5f)
        Spacer(modifier = Modifier.height(8.dp))
        InteractionRow(interactions = Post_Interactions(/*Todo interaction data*/))
    }
}

@Preview(
    name = "LightMode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun ProfileItemPreview() {
    MyTheme {
        Post_Profile_Item()
    }
}

@Preview(
    name = "LightMode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    MyTheme {
        ProfileScreen(navController)
    }
}
