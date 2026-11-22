package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.error.BackendException
import com.example.sagerecipes.data.error.ErrorKind
import com.example.sagerecipes.data.error.messageFor
import com.example.sagerecipes.data.model.UserProfile
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The logged-in user's own profile (name for the greeting, email for the profile screen). */
class ProfileRepository {
    private val client get() = SupabaseProvider.client

    /** Null if nobody is logged in. */
    suspend fun getMyProfile(): UserProfile? {
        client.auth.awaitInitialization()
        val user = client.auth.currentSessionOrNull()?.user ?: return null
        val row = client.from("profiles").select(Columns.list("id", "display_name")) {
            filter { eq("id", user.id) }
        }.decodeList<ProfileDto>().firstOrNull()
        return UserProfile(id = user.id, displayName = row?.displayName, email = user.email)
    }

    suspend fun updateDisplayName(name: String) {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Name can't be empty" }
        val user = client.auth.currentSessionOrNull()?.user
            ?: throw BackendException(ErrorKind.NOT_LOGGED_IN, messageFor(ErrorKind.NOT_LOGGED_IN))
        client.from("profiles").update(NameUpdate(clean)) {
            filter { eq("id", user.id) }
        }
    }
}

@Serializable
private data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
)

@Serializable
private data class NameUpdate(@SerialName("display_name") val displayName: String)