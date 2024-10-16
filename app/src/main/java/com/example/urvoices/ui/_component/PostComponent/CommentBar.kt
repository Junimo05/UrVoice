package com.example.urvoices.ui._component.PostComponent

import android.net.Uri
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBar(
    onSendMessage: (String) -> Unit,
    onAttachFile: () -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(isFocused){
                IconButton(
                    onClick = { /* Xử lý chức năng sticker */ },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_editor_attachament),
                        contentDescription = "Attach file",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
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
                    .height(50.dp)
                    .weight(1f),
            ) {
                TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Bình luận dưới tên Better Call Tún") },
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    enabled = true,
                    modifier = Modifier.fillMaxSize().align(Alignment.CenterHorizontally)
                        .focusRequester(focusRequester)
                        .onFocusChanged {focusState ->
                            if (focusState.isFocused){
                                isFocused = true
                            }
                        },
//                    visualTransformation = PasswordVisualTransformation(),

                )
            }
            if(isFocused){
                IconButton(
                    onClick = {
                        if (comment.isNotBlank()) {
                            // Xử lý gửi comment
                            isFocused = false
                            comment = ""
                        }
                    },
                    enabled = comment.isNotBlank()
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (comment.isNotBlank()) Color(0xFF2196F3) else Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentBarPreview() {
    MyTheme {
        CommentBar(
            onSendMessage = {},
            onAttachFile = {}
        )
    }
}