package com.example.urvoices.ui._component.ProfileComponent

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.viewmodel.ProfileViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileEditScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel? = null,
) {
    val currentUser by remember { mutableStateOf(profileViewModel?.displayuser) }

    var scrollState = rememberScrollState(0)

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = "Edit Profile"
            )
        }
    ) {
        if(currentUser != null){
            var username by remember { mutableStateOf(currentUser!!.username) }
            var bio by remember { mutableStateOf(currentUser!!.bio) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = it.calculateTopPadding())
                    .scrollable(
                        orientation = Orientation.Vertical,
                        enabled = true,
                        state = scrollState
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Profile picture
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }

                Text(
                    text = "Change Avatar",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable {

                        }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Profile fields
                ProfileField("Username", username) { username = it}
                ProfileField("Bio", bio) { bio = it }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                        .height(200.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                    ) {
                        Text("Links",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        )
                        Text("2",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(2) {
                            EditableTextField()
                        }
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = "Save",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

            }
        }else {

        }
    }
}

@Composable
fun EditableTextField(

) {
    var text by remember { mutableStateOf("Links") }
    var isEditing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (isEditing) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.Transparent)
                    .height(48.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color.Transparent,
                    unfocusedPlaceholderColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.9f),
            )
        }
        IconButton(onClick = { isEditing = !isEditing }) {
            Icon(
                painter = if(isEditing) painterResource(id = R.drawable.ic_actions_check) else painterResource(id = R.drawable.ic_contact_edit),
                contentDescription = "Edit",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(0.95f)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            ,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            ) {
            Text(label,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    MyTheme {
        ProfileEditScreen(
            navController = rememberNavController(),

        )
    }
}