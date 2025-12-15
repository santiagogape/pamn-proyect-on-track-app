package com.example.on_track_app.viewModels.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.auth.AuthClient
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginViewModel(
    private val configureLocal: LocalConfigRepository,
    private val authClient: AuthClient,
    private val syncEngine: SyncEngine
) : ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Initial)
    val signInState = _signInState.asStateFlow()

    fun signIn(context: android.content.Context) {
        _signInState.value = SignInState.Loading

        viewModelScope.launch {
            val isSuccess = authClient.signIn(context)
            if (isSuccess) {
                // IMPORTANT: Create the user doc in Firestore if it doesn't exist
                authClient.ensureUserExists()
                runBlocking(Dispatchers.IO) {
                    authClient.getUser()?.let {configureLocal.init(it)}
                }
                syncEngine.start()
                runBlocking(Dispatchers.IO){
                    configureLocal.get().let {
                        Log.d("SyncDebug","\nuser:${it.userID}\n\n")
                        syncEngine.onLocalChange(it.userID,UserDTO::class)
                    }
                }
                _signInState.value = SignInState.Success
            } else {
                _signInState.value = SignInState.Error("Sign in failed")
            }
        }
    }
}

sealed class SignInState {
    object Initial : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}