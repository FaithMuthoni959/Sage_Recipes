package com.example.sagerecipes.data.model

/** The logged-in user's profile. */
data class UserProfile(
    val id: String,
    val displayName: String?,
    val email: String?,
) {
    /** For "Hello, Sarah". Falls back to the part of the email before the @. */
    val firstName: String?
        get() = displayName?.trim()?.substringBefore(' ')?.takeIf { it.isNotEmpty() }
            ?: email?.substringBefore('@')?.takeIf { it.isNotEmpty() }
}

/** Where a list of recommendations came from. */
enum class RecommendationSource { AI, SQL }

data class RecommendationResult(
    val recipes: List<RecommendedRecipe>,
    val source: RecommendationSource,
)