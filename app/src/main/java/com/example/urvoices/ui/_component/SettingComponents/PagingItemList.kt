package com.example.urvoices.ui._component.SettingComponents

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.urvoices.ui.MainScreen.ErrorItem
import com.example.urvoices.ui.MainScreen.LoadingItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun <T : Any> PagingItemList(
	items: List<T>?  = null,
	itemPaging: LazyPagingItems<T>? = null,
	itemContent: @Composable (T) -> Unit,
	modifier: Modifier = Modifier
){

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		if (items != null) {
			items(items) { item ->
				itemContent(item)
				HorizontalDivider()
			}
		} else if (itemPaging != null) {
			items(itemPaging.itemCount) { item ->
				itemContent(itemPaging[item]!!)
				HorizontalDivider()
			}

			itemPaging.apply {
				when{
					loadState.refresh is LoadState.Loading -> {
						item {
							LoadingItem()
						}
					}
					loadState.append is LoadState.Loading -> {
						item {
							Row(
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Center
							){
								LoadingItem()
							}
						}
					}
					loadState.refresh is LoadState.Error -> {
						val e = loadState.refresh as LoadState.Error
						item {
							ErrorItem(e.error.localizedMessage!!)
						}
					}
					loadState.append is LoadState.Error -> {
						val e = loadState.append as LoadState.Error
						item {
							ErrorItem(e.error.localizedMessage!!)
						}
					}
				}
			}
		}
	}
}

@Composable
fun <T : Any> PagingItemGrid(
	column: GridCells = GridCells.Fixed(3),
	items: List<T>?  = null,
	itemPaging: LazyPagingItems<T>? = null,
	itemContent: @Composable (T) -> Unit,
	modifier: Modifier = Modifier
){
	LazyVerticalGrid(
		columns = column,
		userScrollEnabled = true,
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(top = 8.dp)
	) {
		if (items != null) {
			items(items.size) { item ->
				itemContent(items[item])
				HorizontalDivider()
			}
		} else if (itemPaging != null) {
			items(itemPaging.itemCount) { item ->
				itemContent(itemPaging[item]!!)
				HorizontalDivider()
			}

			itemPaging.apply {
				when{
					loadState.refresh is LoadState.Loading -> {
						item {
							LoadingItem()
						}
					}
					loadState.append is LoadState.Loading -> {
						item {
							Row(
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Center
							){
								LoadingItem()
							}
						}
					}
					loadState.refresh is LoadState.Error -> {
						val e = loadState.refresh as LoadState.Error
						item {
							ErrorItem(e.error.localizedMessage!!)
						}
					}
					loadState.append is LoadState.Error -> {
						val e = loadState.append as LoadState.Error
						item {
							ErrorItem(e.error.localizedMessage!!)
						}
					}
				}
			}
		}
	}
}