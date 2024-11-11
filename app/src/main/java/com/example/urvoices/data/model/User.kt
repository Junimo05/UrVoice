package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    val ID: String = "",
    val username: String = "",
    val email: String = "",
    val country: String = "",
    val avatarUrl: String = "",
    val bio: String = ""
) : Parcelable
