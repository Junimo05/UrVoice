package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.data.model.Comment
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.processUsername
import com.example.urvoices.viewmodel.PostDetailState

@Composable
fun CommentBar(
    uiState: PostDetailState,
    currentUserName: String?,
    onSendMessage: (String, String) -> Unit,
    onAttachFile: () -> Unit,
    focusRequester: FocusRequester,
    replyTo : MutableState<Comment?>,
    parentUsername: MutableState<String>,
    commentText: MutableState<String>,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(replyTo.value) {
        if (replyTo.value != null) {
            if(parentUsername.value.isNotBlank()){
                commentText.value = "@${processUsername(parentUsername.value)} "
            }
            focusRequester.requestFocus()
        }
    }

    fun isCommentTextEmpty(): Boolean {
        val replyText = if (parentUsername.value.isNotBlank()) "@${processUsername(parentUsername.value)} " else ""
        return commentText.value.trim() == replyText.trim()
    }

    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(60.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            if(isFocused){
//                IconButton(
//                    onClick = { /* Xử lý chức năng */ },
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_editor_attachament),
//                        contentDescription = "Attach file",
//                        tint = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//                }
//            }
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ) ,
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f),
            ) {
                TextField(
                    value = TextFieldValue(
                        text = commentText.value,
                        selection = TextRange(commentText.value.length)
                    ),
                    onValueChange = { commentText.value = it.text },
                    placeholder = { if(currentUserName != null) Text("Comment as $currentUserName") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    enabled = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                isFocused = true
                            }
                        },
//                    visualTransformation = PasswordVisualTransformation(),

                )
            }
            if(isFocused){
                IconButton(
                    onClick = {
                        if (commentText.value.isNotBlank()) {

                            if(replyTo.value == null){
                                onSendMessage(commentText.value, "")
                            }
                            else{
                                onSendMessage(commentText.value, replyTo.value?.id!!)
                            }
                            isFocused = false
                            focusManager.clearFocus()
                            commentText.value = ""
                        }
                    },
                    enabled = !isCommentTextEmpty()
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (commentText.value.isNotBlank()) Color(0xFF2196F3) else Color.Gray
                    )
                }
            }
        }
    }
}
