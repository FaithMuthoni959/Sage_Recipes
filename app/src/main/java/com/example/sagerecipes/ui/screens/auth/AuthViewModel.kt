package com.example.sagerecipes.ui.theme.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sagerecipes.data.repository.AuthRepository
import com.example.sagerecipes.data.repository.ProfileRepository
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
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
    val userName: String = "",
    val firstName: String = "",
    val userEmail: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val profileRepo = ProfileRepository()

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = try { authRepo.isLoggedIn() } catch (e: Exception) { false }
            if (loggedIn) {
                refreshProfile()
            } else {
                _state.update { it.copy(isLoggedIn = false, isCheckingSession = false) }
            }
        }
    }

    suspend fun refreshProfile() {
        val profile = try { profileRepo.getMyProfile() } catch (e: Exception) { null }
        val fallbackName = authRepo.currentUserName()
        val displayName = profile?.displayName?.takeIf { it.isNotBlank() } ?: fallbackName
        val firstName = profile?.firstName ?: displayName.substringBefore(' ')
        val email = profile?.email ?: authRepo.currentUserEmail()

        _state.update {
            it.copy(
                isLoggedIn = true,
                isCheckingSession = false,
                userName = displayName,
                firstName = firstName,
                userEmail = email
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            fail("Enter your email and password")
            return
        }
        launchAuth {
            authRepo.signIn(email.trim(), password)
            refreshProfile()
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
            val hasSession = authRepo.signUp(email.trim(), name.trim(), password)
            if (hasSession) {
                refreshProfile()
            } else {
                _state.update {
                    it.copy(infoMessage = "Account created. Check your email to confirm it, then log in.")
                }
            }
        }
    }

    fun updateDisplayName(newName: String, onSuccess: () -> Unit = {}) {
        launchAuth {
            profileRepo.updateDisplayName(newName)
            refreshProfile()
            _state.update { it.copy(infoMessage = "Profile updated") }
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { authRepo.signOut() } catch (e: Exception) { /* sign out locally */ }
            _state.update {
                it.copy(isLoggedIn = false, userName = "", firstName = "", userEmail = "")
            }
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
                Log.e("SageAuth", "Supabase auth error", e)
                fail(friendlyMessage(e))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun friendlyMessage(e: Exception): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            e is UnknownHostException || "unable to resolve host" in msg ->
                "No internet connection (check emulator Wi-Fi)"
            e is HttpRequestTimeoutException || e is ConnectTimeoutException || e is SocketTimeoutException || "timed out" in msg ->
                "Connection to server timed out. Check emulator internet."
            "invalid api key" in msg || "apikey" in msg ->
                "Invalid Supabase API key in local.properties"
            "invalid login credentials" in msg ->
                "Wrong email or password"
            "already registered" in msg || "already exists" in msg || "user_already_exists" in msg ->
                "An account with this email already exists"
            "email not confirmed" in msg ->
                "Please confirm your email first"
            else -> e.message ?: "Something went wrong. Try again."
        }
    }
}