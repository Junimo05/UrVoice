package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val username: String = "",
    val displayname: String = "",
    val email: String = "",
    val country: String = "",
    val avatarUrl: String = "",
    val bio: String = ""
) : Parcelable
