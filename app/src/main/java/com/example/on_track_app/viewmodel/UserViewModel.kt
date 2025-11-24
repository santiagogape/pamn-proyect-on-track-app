package com.example.on_track_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreService
import com.example.on_track_app.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {
    private val repo = FirestoreRepository(FirestoreService.firestore)

    private val _users = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val users: StateFlow<List<Map<String, Any>>> = _users

    fun loadUsers() {
        repo.getUsers { result ->
            _users.value = result
        }
    }

    fun addUser(username: String, email: String) {
        repo.addUser(username, email) { success ->
            if (success) {
                loadUsers()
            }
        }
    }
}