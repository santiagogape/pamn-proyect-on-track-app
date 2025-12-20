package com.example.on_track_app.viewModels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.auth.AuthClient
import com.example.on_track_app.data.auth.EnsureUserResult
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.utils.DebugLogcatLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val configureLocal: LocalConfigRepository,
    private val authClient: AuthClient,
    private val syncEngine: SyncEngine,
    private val remoteInit: suspend (UserDTO) -> Unit
) : ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Initial)
    val signInState = _signInState.asStateFlow()

    fun signIn(context: android.content.Context) {
        _signInState.value = SignInState.Loading

        viewModelScope.launch {
            val isSuccess = authClient.signIn(context)
            if (!isSuccess) {
                _signInState.value = SignInState.Error("Sign in failed")
                return@launch
            }

            when (val result = authClient.ensureUserExists()) {

                is EnsureUserResult.Created -> {
                    DebugLogcatLogger.logConfig(result.user,"LVM")
                    val dto = withContext(Dispatchers.IO) {
                        configureLocal.init(result.user)
                    }

                    withContext(Dispatchers.IO) {
                        remoteInit(dto)
                        syncEngine.propagateId(result.user.cloudId)
                    }
                }

                is EnsureUserResult.Exists -> {
                    configureLocal.init(result.user)
                    syncEngine.propagateId(result.user.cloudId)
                }
            }

            syncEngine.loadState()
            syncEngine.start()
            _signInState.value = SignInState.Success
        }

    }
}

sealed class SignInState {
    object Initial : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}