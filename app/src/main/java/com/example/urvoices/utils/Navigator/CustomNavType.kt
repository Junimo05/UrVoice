package com.example.urvoices.utils.Navigator

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.navigation.NavType
import com.example.urvoices.data.model.Post
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object CustomNavType {
    val PostType = object : NavType<Post>(
        isNullableAllowed = false,
    ) {
        override fun get(bundle: Bundle, key: String): Post? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Post {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Post): String {
            return Uri.encode(Json.encodeToString(value))
        }

        override fun put(bundle: Bundle, key: String, value: Post) {
            bundle.putString(key, Json.encodeToString(value))
        }
    }
}


inline fun <reified T : Parcelable?> navType(
    isNullableAllowed: Boolean = true,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }
    }

    override fun parseValue(value: String): T {
        val deserializedResult = json.decodeFromString<T>(value)
        return deserializedResult
    }

    override fun serializeAsValue(value: T): String {
        return if (value == null) {
            ""
        } else {
            json.encodeToString(value)
        }
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        if (value == null) {
            bundle.putParcelable(key, null)
        } else {
            bundle.putParcelable(key, value)
        }
    }
}