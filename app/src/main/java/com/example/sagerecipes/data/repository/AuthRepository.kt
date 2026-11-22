package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val auth get() = SupabaseProvider.client.auth

    /** Returns true if the user is logged in right after sign-up (email confirmation off). */
    suspend fun signUp(email: String, name: String, password: String): Boolean {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("name", name) }   // read by the profile trigger
        }
        return auth.currentSessionOrNull() != null
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }
    suspend fun signOut() = auth.signOut()

    suspend fun isLoggedIn(): Boolean {
        auth.awaitInitialization()
        return auth.currentSessionOrNull() != null
    }
}