package com.example.sagerecipes

import com.example.sagerecipes.data.repository.AuthRepository

object TestSupport {
    private const val BASE_EMAIL = "dominicwaithaka6@gmail.com"   // <-- keep YOUR email here
    private const val PASSWORD = "SageTest#2026"

    /** The name every fresh test account is created with. Tests compare against this. */
    const val DISPLAY_NAME = "Repo Tester"

    /** Creates a brand-new test account and leaves the app logged in as that user. */
    suspend fun signInFreshUser() {
        val auth = AuthRepository()
        runCatching { auth.signOut() }
        val email = BASE_EMAIL.substringBefore("@") +
                "+sagetest" + System.currentTimeMillis() + "@" + BASE_EMAIL.substringAfter("@")
        val hasSession = auth.signUp(email, DISPLAY_NAME, PASSWORD)
        check(hasSession) { "No session after sign-up: turn OFF 'Confirm email' in Supabase." }
    }
}