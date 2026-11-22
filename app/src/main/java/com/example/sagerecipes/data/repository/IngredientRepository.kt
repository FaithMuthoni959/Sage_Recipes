package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.model.IngredientCategory
import com.example.sagerecipes.data.model.dbValue
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class IngredientRepository {
    private val client get() = SupabaseProvider.client

    /** Every ingredient, sorted by name. */
    suspend fun getAll(): List<Ingredient> =
        client.from("ingredients").select()
            .decodeList<Ingredient>()
            .sortedBy { it.name }

    /** e.g. getByCategories(VEGETABLE, PROTEIN) for the Ingredients screen. */
    suspend fun getByCategories(vararg categories: IngredientCategory): List<Ingredient> {
        val values = categories.map { it.dbValue }
        return client.from("ingredients").select {
            filter { isIn("category", values) }
        }.decodeList<Ingredient>().sortedBy { it.name }
    }

    /** The most-used ingredients across recipes (the "Popular Ingredients" row). */
    suspend fun getPopular(limit: Int = 4): List<Ingredient> =
        client.postgrest
            .rpc("popular_ingredients", PopularParams(limit))
            .decodeList<Ingredient>()

    /** Typed search; also matches aliases such as "cilantro" -> Coriander. */
    suspend fun search(query: String, limit: Int = 20): List<Ingredient> {
        if (query.isBlank()) return emptyList()
        return client.postgrest
            .rpc("search_ingredients", SearchParams(query.trim(), limit))
            .decodeList<Ingredient>()
    }
}

@Serializable
private data class PopularParams(@SerialName("max_results") val maxResults: Int)

@Serializable
private data class SearchParams(val q: String, @SerialName("max_results") val maxResults: Int)