package com.example.urvoices.ui._component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import java.util.Locale

@Composable
fun TagInputField(
    value: List<String>,
    onValueChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var tag by remember { mutableStateOf(TextFieldValue("")) }
    var tags by remember {
        mutableStateOf(value)
    }

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TextField(
            value = tag,
            label = { Text("Tags") },
            onValueChange = {newValue ->
                if(newValue.text.endsWith(' ')){
                    tags = tags + newValue.text.trim().lowercase(Locale.ROOT)
                    onValueChange(tags)
                    tag = TextFieldValue("")
                } else {
                    tag = newValue
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            items(tags) {tag ->
                TagChip(tag = tag, onTagRemoved = {
                    tags = tags - it
                    onValueChange(tags)
                })
            }
        }
    }
}

@Composable
fun TagChip(tag: String, onTagRemoved: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            IconButton(onClick = { onTagRemoved(tag) }) {
                Icon(painter = painterResource(id = R.drawable.ic_actions_close),
                    contentDescription = "Remove"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagChipPreview() {
    MyTheme {
        TagChip("Tag", onTagRemoved = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TagInputFieldPreview() {
    TagInputField(
        value = emptyList(),
        onValueChange = {}
    )
}