package com.example.urvoices.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.compose.filter.clear.FilterClear
import com.algolia.instantsearch.compose.filter.list.FilterListState
import com.algolia.instantsearch.compose.hits.HitsState
import com.algolia.instantsearch.compose.searchbox.SearchBoxState
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.core.searcher.Searcher

import com.algolia.instantsearch.filter.clear.FilterClearConnector
import com.algolia.instantsearch.filter.clear.connectView
import com.algolia.instantsearch.filter.list.FilterListConnector
import com.algolia.instantsearch.filter.list.connectView
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.filter.state.groupOr
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.instantsearch.searcher.connectFilterState
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.instantsearch.searcher.hits.addHitsSearcher
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.helper.deserialize
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.filter.Filter
import com.algolia.search.transport.RequestOptions
import com.example.urvoices.BuildConfig
import com.example.urvoices.data.algolia.Post_Algolia
import com.example.urvoices.data.algolia.User_Algolia
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    val requestConfig = RequestOptions().apply {
        readTimeout = 5000
        writeTimeout = 5000

    }
    val multiSearcher = MultiSearcher(
        applicationID = ApplicationID(BuildConfig.ALGOLIA_APPLICATION_ID),
        apiKey = APIKey(BuildConfig.ALGOLIA_SEARCH_API_KEY),
        requestOptions = requestConfig
    ).apply {
        response.subscribe { response ->
            Log.d("SearchViewModel", "Raw response: $response")
        }
        error.subscribe { error ->
            Log.e("SearchViewModel", "Search error: $error")
        }
    }

    private val userSearcher = multiSearcher.addHitsSearcher(IndexName("user_search"))
    private val postSearcher = multiSearcher.addHitsSearcher(IndexName("post_search"))

    val searchBoxConnector = SearchBoxConnector(multiSearcher)


    //SearchBox
    val searchBoxState = SearchBoxState()
    val userState = HitsState<User_Algolia>()
    val postState = HitsState<Post_Algolia>()

    //Tag Filter
    val filterState = FilterState()
    val filterClear = FilterClear()
    val clearAll = FilterClearConnector(filterState = filterState)
    val tags = Attribute("_tags")
    val groupTag = groupOr(tags)


    val filters = listOf(
        Filter.Tag(Tags.STORY),
        Filter.Tag(Tags.MUSIC),
        Filter.Tag(Tags.TRENDING)
    )

    val filterListState = FilterListState<Filter.Tag>()
    val filterListConnector = FilterListConnector.Tag(
        filters = filters,
        filterState = filterState,
        groupID = groupTag
    )

    private val connections = ConnectionHandler(
        searchBoxConnector,
        filterListConnector,
    )
    init {
        userSearcher.response.subscribe { response ->
            Log.d("SearchViewModel", "User searcher response: $response")
        }

        postSearcher.response.subscribe { response ->
            Log.d("SearchViewModel", "Post searcher response: $response")
        }

        connections += userSearcher.connectFilterState(filterState)
        connections += postSearcher.connectFilterState(filterState)
        connections += clearAll.connectView(filterClear)

        connections += searchBoxConnector.connectView(searchBoxState)

        connections += userSearcher.connectHitsView(userState) { it.hits.deserialize(User_Algolia.serializer()) }
        connections += postSearcher.connectHitsView(postState) { it.hits.deserialize(Post_Algolia.serializer()) }

        connections += filterListConnector.connectView(filterListState)

        multiSearcher.searchAsync()
    }

    override fun onCleared() {
        super.onCleared()
        multiSearcher.cancel()
        connections.disconnect()
    }

    fun onClearFilter(){
        filterState.notify {
            clear(groupTag)
        }
    }

}

object Tags {
    const val STORY = "story"
    const val MUSIC = "music"
    const val TRENDING = "trending"
}
