package com.example.urvoices.utils.Auth

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.urvoices.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun GoogleSignIn(
    context: Context,
    scope: CoroutineScope,
    onGetCredentialResponse: (Credential) -> Unit
){
    val credentialManager = CredentialManager.create(context)

    val googleOptions = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOptions)
        .build()

    scope.launch {
        try{
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            onGetCredentialResponse(result.credential)
        }catch (e: Exception) {
            Log.e("GoogleSignIn", "Error: ${e.message}")
        }
    }
}