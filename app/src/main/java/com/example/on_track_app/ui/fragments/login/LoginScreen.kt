package com.example.on_track_app.ui.fragments.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.domain.viewModels.login.LoginViewModel
import com.example.on_track_app.domain.viewModels.login.SignInState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit, // Callback to navigate when login finishes
    viewModelFactory: AppViewModelFactory
) {
    // We get the ViewModel using your custom factory
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.signInState.collectAsState()
    val context = LocalContext.current

    // Colors
    val darkPink =  Color(0xFFA35B88)

    // React to state changes
    LaunchedEffect(state) {
        if (state is SignInState.Success) {
            onLoginSuccess()
        } else if (state is SignInState.Error) {
            Toast.makeText(context, (state as SignInState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to OnTrack",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state is SignInState.Loading) {
                CircularProgressIndicator(
                    color = darkPink
                )
            } else {
                Button(
                    onClick = { viewModel.signIn(context) },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkPink,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Sign in with Google")
                }
            }
        }
    }
}
