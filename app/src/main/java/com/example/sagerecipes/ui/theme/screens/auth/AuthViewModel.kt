package com.example.sagerecipes.ui.theme.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sagerecipes.data.repository.AuthRepository
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = try { repo.isLoggedIn() } catch (e: Exception) { false }
            _state.update { it.copy(isLoggedIn = loggedIn, isCheckingSession = false) }
        }
    }
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            fail("Enter your email and password")
            return
        }
        launchAuth {
            repo.signIn(email.trim(), password)
            _state.update { it.copy(isLoggedIn = true)}
        }
    }
    fun register(email: String, name: String, password: String, confirmPassword: String) {
        val error = when {
            name.isBlank() -> "Enter your name"
            !email.contains("@") -> "Enter a valid email address"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
        if (error != null) {
            fail(error)
            return
        }
        launchAuth {
            val hasSession = repo.signUp(email.trim(), name.trim(), password)
            _state.update {
                if (hasSession) it.copy(isLoggedIn = true)
                else it.copy(infoMessage = "Account created. Check your email to confirm it, then log in.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { repo.signOut() } catch (e: Exception) { /* sign out locally anyway */ }
            _state.update { it.copy(isLoggedIn = false) }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun fail(message: String) {
        _state.update { it.copy(errorMessage = message, infoMessage = null) }
    }

    private fun launchAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail(friendlyMessage(e))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun friendlyMessage(e: Exception): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            e is UnknownHostException || "unable to resolve host" in msg -> "No internet connection"
            "invalid login credentials" in msg -> "Wrong email or password"
            "already registered" in msg || "already exists" in msg -> "An account with this email already exists"
            "email not confirmed" in msg -> "Please confirm your email first"
            else -> e.message ?: "Something went wrong. Try again."
        }
    }
}