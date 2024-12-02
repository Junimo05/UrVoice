package com.example.urvoices.ui._component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algolia.instantsearch.compose.searchbox.SearchBoxState
import com.example.urvoices.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchBoxState: SearchBoxState,
    onValueChange: (String) -> Unit = {},
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
        ,
    ) {
        TextField(
            value = searchBoxState.query,
            onValueChange = {
                searchBoxState.setText(it)
                onValueChange(it)
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
            ),
            maxLines = 1,
            placeholder = {
                Text("Search...")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    searchBoxState.setText(searchBoxState.query, true)
                }
            ),
            trailingIcon = {
                if (searchBoxState.query.isNotEmpty()) {
                    IconButton(onClick = {
                        searchBoxState.setText("")
                        onValueChange("")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_actions_close),
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}