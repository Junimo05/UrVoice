package com.example.urvoices.utils

import com.example.urvoices.data.model.Post
import com.google.gson.Gson

fun toPostJson(post: Post): String{
    val gson = Gson()
    val postJson = gson.toJson(post)
    return postJson
}