package com.example.urvoices.data.algolia

import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class User_Algolia(
    val ID: String = "",
    val username: String = "",
    val email: String = "",
    val country: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    override val objectID: ObjectID,
    override val _highlightResult: JsonObject?
): Indexable, Highlightable {
    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute("username"))
}