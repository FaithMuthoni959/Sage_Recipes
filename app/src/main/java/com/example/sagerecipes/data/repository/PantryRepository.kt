package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.model.PantryItem
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant
import java.time.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The logged-in user's pantry (the database only ever returns their own rows). */
class PantryRepository {
    private val client get() = SupabaseProvider.client

    /** Everything in the pantry, sorted by ingredient name. Group by `ingredient.category` for the screen. */
    suspend fun getPantry(): List<PantryItem> =
        client.from("pantry_items")
            .select(Columns.raw("quantity, added_at, ingredients(id, name, category, image_url)"))
            .decodeList<PantryDto>()
            .map { PantryItem(it.ingredients, it.quantity, parse(it.addedAt)) }
            .sortedBy { it.ingredient.name }

    /** Just the ingredient ids. This is what you pass to recipe matching for "cook from my pantry". */
    suspend fun getPantryIngredientIds(): List<Long> =
        client.from("pantry_items").select(Columns.list("ingredient_id"))
            .decodeList<PantryIdDto>()
            .map { it.ingredientId }

    /**
     * Adds the ingredient, or changes its quantity if it is already there.
     * With no quantity given, an item that already exists is left untouched.
     */
    suspend fun addOrUpdate(ingredientId: Long, quantity: String? = null) {
        val exists = ingredientId in getPantryIngredientIds()
        if (!exists) {
            client.from("pantry_items").insert(PantryInsert(ingredientId, quantity))
        } else if (quantity != null) {
            client.from("pantry_items").update(PantryQuantityUpdate(quantity)) {
                filter { eq("ingredient_id", ingredientId) }
            }
        }
    }

    /** Adds several at once (for example, everything the user ticked), skipping ones already there. */
    suspend fun addAll(ingredientIds: List<Long>) {
        val existing = getPantryIngredientIds().toSet()
        val fresh = ingredientIds.distinct().filter { it !in existing }
        if (fresh.isNotEmpty()) {
            client.from("pantry_items").insert(fresh.map { PantryInsert(it, null) })
        }
    }

    suspend fun remove(ingredientId: Long) {
        client.from("pantry_items").delete { filter { eq("ingredient_id", ingredientId) } }
    }

    private fun parse(value: String): Instant = OffsetDateTime.parse(value).toInstant()
}

@Serializable
private data class PantryIdDto(@SerialName("ingredient_id") val ingredientId: Long)

@Serializable
private data class PantryInsert(
    @SerialName("ingredient_id") val ingredientId: Long,
    val quantity: String? = null,
)

@Serializable
private data class PantryQuantityUpdate(val quantity: String)

@Serializable
private data class PantryDto(
    val quantity: String? = null,
    @SerialName("added_at") val addedAt: String,
    val ingredients: Ingredient,
)