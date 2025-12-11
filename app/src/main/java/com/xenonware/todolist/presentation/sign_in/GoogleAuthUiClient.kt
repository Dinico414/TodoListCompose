@file:Suppress("DEPRECATION")

package com.xenonware.todolist.presentation.sign_in

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.xenonware.todolist.R
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    suspend fun signIn(): BeginSignInResult {
        return oneTapClient.beginSignIn(
            buildSignInRequest()
        ).await()
    }

    fun getTraditionalSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        return firebaseAuthWithGoogle(googleIdToken)
    }

    suspend fun signInWithTraditionalIntent(intent: Intent): SignInResult {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        val account = task.await() // This can throw ApiException
        val idToken = account.idToken
        return firebaseAuthWithGoogle(idToken)
    }

    private suspend fun firebaseAuthWithGoogle(googleIdToken: String?): SignInResult {
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                        email = email.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            googleSignInClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = try {
        auth.currentUser?.run {
            UserData(
                userId = uid,
                username = displayName,
                profilePictureUrl = photoUrl?.toString(),
                email = email.toString()
            )
        }
    } catch (e: Exception) {
        if (e.message?.contains("auth_api_credentials_begin_sign_in") == true ||
            e is com.google.android.gms.common.api.UnsupportedApiCallException) {
            null
        } else {
            throw e
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id)).build()
        ).setAutoSelectEnabled(true).build()

    }
}
