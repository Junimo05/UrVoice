package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.urvoices.ui._component.TagInputField
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.formatFileSize
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.UploadState
import com.example.urvoices.viewmodel.UploadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UploadScreen(
    navController: NavController,
    playerViewModel: MediaPlayerViewModel,
    uploadViewModel: UploadViewModel
){
    val uploadState by uploadViewModel.uploadState.observeAsState()
    var snackbar by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 =uploadState){
        when(uploadState){
            is UploadState.Success -> {
                // Show success message
                snackbar = true
                delay(3000)
                snackbar = false
                navController.navigate(MainScreen.UploadScreen.route)
            }
            is UploadState.Error -> {
                // Show error message
            }
            else -> {}
        }

    }

    Box(modifier = Modifier.fillMaxSize()) {
        Upload(
            navController = navController,
            context = LocalContext.current,
            uploadViewModel = uploadViewModel
        )
        if(snackbar){
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Text("Upload success")
            }
        }
        if (uploadState is UploadState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Upload(
    navController: NavController,
    uploadViewModel: UploadViewModel,
    context: Context
){
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioName by remember { mutableStateOf<String>("") }
    var audioDes by remember { mutableStateOf<String>("") }
    var audioSize by remember { mutableStateOf<Long?>(null) }
    var tag by remember { mutableStateOf<List<String>>(emptyList()) }

    var uploadState = uploadViewModel.uploadState.observeAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            audioUri = it
            // Get file name and size
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                c.moveToFirst()
                audioName = c.getString(nameIndex)
                audioSize = c.getLong(sizeIndex)
            }
        }
    }



    Scaffold(
        topBar = {
            Box(
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
                    }
            ) {
                Text(
                    text = "Upload",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 35.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(painter = painterResource(id = R.drawable.record), contentDescription = "add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { it ->
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    launcher.launch("audio/*")
                },
                    modifier = Modifier
                        .size(200.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_audio),
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                audioUri?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        audioName?.let { name ->
                            TextField(
                                value = name,
                                onValueChange = { audioName = it },
                                label = { Text("Urvoice's Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = audioSize?.let { size -> formatFileSize(size) } ?: "Unknown",
                            onValueChange = { /* Handle text change */},
                            label = { Text("UrVoice's Size") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = audioDes,
                            onValueChange = { audioDes = it },
                            label = { Text("About this urvoice") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TagInputField(
                            value = tag,
                            onValueChange = {tag = it},
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                  CoroutineScope(Dispatchers.Main).launch {
                                      uploadViewModel.createPost(audioUri!!, audioName , audioDes, tag)
                                  }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Upload")
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val uploadViewModel: UploadViewModel = hiltViewModel()
    MyTheme {
        Upload(navController = rememberNavController(), context = LocalContext.current, uploadViewModel = uploadViewModel)
    }
}