package com.example.urvoices.ui._component.ProfileComponent

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.Auth.checkProvider
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.viewmodel.ProfileViewModel
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.Current

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState",
    "StateFlowValueCalledInComposition"
)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel? = null,
) {
    val TAG = "ProfileEditScreen"

    val currentUser by remember { mutableStateOf(profileViewModel?.displayuser) }
    val authProfile by remember { mutableStateOf(profileViewModel?.authCurrentUser) }

    val scrollState = rememberScrollState(0)

    val isChanged = mutableStateOf(false)
    var username by remember { mutableStateOf(currentUser!!.username) }
    var bio by remember { mutableStateOf(currentUser!!.bio) }
    var country by remember { mutableStateOf(currentUser!!.country) }
    var email by remember { mutableStateOf(currentUser!!.email) }
    val downloadAvatarUrl by remember { mutableStateOf(currentUser!!.avatarUrl) }
    var imgUri by remember { mutableStateOf(Uri.EMPTY) }
    var showPreviewDialog by rememberSaveable {
        mutableStateOf(false)
    }


    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Handle the returned Uri
            imgUri = it
            showPreviewDialog = true
        }
    }

    LaunchedEffect(username, bio, country, email, imgUri) {
       isChanged.value = username != currentUser!!.username || bio != currentUser!!.bio || country != currentUser!!.country || email != currentUser!!.email
    }

    fun uploadImageHandle(){
        imagePicker.launch("image/*")
    }

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = "Edit Profile"
            )
        },
    ) {
        if(currentUser != null){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = it.calculateTopPadding())
                    .verticalScroll(
                        state = scrollState,
                        enabled = true
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Profile picture
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if(downloadAvatarUrl != ""){
//                        AsyncImage(
//                            model = ImageRequest.Builder(LocalContext.current)
//                                .data(downloadAvatarUrl)
//                                .crossfade(true)
//                                .build(),
//                            contentDescription = "Avatar",
//                            placeholder = painterResource(id = R.drawable.person),
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .size(64.dp)
//                                .clip(CircleShape)
//                                .border(2.dp, Color.Black, CircleShape)
//                                .clickable {
//                                    uploadImageHandle()
//                                }
//                        )
                        AvatarChangeComponent(
                            imgUri = imgUri,
                            currentAvatarUrl = downloadAvatarUrl,
                            imagePicker = {
                                imagePicker.launch("image/*")
                            },
                            onAvatarSelected = {
                                imgUri = it
                            },
                            onAvatarConfirmed = {

                            },

                        )
                    } else {
                        AvatarChangeComponent(
                            imgUri = imgUri,
                            currentAvatarUrl = downloadAvatarUrl,
                            imagePicker = {
                                imagePicker.launch("image/*")
                            },
                            onAvatarSelected = {
                                imgUri = it
                            },
                            onAvatarConfirmed = {

                            },

                            )
                    }
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
                            imagePicker.launch("image/*")
                        }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Profile fields
                ProfileField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it }
                )
                ProfileField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { bio = it }
                )

                ProfileField(
                    label = "Country",
                    value = country,
                    onValueChange = { country = it }
                )

                ProfileField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    canEdit = checkProvider(authProfile) != GoogleAuthProvider.PROVIDER_ID
                )


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
                    onClick = {
                        if(isChanged.value){
                            profileViewModel?.updateProfile(
                                username = username,
                                bio = bio,
                                country = country,
                                email = email,
                                avatarUri = imgUri
                            )
                            navController.navigate(MainScreen.ProfileScreen.MainProfileScreen.route)
                        }
                    },
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
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Image Preview Dialog
        if(showPreviewDialog){
            ImagePreviewDialog(
                imageUri = imgUri,
                onDismissRequest = { showPreviewDialog = false },
                onConfirm = { showPreviewDialog = false }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImagePreviewDialog(
    imageUri: Uri,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    val imageBitmap = rememberAsyncImagePainter(model = imageUri)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()
    val transformationState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                scale *= zoom
                                offset += pan
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                Image(
                    painter = imageBitmap,
                    contentDescription = "Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text("Hủy")
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text("Xác nhận")
                }
            }
        }
    }
}

@Composable
fun AvatarChangeComponent(
    imgUri: Uri,
    currentAvatarUrl: String?,
    imagePicker: () -> Unit,
    onAvatarSelected: (Uri) -> Unit,
    onAvatarConfirmed: (Uri) -> Unit
) {
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Open camera
        } else {
            // Permission denied, handle this situation
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentAvatarUrl)
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
                    showImagePicker = true
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showImagePicker = true }) {
            Text("Change Avatar")
        }
    }

    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Choose an action") },
            text = {
                Column {
                    Button(
                        onClick = {
                            showImagePicker = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Take a photo")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showImagePicker = false
                            imagePicker()
                        }
                    ) {
                        Text("Choose from gallery")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showImagePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImagePreview && selectedImageUri != null) {
        //TODO: FIX THIS
        ImagePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismissRequest = {
                showImagePreview = false
                selectedImageUri = null
            },
            onConfirm = {
                selectedImageUri?.let { uri ->
                    onAvatarConfirmed(uri)
                    showImagePreview = false
                }
            }
        )
    }
}


@Composable
fun EditableTextField(
    //TODO: Update Links
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

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    canEdit : Boolean = true
) {
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
                colors = TextFieldDefaults.colors(
                    cursorColor = Color.White,
                    disabledTextColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit
            )
        }
    }
}

sealed class ImageState {
    data object Empty : ImageState()
    data object Loading : ImageState()
    data object Success : ImageState()
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