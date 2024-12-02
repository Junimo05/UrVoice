package com.example.urvoices.ui._component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algolia.instantsearch.compose.filter.list.FilterListState
import com.algolia.instantsearch.core.selectable.list.SelectableItem
import com.algolia.instantsearch.filter.clear.FilterClearConnector
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.filter.state.groupOr
import com.algolia.search.model.Attribute
import com.algolia.search.model.filter.Filter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSection(
    tags: List<String> = emptyList(),
    filterState: FilterState,
    filterListState: FilterListState<Filter.Tag> = FilterListState(),
    isFiltered: MutableState<Boolean>,
    onClearFilter: () -> Unit = {},
) {
    val TAG = "TagSection"



    LaunchedEffect(filterState.filters.value) {
        Log.d(TAG, "FilterListState items: ${filterListState.items.size}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)// Light pink background
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                text = "Tag",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .weight(1f)
            )
            if(isFiltered.value){
                Text(
                    text = "Clear Filter",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            isFiltered.value = false
                            onClearFilter()
                        }
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            overflow = FlowRowOverflow.Clip,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if(tags.isNotEmpty()) {
                tags.forEach { tag ->
                    Tag(
                        text = tag,
                        onClick = {
                        }
                    )
                }
            } else {
                filterListState.items.forEach { filter ->
                    Tag(
                        filterTag = filter,
                        isSelected = filter.second,
                        onClick = {
                            isFiltered.value = !isFiltered.value
                            filterListState.onSelection?.invoke(it.first)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Tag(
    text: String = "",
    isSelected: Boolean = false,
    filterTag: SelectableItem<Filter.Tag> = SelectableItem(Filter.Tag(text), false),
    onClick: (SelectableItem<Filter.Tag>) -> Unit = {}
) {
    val value = text.ifEmpty { filterTag.first.value }
    Surface(
        shape = RoundedCornerShape(16.dp),
        //add Logic to change BackGround when isSelected
        color = if(isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick(filterTag)
            }
    ) {
        Text(
            text = "#$value",
            color = if(isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
