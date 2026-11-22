package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.model.IngredientCategory
import com.example.sagerecipes.data.model.dbValue
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class IngredientRepository {
    private val client get() = SupabaseProvider.client

    /** Every ingredient, sorted by name. */
    suspend fun getALl(): List<Ingredient> =
        client.from("ingredients").select()
            .decodeList<Ingredient>()
            .sortedBy {it.name}

    suspend fun getByCategories(vararg categories: IngredientCategory): List<Ingredient> {
        val values = categories.map { it.dbValue }
        return client.from("ingredients").select {
            filter { isIn("category", values) }
        }.decodeList<Ingredient>().sortedBy { it.name }
    }

    /** The most used ingredients across recipes */
    suspend fun getPopular(limit: Int = 4): List<Ingredient> =
        client.postgrest
            .rpc("popular_ingredients", PopularParams(limit))
            .decodeList<Ingredient>()

    /** Search box on the ingredient screens. */
    suspend fun search(query: String): List<Ingredient> {
        if (query.isBlank()) return emptyList()
        return client.from("ingredients").select {
            filter { ilike("name", "%${query.trim()}%") }
        }.decodeList<Ingredient>().sortedBy { it.name }
    }
}

@Serializable
private data class Popularparams(@SerialName("max_results") val maxResults: Int)