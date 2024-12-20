package com.example.urvoices.ui._component.ProfileComponent

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract.Profile
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.deleteOldImageFile
import com.example.urvoices.utils.generateUniqueFileName
import com.example.urvoices.utils.saveBitmapToUri
import com.example.urvoices.viewmodel.ProfileViewModel
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState",
    "StateFlowValueCalledInComposition"
)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
) {
    val TAG = "ProfileEditScreen"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentUser by profileViewModel.displayUser.collectAsState()
    val authProfile by remember { mutableStateOf(profileViewModel.authCurrentUser) }

    val scrollState = rememberScrollState(0)

    val isChanged = mutableStateOf(false)
    var username by remember { mutableStateOf(currentUser.username) }
    var pronouns by remember { mutableStateOf(currentUser.pronouns) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var country by remember { mutableStateOf(currentUser.country) }
    var email by remember { mutableStateOf(currentUser.email) }
    val downloadAvatarUrl by remember { mutableStateOf(currentUser.avatarUrl) }
    var imgUri by remember { mutableStateOf(Uri.EMPTY) }
    var updating by mutableStateOf(false)

    val focusRequester = remember { FocusRequester() }
    val currentFocusRequester = remember { mutableStateOf<FocusRequester?>(null) }

    //Image Handle
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


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            deleteOldImageFile(imgUri)
            val fileName = generateUniqueFileName()
            val uri = saveBitmapToUri(context, it, fileName)
            uri?.let { imageUri ->
                scope.launch {
                    val result = imageCropper.crop(uri = imageUri, context = context)
                    when (result) {
                        CropResult.Cancelled -> {
                            Log.d(TAG, "Crop Cancelled")
                        }
                        is CropResult.Success -> {
                            val croppedBitmap = result.bitmap.asAndroidBitmap()
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
            }
        }
    }



    //Confirm Changed To Update Profile
    LaunchedEffect(username, pronouns, bio, country, email, imgUri) {
       isChanged.value = username != currentUser.username || pronouns != currentUser.pronouns || bio != currentUser.bio || country != currentUser.country || imgUri != Uri.EMPTY
    }

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = "Edit Profile"
            )
        },
    ) {
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
                AvatarChangeComponent(
                    imgUri = imgUri,
                    currentAvatarUrl = downloadAvatarUrl,
                    imagePicker = {
                        imagePicker.pick(
                            "image/*",
                        )
                    },
                    cameraLauncher = {
                        cameraLauncher.launch(input = null)
                    }
                )
                if(cropState != null){
                    ImageCropperDialog(state = cropState)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile fields
            ProfileField(
                focusRequester = focusRequester,
                label = "Username",
                value = username,
                onValueChange = { username = it },
                currentFocusRequester = currentFocusRequester
            )

            ProfileField(
                focusRequester = focusRequester,
                label = "Pronouns",
                value = pronouns,
                onValueChange = { pronouns = it },
                currentFocusRequester = currentFocusRequester
            )

            ProfileField(
                focusRequester = focusRequester,
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                currentFocusRequester = currentFocusRequester
            )

            ProfileField(
                focusRequester = focusRequester,
                label = "Where I am",
                value = country,
                onValueChange = { country = it },
                currentFocusRequester = currentFocusRequester
            )

            ProfileField(
                focusRequester = focusRequester,
                label = "Email",
                value = email,
                onValueChange = { email = it },
                buttonIcon = R.drawable.pencil_svgrepo_com,
                onButtonClick = {
                    navController.navigate(MainScreen.SettingsScreen.SecurityScreen.route)
                },
                currentFocusRequester = currentFocusRequester
            )

            TextButton(onClick = { navController.navigate(MainScreen.SettingsScreen.SecurityScreen.route) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(8.dp)
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
                ){
                    Text(
                        text = "Change Password",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp)
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

            /*Spacer(modifier = Modifier.height(16.dp))

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

            }*/

            Spacer(modifier = Modifier.height(16.dp))
            if (updating) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Button(
                onClick = {
                    if (isChanged.value) {
                        scope.launch {
                            updating = true
                            val result = profileViewModel.updateProfile(
                                username = username,
                                pronouns = pronouns,
                                bio = bio,
                                country = country,
                                avatarUri = imgUri
                            )
                            if (result) {
                                // Delete image after update
                                deleteOldImageFile(imgUri)
                                imgUri = Uri.EMPTY
                                //Reload Data
                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                profileViewModel.loadData(profileViewModel.currentUserID)
                                updating = false
                                navController.navigate(MainScreen.ProfileScreen.MainProfileScreen.route)
                            } else {
                                Toast.makeText(context, "Error when updating profile", Toast.LENGTH_SHORT).show()
                                updating = false
                            }
                        }
                    }
                },
                enabled = !updating,
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
    }
}

@Composable
fun AvatarChangeComponent(
    imgUri: Uri,
    currentAvatarUrl: String?,
    imagePicker: () -> Unit,
    cameraLauncher: () -> Unit
) {
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            cameraLauncher()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(imgUri) {
        if(imgUri != Uri.EMPTY){
            selectedImageUri = imgUri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model =
            if(imgUri != Uri.EMPTY)
                imgUri
            else
                ImageRequest.Builder(LocalContext.current)
                .data(currentAvatarUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.person)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            placeholder = painterResource(id = R.drawable.person),
            colorFilter = if (currentAvatarUrl == null) {
                ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            } else {
                null
            },
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
    focusRequester: FocusRequester,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    buttonIcon: Int = R.drawable.ic_contact_edit,
    onButtonClick: (() -> Unit)? = null,
    currentFocusRequester: MutableState<FocusRequester?>
) {
    var canEdit by remember { mutableStateOf(false) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        cursorColor = Color.White,
                        disabledTextColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                canEdit = false
                                if (currentFocusRequester.value == focusRequester) {
                                    currentFocusRequester.value = null
                                }
                            }
                        },
                    enabled = canEdit
                )
                IconButton(
                    onClick = {
                        onButtonClick?.invoke()
                        if (currentFocusRequester.value != null && currentFocusRequester.value != focusRequester) {
                            currentFocusRequester.value?.freeFocus()
                        }
                        canEdit = !canEdit
                        if (canEdit) {
                            currentFocusRequester.value = focusRequester
                            focusRequester.requestFocus()
                        } else {
                            focusRequester.freeFocus()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if(!canEdit) buttonIcon else R.drawable.ic_actions_check),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

sealed class ImageState {
    data object Empty : ImageState()
    data object Loading : ImageState()
    data object Success : ImageState()
}
