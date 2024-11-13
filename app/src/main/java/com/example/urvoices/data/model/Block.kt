package com.example.urvoices.data.model

data class Block (
    val actionID: String,
    val targetID: String,
) {
    fun toBlockMap(): Map<String, Any?> {
        return mapOf(
            "actionID" to actionID,
            "targetID" to targetID
        )
    }
}

data class BlockList(
    val id: String?,
    val targetID: List<String>
){
    constructor(): this("", emptyList())
}