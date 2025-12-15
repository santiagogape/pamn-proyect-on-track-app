package com.example.on_track_app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.on_track_app.model.MockUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


abstract class AuthClient(protected val context: Context) {
    abstract suspend fun signIn(activityContext: Context): Boolean
    abstract fun getUser(): MockUser?
    abstract suspend fun ensureUserExists()
}
class GoogleAuthClient(context: Context):AuthClient(context) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val credentialManager = CredentialManager.create(this.context)

    override suspend fun signIn(activityContext: Context): Boolean {
        try {
            // 1. Build the Google Sign-In Request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("236486268301-j30773r9oq9m3tpc5507b9o2b6sjhjrm.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Launch the Android system bottom sheet
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            // 3. Extract the ID Token from the result
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleCred.idToken

                // 4. Exchange Google Token for Firebase Credential
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()

                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun getUser(): MockUser? = auth.currentUser?.let {
        MockUser("",it.displayName ?: "", it.email ?: "",it.uid)
    }

    override suspend fun ensureUserExists() {
        val user = auth.currentUser ?: return
        val userDocRef = db.collection("users").document(user.uid)

        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            // First time login! Create the user document.
            val newUser = MockUser(
                id = user.uid,
                email = user.email ?: "",
                name = user.displayName ?: ""
            )
            userDocRef.set(newUser).await()
        }
    }
}