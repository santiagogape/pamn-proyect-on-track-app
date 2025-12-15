package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import com.example.on_track_app.data.abstractions.repositories.UserRepository

class UsersViewModel(private val repo: UserRepository) : ViewModel() {
}