package com.nandikacreativestudio.chatloom.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nandikacreativestudio.chatloom.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    private val credentialManager = CredentialManager.create(context)

    fun getSignInRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getServerClientId())
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun launchSignIn(
        activity: Activity,
        request: GetCredentialRequest
    ): GetCredentialResponse? {
        return try {
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        } catch (e: GetCredentialException) {
            null
        }
    }

    suspend fun firebaseSignInWithGoogle(
        credentialResponse: GetCredentialResponse
    ): Boolean {
        return try {
            val credential = credentialResponse.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken,
                null
            )

            firebaseAuth.signInWithCredential(firebaseCredential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun isLoggedIn() = firebaseAuth.currentUser != null

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun getServerClientId(): String {
        return context.getString(R.string.default_web_client_id)
    }
}