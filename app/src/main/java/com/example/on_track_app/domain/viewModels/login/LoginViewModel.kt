package com.example.on_track_app.domain.viewModels.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.on_track_app.data.auth.GoogleAuthClient
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Initial)
    val signInState = _signInState.asStateFlow()

    fun signIn(context: Context) {
        _signInState.value = SignInState.Loading

        viewModelScope.launch {
            val isSuccess = googleAuthClient.signIn(context)
            if (isSuccess) {
                // IMPORTANT: Create the user doc in Firestore if it doesn't exist
                googleAuthClient.ensureUserExists()
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