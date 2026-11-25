package com.example.sagerecipes.data.remote

import com.example.sagerecipes.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.time.Duration.Companion.seconds

object SupabaseProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL.trim(),
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim(),
        ) {
            requestTimeout = 30.seconds
            install(Auth)
            install(Postgrest)
        }
    }
}