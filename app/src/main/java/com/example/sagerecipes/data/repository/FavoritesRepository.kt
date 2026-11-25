package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.model.FavoriteRecipe
import com.example.sagerecipes.data.model.RecentlyMadeRecipe
import com.example.sagerecipes.data.model.RecipeSummary
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Favorites and cooking history for the logged-in user (the database only ever returns their own rows). */
class FavoritesRepository {
    private val client get() = SupabaseProvider.client

    private val recipeColumns =
        "id, title, description, image_url, cook_time_minutes, difficulty, avg_rating"

    /** Ids of all favorites. One call, handy for drawing hearts on a list of recipes. */
    suspend fun getFavoriteIds(): Set<Long> =
        client.from("favorites").select(Columns.list("recipe_id"))
            .decodeList<FavoriteIdDto>()
            .map { it.recipeId }
            .toSet()

    suspend fun isFavorite(recipeId: Long): Boolean =
        client.from("favorites").select(Columns.list("recipe_id")) {
            filter { eq("recipe_id", recipeId) }
        }.decodeList<FavoriteIdDto>().isNotEmpty()

    /** Safe to call twice: does nothing if it is already a favorite. */
    suspend fun add(recipeId: Long) {
        if (!isFavorite(recipeId)) {
            client.from("favorites").insert(FavoriteInsert(recipeId))
        }
    }

    suspend fun remove(recipeId: Long) {
        client.from("favorites").delete { filter { eq("recipe_id", recipeId) } }
    }

    /** Flips the heart. Returns the new state: true if the recipe is now a favorite. */
    suspend fun toggle(recipeId: Long): Boolean =
        if (isFavorite(recipeId)) {
            remove(recipeId)
            false
        } else {
            add(recipeId)
            true
        }

    /** Newest favorite first, each with its "last made" time. */
    suspend fun getFavorites(): List<FavoriteRecipe> {
        val rows = client.from("favorites")
            .select(Columns.raw("created_at, recipes($recipeColumns)"))
            .decodeList<FavoriteDto>()
        val lastMade = lastMadeByRecipe()
        return rows
            .map { FavoriteRecipe(it.recipes, parse(it.createdAt), lastMade[it.recipes.id]) }
            .sortedByDescending { it.favoritedAt }
    }

    /** "I cooked this": records it in the history. */
    suspend fun markAsMade(recipeId: Long) {
        client.from("cook_history").insert(CookInsert(recipeId))
    }

    /** Most recently cooked first, one entry per recipe. */
    suspend fun getRecentlyMade(limit: Int = 20): List<RecentlyMadeRecipe> {
        val rows = client.from("cook_history")
            .select(Columns.raw("made_at, recipes($recipeColumns)"))
            .decodeList<CookDto>()
        return rows
            .map { RecentlyMadeRecipe(it.recipes, parse(it.madeAt)) }
            .sortedByDescending { it.madeAt }
            .distinctBy { it.recipe.id }
            .take(limit)
    }

    private suspend fun lastMadeByRecipe(): Map<Long, Instant> =
        try {
            client.from("cook_history").select(Columns.list("recipe_id", "made_at"))
                .decodeList<CookTimeDto>()
                .groupBy { it.recipeId }
                .mapValues { (_, entries) -> entries.maxOf { parse(it.madeAt) } }
        } catch (_: Exception) {
            emptyMap()
        }

    private fun parse(value: String): Instant = OffsetDateTime.parse(value).toInstant()
}

@Serializable
private data class FavoriteIdDto(@SerialName("recipe_id") val recipeId: Long)

@Serializable
private data class FavoriteInsert(@SerialName("recipe_id") val recipeId: Long)

@Serializable
private data class FavoriteDto(
    @SerialName("created_at") val createdAt: String,
    val recipes: RecipeSummary,
)

@Serializable
private data class CookInsert(@SerialName("recipe_id") val recipeId: Long)

@Serializable
private data class CookDto(
    @SerialName("made_at") val madeAt: String,
    val recipes: RecipeSummary,
)

@Serializable
private data class CookTimeDto(
    @SerialName("recipe_id") val recipeId: Long,
    @SerialName("made_at") val madeAt: String,
)