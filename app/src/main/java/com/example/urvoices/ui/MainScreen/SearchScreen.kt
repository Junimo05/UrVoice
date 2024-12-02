package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.text.TextAnnotation
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.compose.filter.list.FilterListState
import com.algolia.instantsearch.compose.highlighting.toAnnotatedString
import com.algolia.instantsearch.compose.hits.HitsState
import com.algolia.instantsearch.compose.searchbox.SearchBoxState
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.model.filter.Filter
import com.example.urvoices.R
import com.example.urvoices.data.algolia.Post_Algolia
import com.example.urvoices.data.algolia.User_Algolia
import com.example.urvoices.data.model.User
import com.example.urvoices.ui._component.SearchBar
import com.example.urvoices.ui._component.TagSection
import com.example.urvoices.viewmodel.MediaPlayerVM
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun SearchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    searchBoxState: SearchBoxState,
    multiSearcher: MultiSearcher,
    userState: HitsState<User_Algolia>,
    postState: HitsState<Post_Algolia>,
    filterState: FilterState,
    filterListState: FilterListState<Filter.Tag>,
    onClearFilter: () -> Unit,
    playerViewModel: MediaPlayerVM,
) {
    val TAG = "SearchScreen"
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isSearching = remember { mutableStateOf(false) }

    LaunchedEffect(searchBoxState.query) {
        multiSearcher.searchAsync()
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
                    text = "Explore",
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
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            SearchBar(
                searchBoxState = searchBoxState,
                onValueChange = {value ->
//                    Log.e(TAG, "Search value change send to it: $value")
                    isSearching.value = value.isNotEmpty()
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                }
            )
            TagSection(
                filterListState = filterListState,
                filterState = filterState,
                isFiltered = isSearching,
                onClearFilter = {
                    onClearFilter()
                    isSearching.value = false
                }
            )
            if(isSearching.value){
                SearchList(
                    navController = navController,
                    userState = userState,
                    postState = postState,
                    listState = listState,
                    mediaPlayerVM = playerViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchList(
    navController: NavController,
    modifier: Modifier = Modifier,
    userState: HitsState<User_Algolia>,
    postState: HitsState<Post_Algolia>,
    listState: LazyListState,
    mediaPlayerVM: MediaPlayerVM,
){
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        stickyHeader { SectionTitle(title = "User") }
        if(userState.hits.isNotEmpty()){
            items(userState.hits.size) { index ->
                UserSearchItem(
                    user = userState.hits[index],
                    navController = navController
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(1.dp)
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No User Found",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        stickyHeader { SectionTitle(title = "Post") }
        if (postState.hits.isNotEmpty()) {
            items(postState.hits.size) { index ->
                PostSearchItem(
                    navController = navController,
                    post = postState.hits[index],
                    playerViewModel = mediaPlayerVM
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(1.dp)
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Post Found",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ){
        Text(
            text = title,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(8.dp)
        )
    }

}

@Composable
fun UserSearchItem(
    user: User_Algolia,
    navController: NavController
){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                //To Profile
                navController.navigate("profile/${user.ID}")
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl.takeIf { it.isNotEmpty() } ?: R.drawable.person)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            placeholder = painterResource(id = R.drawable.person),
            contentScale = ContentScale.Crop,
            colorFilter = if(user.avatarUrl.isEmpty()) {
                ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            } else null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
                .clickable {
                    //To Profile
                    navController.navigate("profile/${user.ID}")
                }
        )
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Normal)){
                        append(user.highlightedName!!.toAnnotatedString())
                    }
                },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun PostSearchItem(
    navController: NavController,
    post: Post_Algolia,
    playerViewModel: MediaPlayerVM
){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("post/${post.userId}/${post.ID}")
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Normal)) {
                        if (post.highlightedName != null) {
                            append(post.highlightedName!!.toAnnotatedString())
                        } else {
                            append("No Name")
                        }
                    }
                },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.description,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                post._tags.forEach { tag ->
                    Text(
                        text = "#$tag",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

