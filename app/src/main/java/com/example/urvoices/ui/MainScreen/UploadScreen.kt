package com.example.urvoices.ui.MainScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.app.host.MainActivity
import com.example.urvoices.ui._component.TagInputField
import com.example.urvoices.ui._component.waveform.AudioWaveformLive
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.formatFileSize
import com.example.urvoices.utils.formatToMinSecFromMillisec
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.MediaRecorderVM
import com.example.urvoices.viewmodel.RecorderState
import com.example.urvoices.viewmodel.State.AppGlobalState
import com.example.urvoices.viewmodel.UIEvents
import com.example.urvoices.viewmodel.UploadState
import com.example.urvoices.viewmodel.UploadViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@Composable
fun UploadScreen(
    navController: NavController,
    playerViewModel: MediaPlayerVM,
    uploadViewModel: UploadViewModel,
    mediaRecorderVM: MediaRecorderVM
){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    //mediaRecorder VM
//    val mediaRecorderVM: MediaRecorderVM = hiltViewModel()

    //State
    val recordUri = rememberSaveable {
        mutableStateOf(Uri.EMPTY)
    }
    val uploadState by uploadViewModel.uploadState.observeAsState()
    val recorderState by mediaRecorderVM.recorderState.collectAsState()

    var snackbar by remember {
        mutableStateOf(false)
    }
    var showExitRecordingDialog by remember {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    var displaySwipe by mutableStateOf(true)
    LaunchedEffect(key1 = uploadState, key2 = recorderState) {
        if (uploadState != UploadState.Initial || recorderState != RecorderState.Idle) {
            displaySwipe = false
        }
    }

    LaunchedEffect(recorderState) {
        when (recorderState) {
            is RecorderState.Uploading -> {
                // Update when done
                val uri = mediaRecorderVM.getFileUri()
                if (uri != Uri.EMPTY) {
                    recordUri.value = uri
                    // Switch to Upload
                    pagerState.animateScrollToPage(0)
                }
                mediaRecorderVM.resetState()
            }
            else -> {} //Nothing
        }
    }

    LaunchedEffect(uploadState){
        when(uploadState){
            is UploadState.Success -> {
                // Show success message
                snackbar = true
                delay(3000)
                snackbar = false
                navController.navigate(MainScreen.UploadScreen.route)
                uploadViewModel.resetUploadState()
            }
            is UploadState.Error -> {
                // Show error message
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                uploadViewModel.resetUploadState()
            }
            else -> {}
        }
    }

    //Dispose when Switch Navigation
    DisposableEffect(pagerState.currentPage) {
        onDispose {
            if (recorderState is RecorderState.Recording) {
                Toast.makeText(context, "Your record has been paused", Toast.LENGTH_SHORT).show()
                mediaRecorderVM.pauseRecording()
                showExitRecordingDialog = true
            }
        }
    }

    //Dispose when swipePage
    LaunchedEffect(pagerState.currentPageOffsetFraction) {
        if(recorderState is RecorderState.Recording){
            snapshotFlow { pagerState.currentPageOffsetFraction }
                .collect { offsetFraction ->
                    if (offsetFraction < -0.25f && pagerState.currentPage == 1) {
                        mediaRecorderVM.pauseRecording()
                        showExitRecordingDialog = true
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            Column {
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
                        text = if (pagerState.currentPage == 0) "Upload" else "Record",
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
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> Upload(
                        navController = navController,
                        recordUri = recordUri,
                        uploadViewModel = uploadViewModel,
                        mediaRecorderVM = mediaRecorderVM,
                        playerViewModel = playerViewModel,
                        context = context
                    )
                    1 -> Record(
                        navController = navController,
                        recordUri = recordUri,
                        playerViewModel = playerViewModel,
                        mediaRecorderVM = mediaRecorderVM,
                        onRecordComplete = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                }

                ExitRecordingDialog(
                    showExitRecordingDialog = showExitRecordingDialog,
                    onDismiss = {
                        showExitRecordingDialog = false
                    },
                    onConfirm = {
                        showExitRecordingDialog = false
                        mediaRecorderVM.cancelRecording()
                        navController.navigate(MainScreen.UploadScreen.route)
                    }
                )
            }

            SwipeIndicator(
                displaySwipe = displaySwipe,
                pagerState = pagerState,
                modifier = Modifier.matchParentSize()
            )

            if (snackbar) {
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
}


@Composable
fun ExitRecordingDialog(
    showExitRecordingDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
){
    if(showExitRecordingDialog){
        AlertDialog(
            icon = {
                   Icon(
                       painter = painterResource(id = R.drawable.ic_media_microphone),
                       contentDescription = "Recording",
                   )
            },
            title = {
                Text(
                    text = "Recording in progress",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to exit? Your recording will be lost.",
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
            },
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = "Exit",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun SwipeIndicator(
    displaySwipe: Boolean,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {

    // Animation for fading effect
    val infiniteTransition = rememberInfiniteTransition(label = "Swipe Indicator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha Animation"
    )

    if(displaySwipe){
        if(pagerState.currentPage == 0) {
            Box(
                modifier = modifier
                    .fillMaxHeight(),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = "Swipe right",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_media_microphone),
                        contentDescription = "Swipe right",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "Swipe left",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_actions_add_file),
                        contentDescription = "Swipe left",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Record(
    navController: NavController,
    recordUri: MutableState<Uri>,
    playerViewModel: MediaPlayerVM,
    mediaRecorderVM: MediaRecorderVM,
    onRecordComplete: () -> Unit
){
    //
    val alertRecording = remember {
        mutableStateOf(false)
    }

    //Recorder
    val recorderState by mediaRecorderVM.recorderState.collectAsState()
    val recordingTime by mediaRecorderVM.recordingTime
    val scope = rememberCoroutineScope()

    val REQUEST_RECORD_AUDIO_PERMISSION = 200
    val context = LocalContext.current
    val activity = LocalContext.current as MainActivity

//    LaunchedEffect(mediaRecorderVM.amplitudesLive) {
//        Log.e("RecordScreen", "RecordScreen: ${mediaRecorderVM.amplitudesLive}")
//    }

    Scaffold(
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatToMinSecFromMillisec(mediaRecorderVM.recordingTime.longValue),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = when (recorderState) {
                            is RecorderState.Idle -> "Press Mic To Record"
                            is RecorderState.Recording -> "Recording"
                            is RecorderState.Paused -> "Paused"
                            else -> {"Loading..."}
                        }
                    )
                    AudioWaveformLive(
                        amplitudesLiveData = mediaRecorderVM.amplitudesLive,
                        waveformBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        onProgressChange = {

                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RecordIcon(
                        visible = recorderState != RecorderState.Idle,
                        recorderState = recorderState,
                        title = "Stop",
                        icon = R.drawable.ic_media_stop,
                        size = 32.dp,
                        onClick = {
                            mediaRecorderVM.pauseRecording()
                            alertRecording.value = true
                        }
                    )
                    //Alert Stop
                    if(alertRecording.value){
                        AlertDialog(
                            title = {
                                Text("Stop Recording")
                            },
                            text = {
                                Text("Are you sure you want to stop recording?")
                            },
                            onDismissRequest = { alertRecording.value = false },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        mediaRecorderVM.stopRecording()
                                        onRecordComplete()
                                        alertRecording.value = false
                                    }
                                ) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        alertRecording.value = false
                                    }
                                ) {
                                    Text("No")
                                }
                            },
                        )
                    }


                    Spacer(modifier = Modifier.width(16.dp))
                    RecordIcon(
                        recorderState = recorderState,
                        title = "Main Button",
                        icon = R.drawable.ic_media_microphone,
                        size = 64.dp,
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
                            }
                            if(recorderState is RecorderState.Idle){
                                //check AppGlobalState if is Playing
                                if(AppGlobalState.isPlaying.value){
                                    playerViewModel.onUIEvents(
                                        uiEvents = UIEvents.Stop
                                    )
                                }
                                mediaRecorderVM.startRecording()
                            } else if(recorderState is RecorderState.Recording){
                                mediaRecorderVM.pauseRecording()
                            } else if(recorderState is RecorderState.Paused){
                                mediaRecorderVM.resumeRecording()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    RecordIcon(
                        visible = recorderState != RecorderState.Idle,
                        recorderState = recorderState,
                        title = "Cancel",
                        icon = R.drawable.ic_actions_close,
                        size = 32.dp,
                        onClick = {
                            mediaRecorderVM.cancelRecording()
                        }
                    )

                }
            }
        }
    )
}

@Composable
fun RecordIcon(
    visible: Boolean = true,
    recorderState: RecorderState,
    title: String,
    icon: Int,
    size: Dp,
    onClick: () -> Unit
){
    if(visible){
        IconButton(
            onClick = {
                onClick()
            },
            modifier = Modifier
                .size(size)
                ,
        ) {
            Icon(
                painter = if(title != "Main Button"){
                    painterResource(id = icon)
                } else {
                    painterResource(
                        when (recorderState) {
                            is RecorderState.Idle -> R.drawable.ic_media_microphone
                            is RecorderState.Recording -> R.drawable.ic_media_pause
                            is RecorderState.Paused -> R.drawable.ic_media_play
                            else -> {R.drawable.ic_media_microphone}
                        }
                    )
                },
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(size),
                contentDescription = "Record"
            )
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Upload(
    navController: NavController,
    recordUri: MutableState<Uri>,
    uploadViewModel: UploadViewModel,
    mediaRecorderVM: MediaRecorderVM,
    playerViewModel: MediaPlayerVM,
    context: Context
){
    var audioUri by rememberSaveable { mutableStateOf<Uri?>(recordUri.value) }
    var audioName by rememberSaveable { mutableStateOf<String>("") }
    var audioDes by rememberSaveable { mutableStateOf<String>("") }
    var audioSize by rememberSaveable { mutableStateOf<Long?>(null) }
    var tag by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    var uploadState = uploadViewModel.uploadState.observeAsState()

    LaunchedEffect(recordUri.value) {
        if (recordUri.value != Uri.EMPTY) {
            audioUri = recordUri.value
            // Lấy thông tin file cho audio đã ghi
            val cursor = context.contentResolver.query(recordUri.value, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                c.moveToFirst()
                audioName = c.getString(nameIndex)
                audioSize = c.getLong(sizeIndex)
                uploadViewModel.setFileSelected()
            }
        } else {
            // Reset state khi recordUri empty
            audioUri = null
            audioName = ""
            audioDes = ""
            audioSize = null
            tag = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        Log.e("UploadScreen", "UploadScreen: ${audioUri}")
    }

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
                uploadViewModel.setFileSelected()
            }
        }
    }

    Scaffold(
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
                    if(audioUri != Uri.EMPTY){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            audioName.let { name ->
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

                            Row(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            uploadViewModel.createPost(audioUri!!, audioName , audioDes, tag)
                                        }
                                    },
                                ) {
                                    Text("Upload")
                                }
                                Button(
                                    onClick = {
                                        audioUri = null
                                        audioName = ""
                                        audioDes = ""
                                        audioSize = null
                                        tag = emptyList()
                                    },
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
