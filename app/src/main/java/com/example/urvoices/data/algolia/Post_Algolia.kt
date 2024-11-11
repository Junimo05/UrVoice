package com.example.urvoices.data.algolia

import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Post_Algolia(
    val ID: String = "",
    val userId: String = "",
    val url: String = "",
    val audioName: String = "No Name",
    val description: String = "",
    @SerialName("_tags")
    val _tags: List<String> = listOf(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long = 0,
    override val objectID: ObjectID,
    override val _highlightResult: JsonObject?
): Indexable, Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute("audioName"))
}